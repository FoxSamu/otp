package net.shadew.otp;

import java.security.GeneralSecurityException;

/**
 * An exception thrown during OTP generation or verification.
 */
public class OtpGenException extends GeneralSecurityException {
    public OtpGenException() {
    }

    public OtpGenException(String message) {
        super(message);
    }

    public OtpGenException(String message, Throwable cause) {
        super(message, cause);
    }

    public OtpGenException(Throwable cause) {
        super(cause);
    }
}
