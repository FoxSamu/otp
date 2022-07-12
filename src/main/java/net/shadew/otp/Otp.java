package net.shadew.otp;

import java.util.Objects;

/**
 * A counter-based one time password generator (OTP, or HOTP when it uses HMAC). An instance can be obtained through
 * {@link OtpFactory}.
 */
public final class Otp {
    private final CodeGenerator generator;
    private final byte[] key;
    private long counter;

    Otp(CodeGenerator generator, byte[] key, long counter) {
        this.generator = Objects.requireNonNull(generator, "Generator is null");
        this.key = Objects.requireNonNull(key, "Key is null");
        this.counter = counter;
    }

    /**
     * Increments the OTP counter.
     */
    public void increment() {
        counter++;
    }

    /**
     * Synchronizes the OTP counter by setting it to the new counter value.
     * @param counter The new counter value
     */
    public void sync(long counter) {
        this.counter = counter;
    }

    /**
     * Returns the current counter value.
     */
    public long counter() {
        return counter;
    }

    /**
     * Generates a one-time password code. This does not modify the counter value, so the same code is generated
     * as long as neither {@link #increment} nor {@link #sync} is called.
     * @return The generated code
     * @throws OtpGenException If code generation fails
     */
    public String code() throws OtpGenException {
        char[] code = generator.genCode(key, counter);
        return new String(code);
    }

    /**
     * Verifies the given authentication code. Important is that the counter value of this {@link Otp} instance is
     * synchronized with the client's counter. This does not modify the counter value, so the one same code is valid
     * as long neither {@link #increment} nor {@link #sync} is called.
     * @param auth The authentication code to check
     * @return True if the authentication code is not null and valid, false otherwise
     * @throws OtpGenException If code verification fails
     */
    public boolean verify(String auth) throws OtpGenException {
        if (auth == null) return false;

        char[] code = generator.genCode(key, counter);

        int l = code.length;
        if (auth.length() != l)
            return false;

        // Check every character always, so that the amount of time needed for this check is always more or less the
        // same. This way nobody can guess the code by noticing the verification check takes longer.
        int res = 0;
        for (int i = 0; i < l; i++) {
            res |= code[i] ^ auth.charAt(i);
        }
        return res == 0;
    }
}
