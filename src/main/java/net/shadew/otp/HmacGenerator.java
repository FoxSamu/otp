package net.shadew.otp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.util.Objects;

/**
 * A <a href="https://en.wikipedia.org/wiki/HMAC">HMAC-based</a> {@link CodeGenerator}.
 */
public final class HmacGenerator implements CodeGenerator {
    /**
     * The default {@link HmacGenerator}. When using {@link OtpFactory}, this instance is used for code generation if
     * no custom {@link CodeGenerator} is configured.
     * <p>
     * It uses {@linkplain HashAlgorithm#SHA1 SHA-1} as hash algorithm, and generates 6 digits.
     */
    public static final HmacGenerator STANDARD = new HmacGenerator(HashAlgorithm.SHA1, 6);

    private final HashAlgorithm hashAlg;
    private final int digits;

    /**
     * Instantiates a {@link HmacGenerator}.
     * @param hashAlg The hash algorithm, must not be null
     * @param digits The amount of digits, at least 1 and at most 10
     */
    public HmacGenerator(HashAlgorithm hashAlg, int digits) {
        Objects.requireNonNull(hashAlg, "Hash algorithm is null");
        if (digits < 1 || digits > 10)
            throw new IllegalArgumentException("Amount of digits must be at least 1 and at most 10");

        this.hashAlg = hashAlg;
        this.digits = digits;
    }

    /**
     * Returns the hash algorithm used for this generator.
     */
    public HashAlgorithm hashAlg() {
        return hashAlg;
    }

    /**
     * Returns the amount of digits this generator generates in each code.
     */
    public int digits() {
        return digits;
    }

    /**
     * {@inheritDoc}
     *
     * @param key The key to generate with
     * @param counter The counter value (or time period, in case of TOTP)
     * @return The generated code
     * @throws OtpGenException When code generation fails (e.g. when the key is invalid)
     */
    @Override
    public char[] genCode(byte[] key, long counter) throws OtpGenException {
        // Make array storing the counter value as raw bytes
        byte[] cbytes = {
            (byte) (counter >>> 56),
            (byte) (counter >>> 48),
            (byte) (counter >>> 40),
            (byte) (counter >>> 32),
            (byte) (counter >>> 24),
            (byte) (counter >>> 16),
            (byte) (counter >>> 8),
            (byte) counter,
        };

        // Use HMAC to generate hash from counter
        Mac mac = hashAlg.mac();
        try {
            mac.init(new SecretKeySpec(key, hashAlg.hmacAlg()));
        } catch (InvalidKeyException e) {
            throw new OtpGenException("Invalid key", e);
        }
        byte[] hash = mac.doFinal(cbytes);

        // Truncate hash
        int off = hash[hash.length - 1] & 0xF; // 4 least significant bits of hash determine byte offset
        int trunc = (hash[off] & 0x7F) << 24 // 0x7F: exclude sign bit
                        | (hash[off + 1] & 0xFF) << 16
                        | (hash[off + 2] & 0xFF) << 8
                        | hash[off + 3] & 0xFF;

        // Generate digits
        int d = digits;
        char[] code = new char[d];
        while (d > 0) {
            d --;
            code[d] = (char) (trunc % 10 + '0');
            trunc /= 10;
        }

        return code;
    }
}
