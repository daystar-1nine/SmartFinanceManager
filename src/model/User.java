package model;

/**
 * User Model Class
 * ----------------
 * This class represents a user in the system.
 * It stores user credentials and basic profile information.
 *
 * This class follows encapsulation (data hiding) principles.
 */
public class User {

    // Username of the user (unique identifier)
    private String username;

    // Password of the user (stored as plain text for now - can be improved later)
    private String password;

    /**
     * Constructor to initialize user object
     *
     * @param username the username entered by user
     * @param password the password entered by user
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Getter method to retrieve username
     *
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter method to retrieve password
     *
     * NOTE: In real-world applications, passwords should never be exposed like this.
     * This is kept simple for learning purposes.
     *
     * @return password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Converts user object into file-friendly format
     * Example: suraj,1234
     *
     * @return formatted string for file storage
     */
    public String toFileString() {
        return username + "," + password;
    }

    /**
     * Static method to create User object from file data
     *
     * @param line a line from file (username,password)
     * @return User object
     */
    public static User fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
            return new User(parts[0], parts[1]);
        }
        return null;
    }
}