package net.shadew.otp;

import java.time.Instant;
import java.util.Scanner;

//
// A basic example of TOTP generation
//
// This example asks the user to enter or generate a key, and then continuously
// prompts the user to verify or generate a code
//
public class TestTotp {
    public static void main(String[] args) throws OtpGenException {
        Scanner scanner = new Scanner(System.in);

        // First obtain a key
        System.out.println("Enter Base32 key (leave empty to generate)");
        System.out.print("> ");
        String keyln = scanner.nextLine().trim();
        byte[] key;
        if (keyln.isEmpty()) {
            // Generate key using KeyGenerator
            key = KeyGenerator.STANDARD.generate();
        } else {
            // Decode key
            key = KeyGenerator.decodeBase32(keyln);
        }
        System.out.println("Using key: " + KeyGenerator.encodeBase32(key));



        // Now obtain a Totp instance (you can alternatively instantiate OtpFactory and get an instance from that)
        Totp totp = OtpFactory.standardTotp(key);

        // Continuousy prompt the user to enter a verification code to verify, or nothing to generate a code
        while (true) {
            System.out.println("-------------------------");
            System.out.println("Enter verification code (leave empty to reveal, or type 'exit' to exit)");
            System.out.print("> ");
            String ln = scanner.nextLine().trim();

            if (ln.equals("exit")) {
                return; // Exit
            } else if (ln.isEmpty()) {
                System.out.println("Code is: " + totp.code()); // <- Generate code
                System.out.println("Time is: " + time());
            } else if (totp.verify(ln)) { // <- Verify code
                System.out.println("Verified!");
                System.out.println("Time is: " + time());
            } else {
                System.out.println("Not verified!");
                System.out.println("Time is: " + time());
            }
        }
    }

    // Prints seconds since epoch, time used for generation
    private static String time() {
        return Instant.now().getEpochSecond() + " seconds since Epoch";
    }
}
