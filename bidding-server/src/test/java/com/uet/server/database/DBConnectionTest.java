package com.uet.server.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DBConnectionTest {

    @Test
    public void testGetConnection() throws Exception {
        
        Connection conn = DBConnection.getConnection();

        
        assertNotNull(conn, "Connection object should not be null. Check db.properties and MySQL server.");

        
        assertFalse(conn.isClosed(), "Connection should be open.");

        
        conn.close();
    }
}
