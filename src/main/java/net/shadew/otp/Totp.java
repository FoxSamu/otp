package net.shadew.otp;

import java.util.Objects;

/**
 * A time-based one time password generator (TOTP). An instance can be obtained through {@link OtpFactory}.
 */
public final class Totp {
    private final CodeGenerator generator;
    private final byte[] key;
    private final TimeSource time;
    private final int timePeriod;
    private final int discrepancy;

    Totp(CodeGenerator generator, byte[] key, TimeSource time, int timePeriod, int discrepancy) {
        this.generator = Objects.requireNonNull(generator, "Generator is null");
        this.key = Objects.requireNonNull(key, "Key is null");
        this.time = Objects.requireNonNull(time, "Time is null");

        if (timePeriod < 1)
            throw new IllegalArgumentException("Period must be 1 or more");
        this.timePeriod = timePeriod;

        if (discrepancy < 0)
            throw new IllegalArgumentException("Discrepancy must be 0 or more");
        this.discrepancy = discrepancy;
    }

    /**
     * Generates a one-time password code for the current time as provided by the internal {@link TimeSource}.
     *
     * @return The generated code
     *
     * @throws OtpGenException If code generation fails
     */
    public String code() throws OtpGenException {
        long t = time.time();

        long period = Math.floorDiv(t, timePeriod);
        char[] code = generator.genCode(key, period);
        return new String(code);
    }

    /**
     * Verifies the given authentication code. Important is that the time at which the provided code was generated must
     * be in the same time period as the time provided by the internal {@link TimeSource}, with some allowed discrepancy
     * (usually the discrepancy is the same as one time period length).
     *
     * @param auth The authentication code to check
     * @return True if the authentication code is not null and valid, false otherwise
     *
     * @throws OtpGenException If code verification fails
     * @see OtpFactory#discrepancy(int)
     */
    public boolean verify(String auth) throws OtpGenException {
        if (auth == null) return false;

        long t = time.time();

        long firstPeriod = Math.floorDiv(t - discrepancy, timePeriod);
        long lastPeriod = -Math.floorDiv(-(t + discrepancy), timePeriod); // Apply negation so it actually becomes ceilDiv

        // Check every covered time period always, so that the amount of time needed for this check is always more or
        // less the same. This way nobody can guess the code by noticing the verification check takes longer.
        int authL = auth.length();
        boolean success = false;
        for (long period = firstPeriod; period <= lastPeriod; period++) {
            char[] code = generator.genCode(key, period);

            int l = code.length;
            if (authL == l) {
                int res = 0;
                for (int i = 0; i < l; i++) {
                    res |= code[i] ^ auth.charAt(i);
                }
                success |= res == 0;
            }
        }

        return success;
    }
}
