package net.shadew.otp;

import javax.crypto.Mac;
import java.security.NoSuchAlgorithmException;

/**
 * Hash algorithms available for {@link HmacGenerator}.
 */
public enum HashAlgorithm {
    /**
     * Use SHA-1 for hashing.
     */
    SHA1("HmacSHA1"),
    /**
     * Use SHA-256 for hashing.
     */
    SHA256("HmacSHA256"),
    /**
     * Use SHA-512 for hashing.
     */
    SHA512("HmacSHA512");

    private final String hmacAlg;

    HashAlgorithm(String hmacAlg) {
        this.hmacAlg = hmacAlg;
    }

    /**
     * Returns the HMAC algorithm name, for {@link Mac#getInstance(String)}.
     */
    public String hmacAlg() {
        return hmacAlg;
    }

    /**
     * Returns a {@link Mac} instance for the hash algorithm.
     * @throws OtpGenException If the used algorithm is not available.
     */
    public Mac mac() throws OtpGenException {
        try {
            return Mac.getInstance(hmacAlg);
        } catch (NoSuchAlgorithmException e) {
            throw new OtpGenException("Could not find algorithm " + hmacAlg, e);
        }
    }
}
