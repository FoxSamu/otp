package net.shadew.otp;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {
    @Test
    void testBase32Encode() {
        byte[] bytes = "Hello world".getBytes(StandardCharsets.US_ASCII);
        assertEquals("JBSWY3DPEB3W64TMMQ======", KeyGenerator.encodeBase32(bytes, true));
        assertEquals("JBSWY3DPEB3W64TMMQ", KeyGenerator.encodeBase32(bytes, false));
    }

    @Test
    void testBase32Decode() {
        assertEquals("Hello world", new String(KeyGenerator.decodeBase32("JBSWY3DPEB3W64TMMQ======"), StandardCharsets.US_ASCII));
        assertEquals("Hello world", new String(KeyGenerator.decodeBase32("JBSWY3DPEB3W64TMMQ"), StandardCharsets.US_ASCII));
    }
}
