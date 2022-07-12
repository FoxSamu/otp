package net.shadew.otp;

/**
 * A source of time for TOTP verification. The default is {@link SystemTimeSource}, but {@link NtpTimeSource} and {@link
 * FixedTimeSource} are available as alternatives. You may as well implement this interface yourself.
 */
public interface TimeSource {
    /**
     * Provides the time for TOTP generation. This time is in seconds since the midnight of January 1st, 1970.
     *
     * @throws OtpGenException If providing time fails
     */
    long time() throws OtpGenException;
}
