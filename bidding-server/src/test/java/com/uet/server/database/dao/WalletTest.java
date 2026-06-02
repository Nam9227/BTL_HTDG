package com.uet.server.database.dao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletTest {

    @Test
    public void testAvailableBalanceCalculation() {
        double totalBalance = 10000000.0; 
        double frozenBalance = 6000000.0; 

        double available = totalBalance - frozenBalance;

        
        Assertions.assertEquals(4000000.0, available, "Logic tính số dư khả dụng bị sai rồi Nam ơi!");
    }
}