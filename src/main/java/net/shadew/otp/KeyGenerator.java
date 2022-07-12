package net.shadew.otp;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * A generator for keys.
 */
public final class KeyGenerator {
    /**
     * A standard key generator instance, which generates keys of 20 bytes (which is exactly 32 Base-32 characters).
     * Instantiated with a new {@link SecureRandom} instance.
     */
    public static final KeyGenerator STANDARD = new KeyGenerator(new SecureRandom(), 20);

    private final SecureRandom random;
    private final int length;

    /**
     * Create a key generator.
     *
     * @param random The {@link SecureRandom} instance to use
     * @param length The amount of bytes in the generated keys
     */
    public KeyGenerator(SecureRandom random, int length) {
        this.random = random;
        this.length = length;
    }

    /**
     * Create a key generator with a new {@link SecureRandom} instance.
     *
     * @param length The amount of bytes in the generated keys
     */
    public KeyGenerator(int length) {
        this(new SecureRandom(), length);
    }

    /**
     * Generates a key.
     *
     * @return The generated key
     */
    public byte[] generate() {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates a key as Base-32 string (unpadded).
     *
     * @return The generated key
     */
    public String generateBase32() {
        return encodeBase32(generate());
    }

    private static final char[] BASE_32_ENC = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', '2', '3', '4', '5', '6', '7'
    };

    private static final byte[] BASE_32_DEC = new byte[128];

    static {
        Arrays.fill(BASE_32_DEC, (byte) -1);

        for (int i = 0; i < 32; i++) {
            char c = BASE_32_ENC[i];
            BASE_32_DEC[c] = (byte) i;
            BASE_32_DEC[Character.toLowerCase(c)] = (byte) i;
        }
    }

    /**
     * Encodes the given byte array into Base-32. No '=' symbols are added as padding.
     *
     * @param bytes The bytes to encode, must not be null
     * @return The Base-32 string
     */
    public static String encodeBase32(byte[] bytes) {
        return encodeBase32(bytes, false);
    }

    /**
     * Encodes the given byte array into Base-32.
     *
     * @param bytes   The bytes to encode, must not be null
     * @param padding Whether '=' symbols must be added as padding
     * @return The Base-32 string
     */
    public static String encodeBase32(byte[] bytes, boolean padding) {
        Objects.requireNonNull(bytes, "Cannot encode a null array");
        int blen = bytes.length;
        byte[] values = new byte[(int) ((long) blen * 8L / 5L) + 8];
        long bitOff = 0;

        for (byte b : bytes) {
            int bv = b & 0xFF;
            int bit = 0;

            while (bit < 8) {
                int pos = (int) (bitOff / 5);
                int boff = (int) (bitOff % 5);

                int bits = Math.min(5 - boff, 8 - bit);
                int rs = 3 - bit + boff;
                int val = rs < 0 ? bv << -rs : bv >>> rs;

                values[pos] |= val & 0x1F;

                bit += bits;
                bitOff += bits;
            }
        }

        int len = (int) (bitOff / 5) + (bitOff % 5 == 0 ? 0 : 1);

        char[] chars = new char[padding ? -Math.floorDiv(-len, 8) * 8 : len];
        for (int i = 0; i < len; i++) {
            chars[i] = BASE_32_ENC[values[i]];
        }
        // Add padding
        for (int i = len, l = chars.length; i < l; i++) {
            chars[i] = '=';
        }

        return new String(chars);
    }

    /**
     * Decodes the given Base-32 string into a byte array. Input is case insensitive and can (but is not required to)
     * have '=' symbols at the end for padding.
     *
     * @param base32 The Base-32 string, must not be null
     * @return The decoded bytes
     */
    public static byte[] decodeBase32(String base32) {

        int len = base32.length();

        byte[] values = new byte[len];

        int paddingStart = -1;
        for (int i = 0; i < len; i++) {
            char c = base32.charAt(i);
            if (c == '=') {
                if (paddingStart < 0)
                    paddingStart = i;
            } else if (paddingStart >= 0) {
                throw new IllegalArgumentException("Invalid Base32 string");
            } else {
                if (c > 128) throw new IllegalArgumentException("Invalid Base32 string");
                int v = BASE_32_DEC[c];
                if (v < 0) throw new IllegalArgumentException("Invalid Base32 string");
                values[i] = (byte) v;
            }
        }
        if (paddingStart < 0)
            paddingStart = len;

        int padding = paddingStart & 7;
        if (padding == 1 || padding == 3 || padding == 6)
            // Invalid padding configurations
            throw new IllegalArgumentException("Invalid Base32 string");

        byte[] result = new byte[(int) ((long) len * 5L / 8L) + 5];
        long bitOff = 0;

        for (int i = 0; i < paddingStart; i++) {
            int startByte = (int) (bitOff / 8);
            int bytePos = (int) (bitOff & 7);

            int bv = (values[i] & 0x1F) << 3;

            if (bytePos < 4) {
                result[startByte] |= bv >>> bytePos;
            } else {
                int bv2 = bv << 8;
                result[startByte] |= bv >>> bytePos;
                result[startByte + 1] |= bv2 >>> bytePos & 0xFF;
            }

            bitOff += 5;
        }

        if (padding == 2) bitOff -= 2; // 01234 567__ ===== ===== ===== ===== ===== =====
        if (padding == 4) bitOff -= 4; // 01234 56701 23456 7____ ===== ===== ===== =====
        if (padding == 5) bitOff -= 1; // 01234 56701 23456 70123 4567_ ===== ===== =====
        if (padding == 7) bitOff -= 3; // 01234 56701 23456 70123 45670 12345 67___ =====
        // Otherwise                      01234 56701 23456 70123 45670 12345 67012 34567

        int blen = (int) (bitOff / 8) + ((bitOff & 7) == 0 ? 0 : 1);
        byte[] copy = new byte[blen];
        System.arraycopy(result, 0, copy, 0, blen);
        return copy;
    }
}
