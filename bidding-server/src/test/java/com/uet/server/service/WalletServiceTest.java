package com.uet.server.service;

import com.uet.server.database.dao.AuctionDAO;
import com.uet.server.database.dao.WalletDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    private WalletService walletService;
    private WalletDAO mockWalletDAO;
    private AuctionDAO mockAuctionDAO;

    @BeforeEach
    public void setUp() {
        mockWalletDAO = mock(WalletDAO.class);
        mockAuctionDAO = mock(AuctionDAO.class);
        walletService = new WalletService(mockWalletDAO, mockAuctionDAO);
    }

    @Test
    public void testCheckAndFreezeBalance_Success() {
        
        when(mockWalletDAO.getAvailableBalanceForAuction("user1", "auc1")).thenReturn(1000.0);
        
        
        boolean result = walletService.checkAndFreezeBalance("user1", "auc1", 500.0);
        assertTrue(result, "Hệ thống phải cho phép đóng băng số dư nếu ví còn đủ tiền");
    }

    @Test
    public void testCheckAndFreezeBalance_Fail_InsufficientFunds() {
        
        when(mockWalletDAO.getAvailableBalanceForAuction("user2", "auc1")).thenReturn(200.0);
        
        
        boolean result = walletService.checkAndFreezeBalance("user2", "auc1", 500.0);
        assertFalse(result, "Hệ thống phải từ chối nếu số tiền đặt cao hơn số dư khả dụng");
    }

    @Test
    public void testProcessAuctionEnd_TriggerCorrectDAO() {
        when(mockAuctionDAO.forceEndAuctionAndProcessTransaction("auc1")).thenReturn(true);
        
        boolean result = walletService.processAuctionEnd("auc1");
        
        assertTrue(result);
        verify(mockAuctionDAO, times(1)).forceEndAuctionAndProcessTransaction("auc1");
    }
}
