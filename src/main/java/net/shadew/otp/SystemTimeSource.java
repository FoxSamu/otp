package net.shadew.otp;

import java.time.Instant;

/**
 * The default {@link TimeSource}, providing the system's local time. The returned time is {@link
 * Instant#getEpochSecond()} from {@link Instant#now()}.
 */
public enum SystemTimeSource implements TimeSource {
    INSTANCE;

    /**
     * {@inheritDoc}
     */
    @Override
    public long time() {
        return Instant.now().getEpochSecond();
    }
}
