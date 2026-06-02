package com.uet.common.model;

import com.uet.common.model.auction.AuctionItem;
import com.uet.common.model.transaction.Transaction;
import com.uet.common.model.user.User;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class SerializationTest {

    @Test
    public void testUserSerialization() throws Exception {
        User user = new User();
        user.setId("U123");
        user.setUsername("testuser");

        User deserializedUser = serializeAndDeserialize(user);
        assertNotNull(deserializedUser);
        assertEquals("U123", deserializedUser.getId());
        assertEquals("testuser", deserializedUser.getUsername());
    }

    @Test
    public void testAuctionItemSerialization() throws Exception {
        AuctionItem item = new AuctionItem();
        item.setAuctionId("A123");
        item.setProductName("Test Product");

        AuctionItem deserializedItem = serializeAndDeserialize(item);
        assertNotNull(deserializedItem);
        assertEquals("A123", deserializedItem.getAuctionId());
        assertEquals("Test Product", deserializedItem.getProductName());
    }

    @Test
    public void testTransactionSerialization() throws Exception {
        Transaction transaction = new Transaction(1L, "2007", 100.0, "DEPOSIT", "2026-06-02", "SUCCESS");

        Transaction deserializedTx = serializeAndDeserialize(transaction);
        assertNotNull(deserializedTx);
        assertEquals(1L, deserializedTx.getId());
        assertEquals("2007", deserializedTx.getUserId());
        assertEquals(100.0, deserializedTx.getAmount());
    }

    @SuppressWarnings("unchecked")
    private <T> T serializeAndDeserialize(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        T deserialized = (T) ois.readObject();
        ois.close();

        return deserialized;
    }
}
