package com.uet.common.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordRequestTest {

    @Test
    public void testConstructorAndGetters() {
        ChangePasswordRequest request = new ChangePasswordRequest("user123", "oldPass", "newPass");
        
        assertEquals("user123", request.getUserId());
        assertEquals("oldPass", request.getOldPassword());
        assertEquals("newPass", request.getNewPassword());
    }

    @Test
    public void testSetters() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId("user456");
        request.setOldPassword("pass1");
        request.setNewPassword("pass2");
        
        assertEquals("user456", request.getUserId());
        assertEquals("pass1", request.getOldPassword());
        assertEquals("pass2", request.getNewPassword());
    }
}
