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
     * Hashes a password string using SHA-256 cryptosystems.
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

        // Create User object with hashed password
        User user = new User(username, hashPassword(password), username.toLowerCase() + "@example.com", LocalDate.now().toString());

        // Save user to file
        FileUtil.writeToFile(USER_FILE, user.toFileString(), true);

        return true;
    }

    /**
     * Logs in a user by checking credentials.
     * Backwards-compatible: Automatically upgrades legacy plaintext passwords to SHA-256 hashes.
     */
    public boolean login(String username, String password) {
        List<String> usersLines = FileUtil.readFromFile(USER_FILE);
        String hashedInput = hashPassword(password);
        boolean authenticated = false;
        boolean needUpgrade = false;

        for (String line : usersLines) {
            User user = User.fromFileString(line);

            if (user != null && user.getUsername().equalsIgnoreCase(username)) {
                if (user.getPassword().equals(hashedInput)) {
                    authenticated = true;
                    break;
                } else if (user.getPassword().equals(password)) {
                    // Authenticated via legacy plaintext match; flag for password upgrade
                    authenticated = true;
                    needUpgrade = true;
                    break;
                }
            }
        }

        // Seamlessly hash legacy plaintext password upon successful login
        if (authenticated && needUpgrade) {
            List<User> usersList = new ArrayList<>();
            for (String line : usersLines) {
                User user = User.fromFileString(line);
                if (user != null) {
                    if (user.getUsername().equalsIgnoreCase(username)) {
                        user.setPassword(hashedInput);
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
                    user.setPassword(hashPassword(newPassword));
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