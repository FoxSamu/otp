package net.shadew.otp;

/**
 * Algorithm for generating a code. The default is {@link HmacGenerator}. A generator can be configured in {@link
 * OtpFactory}.
 */
public interface CodeGenerator {
    /**
     * Generates a code
     * @param key The key to generate with
     * @param counter The counter value (or time period, in case of TOTP)
     * @return The generated code in a {@code char[]}
     * @throws OtpGenException When code generation fails
     */
    char[] genCode(byte[] key, long counter) throws OtpGenException;
}
