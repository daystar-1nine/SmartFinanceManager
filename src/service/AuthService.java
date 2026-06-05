package service;

import model.User;
import util.FileUtil;

import java.util.List;

/**
 * AuthService Class
 * -----------------
 * Handles all authentication-related operations such as
 * user registration (signup) and login.
 */
public class AuthService {

    // Path where user data is stored
    private static final String USER_FILE = "SmartFinanceManager/src/data/users/users.txt";

    /**
     * Registers a new user
     *
     * @param username user's username
     * @param password user's password
     * @return true if signup successful, false otherwise
     */
    public boolean signup(String username, String password) {

        // Basic validation
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return false;
        }

        // Check if user already exists
        if (userExists(username)) {
            return false; // duplicate username not allowed
        }

        // Create User object
        User user = new User(username, password);

        // Save user to file using model method
        FileUtil.writeToFile(USER_FILE, user.toFileString(), true);

        return true;
    }

    /**
     * Logs in a user by checking credentials
     *
     * @param username input username
     * @param password input password
     * @return true if credentials match, false otherwise
     */
    public boolean login(String username, String password) {

        List<String> users = FileUtil.readFromFile(USER_FILE);

        for (String line : users) {
            User user = User.fromFileString(line);

            if (user != null &&
                    user.getUsername().equals(username) &&
                    user.getPassword().equals(password)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a username already exists
     *
     * @param username username to check
     * @return true if user exists
     */
    private boolean userExists(String username) {

        List<String> users = FileUtil.readFromFile(USER_FILE);

        for (String line : users) {
            User user = User.fromFileString(line);

            if (user != null &&
                    user.getUsername().equals(username)) {

                return true;
            }
        }

        return false;
    }
}