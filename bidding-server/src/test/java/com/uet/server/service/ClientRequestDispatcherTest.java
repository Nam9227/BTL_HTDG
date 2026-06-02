package com.uet.server.service;

import com.uet.common.network.AddProductRequest;
import com.uet.common.network.DeleteUserRequest;
import com.uet.common.network.LoginRequest;
import com.uet.common.network.Response;
import com.uet.server.database.DBConnection;
import com.uet.server.database.dao.UserDAO;
import com.uet.server.network.ClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClientRequestDispatcherTest {

    private ClientRequestDispatcher dispatcher;
    private UserDAO mockUserDAO;
    private ClientHandler mockClientHandler;
    private MockedStatic<DBConnection> mockedDBConnection;

    @BeforeEach
    public void setUp() throws Exception {
        dispatcher = new ClientRequestDispatcher();
        
        mockUserDAO = mock(UserDAO.class);
        mockClientHandler = mock(ClientHandler.class);
        
        // Mock DBConnection to prevent static AdminDAO logging from connecting to DB
        mockedDBConnection = mockStatic(DBConnection.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        
        // Dùng reflection để thay thế userDAO
        Field userDAOField = ClientRequestDispatcher.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(dispatcher, mockUserDAO);
    }

    @AfterEach
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testDispatch_LoginRequest() {
        LoginRequest request = new LoginRequest("test", "test");
        when(mockUserDAO.handleLogin(request)).thenReturn(Response.success("OK", null));
        
        boolean keepRunning = dispatcher.dispatch(request, mockClientHandler);
        
        assertTrue(keepRunning);
        verify(mockUserDAO, times(1)).handleLogin(request);
        verify(mockClientHandler, times(1)).send(any(Response.class));
    }

    @Test
    public void testDispatch_AddProductRequest() {
        AddProductRequest request = new AddProductRequest("u1", "p1", "desc", 100.0, null, "cat", "brand", null, null);
        
        // Thực thi. Vì AuctionService được tạo mới trong ClientRequestDispatcher
        // Ta chỉ cần đảm bảo dispatch chạy không lỗi (tuy logic bên trong có thể phụ thuộc DB)
        // Mock tĩnh DBConnection đã được dựng sẵn.
        boolean keepRunning = dispatcher.dispatch(request, mockClientHandler);
        
        assertTrue(keepRunning);
    }

    @Test
    public void testDispatch_DeleteUserRequest() {
        DeleteUserRequest request = new DeleteUserRequest("user123");
        doNothing().when(mockUserDAO).deleteUser("user123");
        
        boolean keepRunning = dispatcher.dispatch(request, mockClientHandler);
        
        assertTrue(keepRunning);
        verify(mockUserDAO, times(1)).deleteUser("user123");
        verify(mockClientHandler, times(1)).send(argThat(res -> ((Response)res).isSuccess()));
    }
}
