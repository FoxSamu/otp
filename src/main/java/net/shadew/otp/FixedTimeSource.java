package net.shadew.otp;

/**
 * A {@link TimeSource} for providing a fixed time. Primarily useful for tracing back the code at a specific time.
 * <strong>DO NOT USE IN PRODUCTION TO PERFORM AUTHENTICATION, IT IS NOT SECURE!!!</strong>
 */
public final class FixedTimeSource implements TimeSource {
    private final long secondsSinceEpoch;

    /**
     * Instantiate a {@link FixedTimeSource}
     * @param secondsSinceEpoch The specific time stamp to be returned by {@link #time()}.
     */
    public FixedTimeSource(long secondsSinceEpoch) {
        this.secondsSinceEpoch = secondsSinceEpoch;
    }

    /**
     * Returns a fixed time, as set in {@linkplain #FixedTimeSource the constructor}.
     */
    @Override
    public long time() {
        return secondsSinceEpoch;
    }
}
