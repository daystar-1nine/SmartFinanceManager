package util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * <h2>SecurityUtil</h2>
 * <p>
 * This class provides cryptographic utilities to manage the active user session key
 * and secure local file storage using AES-GCM-256 encryption.
 * </p>
 */
public class SecurityUtil {

    private static byte[] sessionKey = null;
    private static final int GCM_IV_LENGTH = 12; // Standard 12 bytes IV for GCM
    private static final int GCM_TAG_LENGTH = 128; // Standard 128-bit authentication tag

    /**
     * Stores the active session key derived from user password.
     * Overwrites any existing key copy securely.
     *
     * @param key Symmetric key bytes.
     */
    public static synchronized void setSessionKey(byte[] key) {
        clearSessionKey();
        if (key != null) {
            sessionKey = Arrays.copyOf(key, key.length);
        }
    }

    /**
     * Retrieves the active session key.
     *
     * @return Symmetric key bytes, or null if unauthenticated.
     */
    public static synchronized byte[] getSessionKey() {
        return sessionKey;
    }

    /**
     * Zero-fills and clears the active session key in memory to prevent credential recovery.
     */
    public static synchronized void clearSessionKey() {
        if (sessionKey != null) {
            Arrays.fill(sessionKey, (byte) 0);
            sessionKey = null;
        }
    }

    /**
     * Encrypts plaintext using AES-GCM-256 with the active session key.
     * If no session key is active (e.g. initial setup), returns the data unencrypted.
     *
     * @param plaintext Raw bytes to encrypt.
     * @return Concatenated IV + Ciphertext bytes.
     * @throws Exception if encryption fails.
     */
    public static byte[] encrypt(byte[] plaintext) throws Exception {
        byte[] activeKey = getSessionKey();
        if (activeKey == null) {
            return plaintext;
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(activeKey, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prep output array for concatenated IV + ciphertext
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);

        return encrypted;
    }

    /**
     * Decrypts ciphertext using AES-GCM-256 and the active session key.
     * If no session key is active, returns the data as-is.
     *
     * @param encrypted Concatenated IV + Ciphertext bytes.
     * @return Decrypted plaintext bytes.
     * @throws Exception if decryption fails.
     */
    public static byte[] decrypt(byte[] encrypted) throws Exception {
        byte[] activeKey = getSessionKey();
        if (activeKey == null) {
            return encrypted;
        }

        if (encrypted.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted payload size");
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);

        byte[] ciphertext = new byte[encrypted.length - iv.length];
        System.arraycopy(encrypted, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(activeKey, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

        return cipher.doFinal(ciphertext);
    }

    /**
     * Attempts to decrypt the ciphertext. If decryption fails (e.g. file is legacy plain-text CSV),
     * returns the original input bytes as a fallback.
     *
     * @param encrypted The bytes from the file.
     * @return Decrypted bytes or the fallback original bytes.
     */
    public static byte[] decryptSafe(byte[] encrypted) {
        if (getSessionKey() == null) {
            return encrypted;
        }
        try {
            return decrypt(encrypted);
        } catch (Exception e) {
            // Safe fallback for legacy plain-text files
            return encrypted;
        }
    }
}
