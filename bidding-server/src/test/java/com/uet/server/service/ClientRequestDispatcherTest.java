package com.uet.server.service;

import com.uet.common.network.DeleteUserRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.UserDAO;
import com.uet.server.network.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientRequestDispatcherTest {

    private ClientRequestDispatcher dispatcher;
    private UserDAO mockUserDAO;
    private ClientHandler mockClientHandler;

    @BeforeEach
    public void setUp() throws Exception {
        dispatcher = new ClientRequestDispatcher();
        
        // Tạo các đối tượng giả (Mock)
        mockUserDAO = mock(UserDAO.class);
        mockClientHandler = mock(ClientHandler.class);
        
        // Sử dụng Java Reflection để thay thế UserDAO thật (kết nối MySQL) bằng mockUserDAO
        Field userDAOField = ClientRequestDispatcher.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(dispatcher, mockUserDAO);
    }

    @Test
    public void testDispatch_LogoutRequest() {
        // Gửi một chuỗi "LOGOUT" làm request
        boolean keepRunning = dispatcher.dispatch("LOGOUT", mockClientHandler);
        
        // Đảm bảo hàm trả về false (nghĩa là đóng kết nối)
        assertFalse(keepRunning);
        
        // Dùng ArgumentCaptor để "bắt" lại gói tin Response mà dispatcher gửi cho client
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response response = responseCaptor.getValue();
        assertTrue(response.isSuccess());
        assertEquals("Đăng xuất thành công", response.getMessage());
    }

    @Test
    public void testDispatch_DeleteUserRequest_Success() throws Exception {
        // 1. Chuẩn bị Request
        DeleteUserRequest request = new DeleteUserRequest("user123");
        
        // Không làm gì cả khi gọi userDAO.deleteUser() (giả lập xóa thành công không báo lỗi)
        doNothing().when(mockUserDAO).deleteUser("user123");
        
        // 2. Thực thi Dispatcher
        boolean keepRunning = dispatcher.dispatch(request, mockClientHandler);
        
        // 3. Kiểm tra
        assertTrue(keepRunning); // Server vẫn tiếp tục phục vụ client này
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response response = responseCaptor.getValue();
        assertTrue(response.isSuccess());
        assertEquals("Xóa tài khoản người dùng thành công!", response.getMessage());
    }
    
    @Test
    public void testDispatch_DeleteUserRequest_Exception() throws Exception {
        // Giả lập tình huống lỗi Database khi xóa
        DeleteUserRequest request = new DeleteUserRequest("user123");
        doThrow(new RuntimeException("DB Connection Lost")).when(mockUserDAO).deleteUser("user123");
        
        dispatcher.dispatch(request, mockClientHandler);
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response response = responseCaptor.getValue();
        assertFalse(response.isSuccess());
        assertEquals("Lỗi Server: Không thể xóa tài khoản người dùng.", response.getMessage());
    }
}
