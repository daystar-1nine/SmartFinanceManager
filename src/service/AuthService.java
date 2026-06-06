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
 * AuthService Class
 * -----------------
 * Handles user authentication, registration, credential security, and automatic hashing upgrades.
 * Upgraded to use char[] passwords and native JVM memory cleaning.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final String USER_FILE = Constants.USER_FILE;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<String, User> userCache = new HashMap<>();

    /**
     * Hashes a password string using SHA-256 cryptosystems (Legacy).
     */
    public static String hashPassword(char[] password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = charArrayToByteArray(password);
            byte[] hash = digest.digest(bytes);
            
            // Clear temporary byte buffer immediately
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
     * Hashes a password using PBKDF2WithHmacSHA256.
     */
    public static String hashPasswordPBKDF2(char[] password, String saltHex) {
        if (password == null || saltHex == null) return null;
        try {
            byte[] salt = hexToBytes(saltHex);
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword(); // Zero out password buffer natively
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password with PBKDF2", e);
        }
    }

    /**
     * Generates a cryptographically secure random salt.
     */
    public static String generateSalt() {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return bytesToHex(salt);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    private static byte[] charArrayToByteArray(char[] chars) {
        java.nio.CharBuffer charBuffer = java.nio.CharBuffer.wrap(chars);
        java.nio.ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = java.util.Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        java.util.Arrays.fill(byteBuffer.array(), (byte) 0); // Clear backup array
        return bytes;
    }

    /**
     * Registers a new user, hashing the password before writing to file.
     */
    public boolean signup(String username, char[] password) {
        if (username == null || username.isEmpty() || password == null || password.length == 0) {
            return false;
        }

        rwLock.writeLock().lock();
        try {
            if (userExistsInternal(username)) {
                return false; // Duplicate username not allowed
            }

            // Create User object with PBKDF2 hashed password
            String salt = generateSalt();
            String hash = hashPasswordPBKDF2(password, salt);
            User user = new User(username, salt + ":" + hash, username.toLowerCase() + "@example.com", LocalDate.now().toString());

            // Save user to file
            FileUtil.writeToFile(USER_FILE, user.toFileString(), true);

            // Populate cache
            userCache.put(username.toLowerCase(), user);
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Logs in a user by checking credentials.
     * Backwards-compatible: Automatically upgrades legacy passwords to PBKDF2 hashes.
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
                        // PBKDF2 Verification
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
                        // Legacy Verification (SHA-256 or plaintext)
                        String legacyHashed = hashPassword(password);
                        if (storedPass.equals(legacyHashed) || storedPass.equals(new String(password))) {
                            authenticated = true;
                            needUpgrade = true;
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
                
                // Write out file atomically
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

            if (authenticated && targetUser != null) {
                userCache.put(username.toLowerCase(), targetUser);
            }

            return authenticated;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a User object by username (cached lookup supported)
     */
    public User getUser(String username) {
        if (username == null) return null;

        rwLock.readLock().lock();
        try {
            if (userCache.containsKey(username.toLowerCase())) {
                return userCache.get(username.toLowerCase());
            }
        } finally {
            rwLock.readLock().unlock();
        }

        rwLock.writeLock().lock();
        try {
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
     * Updates credentials and invalidates caches.
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

    public void clearCache(String username) {
        rwLock.writeLock().lock();
        try {
            userCache.remove(username.toLowerCase());
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}