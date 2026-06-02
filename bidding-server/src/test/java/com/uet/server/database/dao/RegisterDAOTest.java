package com.uet.server.database.dao;

import com.uet.common.network.RegisterRequest;
import com.uet.common.network.Response;
import com.uet.server.database.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RegisterDAOTest {

    private RegisterDAO registerDAO;
    private MockedStatic<DBConnection> mockedDBConnection;

    @BeforeEach
    public void setUp() {
        registerDAO = new RegisterDAO();
        // Bắt đầu mock static method DBConnection.getConnection()
        mockedDBConnection = mockStatic(DBConnection.class);
    }

    @AfterEach
    public void tearDown() {
        // Cần đóng mock static sau mỗi test để tránh ảnh hưởng đến test khác
        mockedDBConnection.close();
    }

    @Test
    public void testRegister_EmailExists() throws Exception {
        // 1. Chuẩn bị dữ liệu đầu vào
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@gmail.com", "Test User");

        // 2. Làm giả các đối tượng Database
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(Connection.class); // Vẫn mock được dù là interface
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        // Khi DBConnection.getConnection() được gọi, trả về mockConnection
        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
        
        // Khi prepareStatement() được gọi với bất kỳ chuỗi SQL nào, trả về mockStmt
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        
        // Khi executeQuery() được gọi để kiểm tra email, trả về mockResultSet
        when(mockStmt.executeQuery()).thenReturn(mockResultSet);
        
        // Cấu hình mockResultSet: giả sử next() trả về true -> tức là email đã có trong DB
        when(mockResultSet.next()).thenReturn(true);

        // 3. Thực thi hàm cần test
        Response response = registerDAO.handleRegister(request);

        // 4. Kiểm tra kết quả
        assertFalse(response.isSuccess());
        assertEquals("Email đã tồn tại", response.getMessage());
        
        // Xác minh rằng hàm setString đã được gọi với email để kiểm tra
        verify(mockStmt).setString(1, "test@gmail.com");
    }

    @Test
    public void testRegister_Success() throws Exception {
        // Giả lập luồng đăng ký thành công hoàn toàn
        RegisterRequest request = new RegisterRequest("newuser", "pass", "new@gmail.com", "New User");

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        
        // Cả checkEmail, checkUser, và generateUniqueId đều không tìm thấy bản ghi trùng
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false); 
        
        // Cho executeUpdate() chạy trơn tru
        when(mockStmt.executeUpdate()).thenReturn(1);

        Response response = registerDAO.handleRegister(request);

        assertTrue(response.isSuccess());
        assertEquals("Tạo tài khoản thành công", response.getMessage());
        
        // Xác minh transaction đã được commit
        verify(mockConn).commit();
    }
}
