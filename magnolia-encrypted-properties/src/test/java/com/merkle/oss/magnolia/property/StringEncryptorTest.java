package com.merkle.oss.magnolia.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringEncryptorTest {
    private StringEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new StringEncryptor("someMasterPassword");
    }

    @Test
    void test() {
        final String input = "secret message 42";
        final String encrypted = encryptor.encrypt(input);
        final String decrypted = encryptor.decrypt(encrypted);
        Assertions.assertEquals(input, decrypted, "encrypt -> decrypt doesn't match input!");
    }
}
