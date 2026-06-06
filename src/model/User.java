package model;

import java.time.LocalDate;

/**
 * User Model Class
 * ----------------
 * Represents a user in the system, storing credentials, email, and registration date.
 */
public class User {

    private String username;
    private String password;
    private String email;
    private String registrationDate;

    public User(String username, String password, String email, String registrationDate) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Converts user object into file-friendly format
     * Example: suraj,1234,suraj@example.com,2026-06-06
     */
    public String toFileString() {
        return username + "," + password + "," + email + "," + registrationDate;
    }

    /**
     * Creates a User object from file data.
     * Backwards-compatible: Handles lines with only username and password.
     */
    public static User fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",");
        if (parts.length >= 2) {
            String username = parts[0].trim();
            String password = parts[1].trim();
            String email = parts.length >= 3 ? parts[2].trim() : (username.toLowerCase() + "@example.com");
            String regDate = parts.length >= 4 ? parts[3].trim() : LocalDate.now().toString();
            return new User(username, password, email, regDate);
        }
        return null;
    }
}