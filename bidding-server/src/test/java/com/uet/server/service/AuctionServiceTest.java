package com.uet.server.service;

import com.uet.common.exception.AuctionClosedException;
import com.uet.common.exception.InvalidBidException;
import com.uet.common.network.BidRequest;
import com.uet.common.network.Response;
import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.BidDAO;
import com.uet.server.database.dao.WalletDAO;
import com.uet.server.network.ClientHandler;
import com.uet.common.model.auction.AuctionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AuctionServiceTest {

    private AuctionRealtimeService auctionService;
    private AuctionDAO mockAuctionDAO;
    private BidDAO mockBidDAO;
    private WalletDAO mockWalletDAO;
    private ClientHandler mockClientHandler;

    @BeforeEach
    public void setUp() {
        mockAuctionDAO = mock(AuctionDAO.class);
        mockBidDAO = mock(BidDAO.class);
        mockWalletDAO = mock(WalletDAO.class);
        mockClientHandler = mock(ClientHandler.class);
        
        auctionService = new AuctionRealtimeService(mockAuctionDAO, mockBidDAO, mockWalletDAO);
    }

    @Test
    public void testPlaceBid_ValidCase() throws Exception {
        BidRequest request = new BidRequest("auc1", "bidder1", 200.0);
        AuctionItem mockItem = new AuctionItem();
        mockItem.setSellerId("seller1");
        
        when(mockAuctionDAO.getAuctionById("auc1", false)).thenReturn(mockItem);
        when(mockWalletDAO.getAvailableBalanceForAuction("bidder1", "auc1")).thenReturn(500.0);
        when(mockBidDAO.handleBid(request)).thenReturn(Response.success("NEW_BID", 200.0));
        
        auctionService.placeBid(request, mockClientHandler);
        
        
        verify(mockBidDAO, times(1)).handleBid(request);
        
        
        
        
    }

    @Test
    public void testPlaceBid_InvalidBid_ThrowsInvalidBidException() throws Exception {
        BidRequest request = new BidRequest("auc1", "bidder1", 50.0);
        AuctionItem mockItem = new AuctionItem();
        mockItem.setSellerId("seller1");
        
        when(mockAuctionDAO.getAuctionById("auc1", false)).thenReturn(mockItem);
        when(mockWalletDAO.getAvailableBalanceForAuction("bidder1", "auc1")).thenReturn(500.0);
        
        when(mockBidDAO.handleBid(request)).thenThrow(new InvalidBidException("Giá đặt phải lớn hơn giá hiện tại."));
        
        auctionService.placeBid(request, mockClientHandler);
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response res = responseCaptor.getValue();
        assertFalse(res.isSuccess());
        assertEquals("Giá đặt phải lớn hơn giá hiện tại.", res.getMessage());
    }

    @Test
    public void testPlaceBid_AuctionClosed_ThrowsAuctionClosedException() throws Exception {
        BidRequest request = new BidRequest("auc1", "bidder1", 100.0);
        AuctionItem mockItem = new AuctionItem();
        mockItem.setSellerId("seller1");
        
        when(mockAuctionDAO.getAuctionById("auc1", false)).thenReturn(mockItem);
        when(mockWalletDAO.getAvailableBalanceForAuction("bidder1", "auc1")).thenReturn(500.0);
        
        when(mockBidDAO.handleBid(request)).thenThrow(new AuctionClosedException("Phiên đấu giá đã đóng hoặc chưa bắt đầu."));
        
        auctionService.placeBid(request, mockClientHandler);
        
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockClientHandler).send(responseCaptor.capture());
        
        Response res = responseCaptor.getValue();
        assertFalse(res.isSuccess());
        assertEquals("Phiên đấu giá đã đóng hoặc chưa bắt đầu.", res.getMessage());
    }
}
