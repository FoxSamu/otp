# One-Time Password (OTP) generator and verifier for Java
This simple library can generate One-Time Passwords (OTPs) for 2-factor authentication systems, either time based or counter based.

## Usage
To create a verifier, you'll need a secret key. A key can be obtained from a `KeyGenerator` and will be either a `byte[]` with the raw key or a Base-32 encoded string of the key.

```java
OtpFactory factory = new OtpFactory();
// You can set various properties in the OtpFactory instance

Totp totp = factory.totp(yourSecretKey);

try {
    // To generate a code
    String code = totp.code();
    
    // To verify a code
    boolean verified = totp.verify(code);
} catch (OtpGenException exc) {
    // This checked exception wraps exceptions occurring during code generation
    exc.printStackTrace();
}
```

## Installation
You can install the library from my maven: https://maven.shadew.net/.

```gradle
repositories {
    maven { url "https://maven.shadew.net/" }
}

dependencies {
    implementation "net.shadew:otp:0.1"
}
```
