package comp3911.cwk2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class SHAExample {

    public static String get_SHA_512_SecurePassword(String passwordToHash,
                                                     String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    // Add salt
    private static String getSalt() throws NoSuchAlgorithmException {
//        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];

        sr.nextBytes(salt);
        return Arrays.toString(salt);
    }
}
