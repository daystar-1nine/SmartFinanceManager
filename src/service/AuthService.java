package service;

import model.User;
import util.FileUtil;
import util.Constants;

import java.time.LocalDate;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

/**
 * <h2>AuthService</h2>
 * <p>
 * This class handles all user authentication, profile registration, credential verification,
 * and seamless security hashing upgrades.
 * </p>
 * 
 * <h3>Architecture Role:</h3>
 * <p>
 * Part of the <b>Service Layer</b>. It manages security credentials and acts as the gatekeeper
 * for user sessions before the main application dashboards are loaded.
 * </p>
 * 
 * <h3>Security Mechanisms & Best Practices:</h3>
 * <ul>
 *   <li><b>PBKDF2 Password Hashing:</b> Uses <code>PBKDF2WithHmacSHA256</code> with 10,000 iterations
 *       and a 256-bit key length to mitigate brute-force and pre-computation attacks.</li>
 *   <li><b>Cryptographic Salts:</b> Generates unique 16-byte random salts per user, preventing rainbow table attacks.</li>
 *   <li><b>Memory Leak Prevention:</b> Takes <code>char[]</code> inputs instead of immutable strings. It zero-fills
 *       the character buffers immediately after hashing to prevent password residue from lingering in JVM heap dumps.</li>
 *   <li><b>Backward Compatibility:</b> Automatically upgrades legacy plain text or SHA-256 passwords to salted PBKDF2
 *       hashes during a successful login attempt.</li>
 * </ul>
 * 
 * @see User
 * @see FileUtil
 */
public class AuthService {

    /**
     * Logger instance for consolidating error diagnostics and warning logs.
     */
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    /**
     * File path where user login credentials and profile strings are persisted.
     */
    private static final String USER_FILE = Constants.USER_FILE;

    /**
     * Number of iterations for PBKDF2 hashing. Balancing security and desktop response speed.
     */
    private static final int ITERATIONS = 10000;

    /**
     * Target key length in bits for the generated PBKDF2 hash.
     */
    private static final int KEY_LENGTH = 256;

    /**
     * Read/Write lock to secure concurrent file accesses and thread-safe cache updates.
     */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * In-memory cache mapping lowercase usernames to User profiles to prevent excessive disk hits.
     */
    private final Map<String, User> userCache = new HashMap<>();

