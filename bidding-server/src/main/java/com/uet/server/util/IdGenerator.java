package com.uet.server.util;
import java.util.Random;

public class IdGenerator {
    private static final Random random = new Random();

    public static String generateId() {
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(number);
    }
}


