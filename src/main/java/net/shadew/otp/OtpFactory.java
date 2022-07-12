package net.shadew.otp;

import java.util.Objects;

/**
 * A factory for {@link Otp} and {@link Totp} instances. You may set various properties in this factory before obtaining
 * an {@link Otp} or {@link Totp} instance.
 */
public final class OtpFactory {
    private CodeGenerator generator = HmacGenerator.STANDARD;
    private TimeSource timeSource = SystemTimeSource.INSTANCE;
    private int period = 30;
    private int discrepancy = -1; // equal to period
    private long counter = 0;

    /**
     * Sets the code generator that should be used for code generation. The default is {@link HmacGenerator#STANDARD}.
     *
     * @param generator The generator, must not be null
     * @return This instance for chain calls
     */
    public OtpFactory generator(CodeGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "Generator is null");
        return this;
    }

    /**
     * Sets the time source that should be used for TOTP code generation. The default is {@link
     * SystemTimeSource#INSTANCE}. Does not affect {@link Otp} instances.
     *
     * @param timeSource The time source, must not be null
     * @return This instance for chain calls
     */
    public OtpFactory timeSource(TimeSource timeSource) {
        this.timeSource = Objects.requireNonNull(timeSource, "Time source is null");
        return this;
    }

    /**
     * Sets the time period in which a certain TOTP code is valid, in seconds. The default is 30 seconds. Does not
     * affect {@link Otp} instances.
     *
     * @param period The code refresh period in seconds, must not be less than 1
     * @return This instance for chain calls
     */
    public OtpFactory period(int period) {
        if (period < 1)
            throw new IllegalArgumentException("Period must be 1 or more");
        this.period = period;
        return this;
    }

    /**
     * Sets the time discrepancy for TOTP code verification, in seconds. This is the maximum allowed time difference
     * between client and server. The default is equal to the {@link #period(int) period} (set to a negative number).
     * Does not affect {@link Otp} instances.
     *
     * @param discrepancy The time discrepancy in seconds, negative numbers make it copy the refresh period, zero
     *                    disables discrepancy
     * @return This instance for chain calls
     */
    public OtpFactory discrepancy(int discrepancy) {
        this.discrepancy = discrepancy;
        return this;
    }

    /**
     * Sets the initial counter value for OTP code verification. Does not affect {@link Totp} instances.
     *
     * @param counter The initial counter value
     * @return This instance for chain calls
     */
    public OtpFactory counter(long counter) {
        this.counter = counter;
        return this;
    }

    /**
     * Instantiates a configured {@link Otp} instance.
     *
     * @param key The key for code generation
     */
    public Otp otp(byte[] key) {
        Objects.requireNonNull(key, "Key is null");
        return new Otp(generator, key, counter);
    }

    /**
     * Instantiates a configured {@link Otp} instance.
     *
     * @param key The key for code generation, in Base-32
     * @see KeyGenerator#decodeBase32(String)
     */
    public Otp otp(String key) {
        return otp(KeyGenerator.decodeBase32(key));
    }

    /**
     * Instantiates a {@link Otp} instance using the default configuration. This is equivalent to calling {@code new
     * OtpFactory().otp(key)}.
     *
     * @param key The key for code generation
     */
    public static Otp standardOtp(byte[] key) {
        return new OtpFactory().otp(key);
    }

    /**
     * Instantiates a {@link Otp} instance using the default configuration. This is equivalent to calling {@code new
     * OtpFactory().otp(key)}.
     *
     * @param key The key for code generation, in Base-32
     * @see KeyGenerator#decodeBase32(String)
     */
    public static Otp standardOtp(String key) {
        return new OtpFactory().otp(key);
    }

    /**
     * Instantiates a configured {@link Totp} instance.
     *
     * @param key The key for code generation
     */
    public Totp totp(byte[] key) {
        Objects.requireNonNull(key, "Key is null");
        return new Totp(generator, key, timeSource, period, discrepancy < 0 ? period : discrepancy);
    }

    /**
     * Instantiates a configured {@link Totp} instance.
     *
     * @param key The key for code generation, in Base-32
     * @see KeyGenerator#decodeBase32(String)
     */
    public Totp totp(String key) {
        return totp(KeyGenerator.decodeBase32(key));
    }

    /**
     * Instantiates a {@link Totp} instance using the default configuration. This is equivalent to calling {@code new
     * OtpFactory().totp(key)}.
     *
     * @param key The key for code generation
     */
    public static Totp standardTotp(byte[] key) {
        return new OtpFactory().totp(key);
    }

    /**
     * Instantiates a {@link Totp} instance using the default configuration. This is equivalent to calling {@code new
     * OtpFactory().totp(key)}.
     *
     * @param key The key for code generation, in Base-32
     * @see KeyGenerator#decodeBase32(String)
     */
    public static Totp standardTotp(String key) {
        return new OtpFactory().totp(key);
    }
}
