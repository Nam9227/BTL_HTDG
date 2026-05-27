package com.uet.server.database.dao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletTest {

    @Test
    public void testAvailableBalanceCalculation() {
        double totalBalance = 10000000.0; // 10 triệu
        double frozenBalance = 6000000.0; // Đang giam 6 triệu ở phiên khác

        double available = totalBalance - frozenBalance;

        // Mong đợi số dư khả dụng phải là 4 triệu
        Assertions.assertEquals(4000000.0, available, "Logic tính số dư khả dụng bị sai rồi Nam ơi!");
    }
}