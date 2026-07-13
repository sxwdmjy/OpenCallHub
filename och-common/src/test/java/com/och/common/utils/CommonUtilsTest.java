package com.och.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUtilsTest {

    @Test
    void encryptPasswordUsesBCryptHash() {
        String rawPassword = "Admin@123456";

        String encrypted = SecurityUtils.encryptPassword(rawPassword);

        assertNotEquals(rawPassword, encrypted);
        assertTrue(new BCryptPasswordEncoder().matches(rawPassword, encrypted));
    }

    @Test
    void rightPadUsesProvidedPaddingString() {
        assertEquals("0000", StringUtils.rightPad("", 4, "0"));
    }
}
