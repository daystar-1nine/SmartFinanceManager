package service;

import model.User;
import util.FileUtil;
import util.Constants;

import java.time.LocalDate;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * AuthService Class
 * -----------------
 * Handles user authentication, registration, credential security, and automatic hashing upgrades.
 */
public class AuthService {

    private static final String USER_FILE = Constants.USER_FILE;

    /**
     * Hashes a password string using SHA-256 cryptosystems (Legacy).
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
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

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    /**
     * Hashes a password using PBKDF2WithHmacSHA256.
     */
    public static String hashPasswordPBKDF2(String password, String saltHex) {
        if (password == null || saltHex == null) return null;
        try {
            byte[] salt = hexToBytes(saltHex);
            char[] chars = password.toCharArray();
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
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

    /**
     * Registers a new user, hashing the password before writing to file.
     */
    public boolean signup(String username, String password) {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return false;
        }

        if (userExists(username)) {
            return false; // Duplicate username not allowed
        }

        // Create User object with PBKDF2 hashed password
        String salt = generateSalt();
        String hash = hashPasswordPBKDF2(password, salt);
        User user = new User(username, salt + ":" + hash, username.toLowerCase() + "@example.com", LocalDate.now().toString());

        // Save user to file
        FileUtil.writeToFile(USER_FILE, user.toFileString(), true);

        return true;
    }

    /**
     * Logs in a user by checking credentials.
     * Backwards-compatible: Automatically upgrades legacy passwords to PBKDF2 hashes.
     */
    public boolean login(String username, String password) {
        List<String> usersLines = FileUtil.readFromFile(USER_FILE);
        boolean authenticated = false;
        boolean needUpgrade = false;

        for (String line : usersLines) {
            User user = User.fromFileString(line);

            if (user != null && user.getUsername().equalsIgnoreCase(username)) {
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
                    if (storedPass.equals(legacyHashed) || storedPass.equals(password)) {
                        authenticated = true;
                        needUpgrade = true;
                        break;
                    }
                }
            }
        }

        // Seamlessly upgrade legacy password to PBKDF2 upon successful login
        if (authenticated && needUpgrade) {
            List<User> usersList = new ArrayList<>();
            for (String line : usersLines) {
                User user = User.fromFileString(line);
                if (user != null) {
                    if (user.getUsername().equalsIgnoreCase(username)) {
                        String newSalt = generateSalt();
                        String newHash = hashPasswordPBKDF2(password, newSalt);
                        user.setPassword(newSalt + ":" + newHash);
                    }
                    usersList.add(user);
                }
            }
            // Overwrite file with upgraded records atomically
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
                System.out.println("Error upgrading user password: " + e.getMessage());
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        return authenticated;
    }

    /**
     * Retrieves a User object by username
     */
    public User getUser(String username) {
        List<String> users = FileUtil.readFromFile(USER_FILE);

        for (String line : users) {
            User user = User.fromFileString(line);
            if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Updates password and email for a user, hashing the new password.
     */
    public boolean updateUserCredentials(String username, String newPassword, String newEmail) {
        if (newPassword == null || newPassword.isEmpty() || newEmail == null || newEmail.isEmpty()) {
            return false;
        }

        List<String> lines = FileUtil.readFromFile(USER_FILE);
        List<User> users = new ArrayList<>();
        boolean updated = false;

        for (String line : lines) {
            User user = User.fromFileString(line);
            if (user != null) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    String salt = generateSalt();
                    String hash = hashPasswordPBKDF2(newPassword, salt);
                    user.setPassword(salt + ":" + hash);
                    user.setEmail(newEmail);
                    updated = true;
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
            } catch (IOException e) {
                System.out.println("Error updating user credentials: " + e.getMessage());
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                return false;
            }
        }

        return updated;
    }

    /**
     * Checks whether a username already exists
     */
    private boolean userExists(String username) {
        List<String> users = FileUtil.readFromFile(USER_FILE);

        for (String line : users) {
            User user = User.fromFileString(line);

            if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}