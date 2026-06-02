package com.uet.server.database.dao;

import com.uet.common.network.LoginRequest;
import com.uet.common.network.Response;
import com.uet.server.database.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserDAOTest {

    private UserDAO userDAO;
    private MockedStatic<DBConnection> mockedDBConnection;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        mockedDBConnection = mockStatic(DBConnection.class);
    }

    @AfterEach
    public void tearDown() {
        mockedDBConnection.close();
    }

    @Test
    public void testHandleLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("wronguser", "wrongpass");

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        
        // Không tìm thấy user nào (rs.next() = false)
        when(mockRs.next()).thenReturn(false);

        Response response = userDAO.handleLogin(request);

        assertFalse(response.isSuccess());
        assertEquals("Sai tài khoản hoặc mật khẩu", response.getMessage());
    }
    
    @Test
    public void testHandleLogin_UserInactive() throws Exception {
        LoginRequest request = new LoginRequest("user123", "password");

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        
        // Trả về một user bị khóa (active = false)
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("id")).thenReturn("u1");
        when(mockRs.getBoolean("active")).thenReturn(false); 
        // Các trường khác tạm để null/default vì logic kiểm tra active chạy ngay sau khi check null user

        Response response = userDAO.handleLogin(request);

        assertFalse(response.isSuccess());
        assertEquals("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.", response.getMessage());
    }
    
    @Test
    public void testHandleLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("user123", "password");

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        
        // Giả lập dữ liệu trả về từ MySQL
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("id")).thenReturn("u1");
        when(mockRs.getString("username")).thenReturn("user123");
        when(mockRs.getString("role")).thenReturn("USER");
        when(mockRs.getBoolean("active")).thenReturn(true); 
        when(mockRs.getBigDecimal("balance")).thenReturn(BigDecimal.valueOf(1000));
        
        // Hàm cập nhật last_login chạy trơn tru
        when(mockStmt.executeUpdate()).thenReturn(1);

        Response response = userDAO.handleLogin(request);

        assertTrue(response.isSuccess());
        assertEquals("Đăng nhập thành công", response.getMessage());
        assertNotNull(response.getData()); 
    }
}