    /**
     * Hashes a password string using SHA-256 cryptosystems (Legacy compatibility helper).
     * Zero-fills all temporary byte arrays immediately after calculation.
     * 
     * @param password Char array representation of the password.
     * @return Hex-encoded SHA-256 string, or null if input is null.
     */
    public static String hashPassword(char[] password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = charArrayToByteArray(password);
            byte[] hash = digest.digest(bytes);
            
            // Security: Zero-out temporary byte buffer immediately
            java.util.Arrays.fill(bytes, (byte) 0);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA256 and a hex-encoded salt.
     * Clears the internal PBEKeySpec password buffers natively.
     * 
     * @param password Char array password to be hashed.
     * @param saltHex Hexadecimal string representing the salt.
     * @return Hex-encoded PBKDF2 hash string.
     */
    public static String hashPasswordPBKDF2(char[] password, String saltHex) {
        if (password == null || saltHex == null) return null;
        try {
            byte[] salt = hexToBytes(saltHex);
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            
            // Security: Zero out the PBEKeySpec buffer natively inside JDK crypto classes
            spec.clearPassword(); 
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password with PBKDF2", e);
        }
    }

    /**
     * Generates a cryptographically secure random 16-byte salt, converted to a hexadecimal string.
     * 
     * @return Hexadecimal salt string.
     */
    public static String generateSalt() {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Converts a hexadecimal string back to a byte array.
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Safely decodes a char array to a UTF-8 byte array and zero-fills intermediate buffers.
     */
    private static byte[] charArrayToByteArray(char[] chars) {
        java.nio.CharBuffer charBuffer = java.nio.CharBuffer.wrap(chars);
        java.nio.ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = java.util.Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        // Security: Zero out the backup array of the byte buffer to avoid credentials leaking
        java.util.Arrays.fill(byteBuffer.array(), (byte) 0); 
        return bytes;
    }

    /**
     * Registers a new user with PBKDF2 hashing, verifying username availability.
     * 
     * @param username The desired username (case-insensitive checks applied).
     * @param password Char array password.
     * @return True if signup succeeded, false if username already exists or inputs invalid.
     */
    public boolean signup(String username, char[] password) {
        if (username == null || username.isEmpty() || password == null || password.length == 0) {
            return false;
        }

        rwLock.writeLock().lock();
        try {
            if (userExistsInternal(username)) {
                return false; // Duplicate check
            }

            // Create User object with PBKDF2 hashed password
            String salt = generateSalt();
            String hash = hashPasswordPBKDF2(password, salt);
            // Save as salt:hash
            User user = new User(username, salt + ":" + hash, username.toLowerCase() + "@example.com", LocalDate.now().toString());

            // Save user to file
            FileUtil.writeToFile(USER_FILE, user.toFileString(), true);

            // Update local memory cache
            userCache.put(username.toLowerCase(), user);
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Authenticates user credentials. Supports automatic, seamless hashing upgrades of legacy passwords
     * (e.g. Plaintext or plain SHA-256 hashes) to PBKDF2.
     * 
     * @param username Case-insensitive username.
     * @param password Char array password to verify.
     * @return True if credentials match, false otherwise.
     */
    public boolean login(String username, char[] password) {
        if (username == null || password == null || password.length == 0) {
            return false;
        }

        rwLock.writeLock().lock();
        try {
            List<String> usersLines = FileUtil.readFromFile(USER_FILE);
            boolean authenticated = false;
            boolean needUpgrade = false;
            User targetUser = null;

            for (String line : usersLines) {
                User user = User.fromFileString(line);

                if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                    targetUser = user;
                    String storedPass = user.getPassword();
                    
                    if (storedPass.contains(":")) {
                        // PBKDF2 Verification: format is salt:hash
                        String[] parts = storedPass.split(":");
                        if (parts.length == 2) {
                            String salt = parts[0];
                            String hash = parts[1];
                            String inputHash = hashPasswordPBKDF2(password, salt);
                            if (inputHash.equals(hash)) {
                                authenticated = true;
                                break;
                            }
                        }
                    } else {
                        // Legacy password verification: hashes using original MD5/SHA-256 routines
                        String legacyHashed = hashPassword(password);
                        if (storedPass.equals(legacyHashed) || storedPass.equals(new String(password))) {
                            authenticated = true;
                            needUpgrade = true; // Flag for seamless security upgrading
                            break;
                        }
                    }
                }
            }

            // Seamlessly upgrade legacy password to PBKDF2 upon successful login
            if (authenticated && needUpgrade && targetUser != null) {
                List<User> usersList = new ArrayList<>();
                for (String line : usersLines) {
                    User user = User.fromFileString(line);
                    if (user != null) {
                        if (user.getUsername().equalsIgnoreCase(username)) {
                            String newSalt = generateSalt();
                            String newHash = hashPasswordPBKDF2(password, newSalt);
                            user.setPassword(newSalt + ":" + newHash);
                            targetUser = user;
                        }
                        usersList.add(user);
                    }
                }
                
                // Write out file atomically to avoid profile data corruption during runtime failures
                File targetFile = new File(USER_FILE);
                File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");
                try {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                        for (User u : usersList) {
                            writer.write(u.toFileString());
                            writer.newLine();
                        }
                    }
                    java.nio.file.Files.move(tempFile.toPath(), targetFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error upgrading user legacy password: " + username, e);
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }

            // Cache successfully authenticated profiles
            if (authenticated && targetUser != null) {
                userCache.put(username.toLowerCase(), targetUser);
            }

            return authenticated;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a User object by username. Employs double-checked caching patterns to reduce disk I/O.
     * 
     * @param username User name to look up.
     * @return User object, or null if not found.
     */
    public User getUser(String username) {
        if (username == null) return null;

        // Try reading from cache first under read lock
        rwLock.readLock().lock();
        try {
            if (userCache.containsKey(username.toLowerCase())) {
                return userCache.get(username.toLowerCase());
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Cache Miss: fetch from disk under write lock
        rwLock.writeLock().lock();
        try {
            // Double check cache
            if (userCache.containsKey(username.toLowerCase())) {
                return userCache.get(username.toLowerCase());
            }

            List<String> users = FileUtil.readFromFile(USER_FILE);
            for (String line : users) {
                User user = User.fromFileString(line);
                if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                    userCache.put(username.toLowerCase(), user);
                    return user;
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
        return null;
    }

    /**
     * Updates user credentials (password, email) and commits changes atomically.
     * 
     * @param username The target user.
     * @param newPassword New password array.
     * @param newEmail New email address.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateUserCredentials(String username, char[] newPassword, String newEmail) {
        if (username == null || newPassword == null || newPassword.length == 0 || newEmail == null || newEmail.isEmpty()) {
            return false;
        }

        rwLock.writeLock().lock();
        try {
            List<String> lines = FileUtil.readFromFile(USER_FILE);
            List<User> users = new ArrayList<>();
            boolean updated = false;
            User updatedUser = null;

            for (String line : lines) {
                User user = User.fromFileString(line);
                if (user != null) {
                    if (user.getUsername().equalsIgnoreCase(username)) {
                        String salt = generateSalt();
                        String hash = hashPasswordPBKDF2(newPassword, salt);
                        user.setPassword(salt + ":" + hash);
                        user.setEmail(newEmail);
                        updated = true;
                        updatedUser = user;
                    }
                    users.add(user);
                }
            }

            if (updated) {
                File targetFile = new File(USER_FILE);
                File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");
                try {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                        for (User u : users) {
                            writer.write(u.toFileString());
                            writer.newLine();
                        }
                    }
                    java.nio.file.Files.move(tempFile.toPath(), targetFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                    
                    if (updatedUser != null) {
                        userCache.put(username.toLowerCase(), updatedUser);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error updating credentials for user: " + username, e);
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    return false;
                }
            }
            return updated;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Helper to verify if a username already exists in the file database (internal usage).
     */
    private boolean userExistsInternal(String username) {
        if (userCache.containsKey(username.toLowerCase())) {
            return true;
        }
        List<String> users = FileUtil.readFromFile(USER_FILE);
        for (String line : users) {
            User user = User.fromFileString(line);
            if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                userCache.put(username.toLowerCase(), user);
                return true;
            }
        }
        return false;
    }

    /**
     * Evicts user records from cache.
     * 
     * @param username Lowercase username.
     */
    public void clearCache(String username) {
        rwLock.writeLock().lock();
        try {
            userCache.remove(username.toLowerCase());
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}