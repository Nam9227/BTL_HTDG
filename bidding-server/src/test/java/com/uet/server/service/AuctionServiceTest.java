package com.uet.server.service;

import com.uet.common.network.AddProductRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.network.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuctionServiceTest {

    private AuctionService auctionService;
    private AuctionDAO mockAuctionDAO;
    private ClientHandler mockClientHandler;

    @BeforeEach
    public void setUp() {
        mockAuctionDAO = mock(AuctionDAO.class);
        mockClientHandler = mock(ClientHandler.class);
        
        // Truyền mockDAO qua constructor (Dependency Injection)
        auctionService = new AuctionService(mockAuctionDAO);
    }

    @Test
    public void testHandleRegisterProduct_NoImage_Success() {
        // 1. Chuẩn bị Request không có hình ảnh
        AddProductRequest request = new AddProductRequest(
            "seller123", "Laptop Dell", "Desc", 1000.0, null, "ELECTRONICS", "Dell",
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(1)
        );
        // Bỏ qua xử lý File Storage (đã để null ở tham số thứ 5)
        
        // Giả lập lưu vào DB thành công
        when(mockAuctionDAO.createNewAuction(
                anyString(), anyString(), any(), anyDouble(), anyString(),
                any(), any(), any(), any(), any(), any()
        )).thenReturn(true);

        // 2. Thực thi
        auctionService.handleRegisterProduct(request, mockClientHandler);

        // 3. Kiểm tra kết quả
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response response = responseCaptor.getValue();
        assertTrue(response.isSuccess());
        assertEquals("Đăng bán sản phẩm đấu giá thành công! Vui lòng chờ Admin phê duyệt.", response.getMessage());
        
        // Đảm bảo DAO thực sự được gọi
        verify(mockAuctionDAO, times(1)).createNewAuction(
                anyString(), anyString(), any(), anyDouble(), anyString(),
                any(), any(), any(), any(), any(), any()
        );
    }
}
