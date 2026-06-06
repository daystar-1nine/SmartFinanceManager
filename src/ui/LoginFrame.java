package ui;

import service.AuthService;
import util.ThemeUtil;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame Class
 * ----------------
 * This class represents the login screen of the application.
 * It allows users to enter credentials and access the system.
 */
@SuppressWarnings({"serial", "this-escape"})
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {

        // Frame settings
        setTitle("Smart Finance Manager - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(ThemeUtil.getBackgroundColor());

        // Input fields
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        // Buttons
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");

        // Labels + Fields
        panel.add(new JLabel("Username"));
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ThemeUtil.getBackgroundColor());
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().setBackground(ThemeUtil.getBackgroundColor());

        // Auth service instance
        AuthService authService = new AuthService();

        /**
         * Login Button Action
         * -------------------
         * Validates user credentials and opens dashboard if successful
         */
        loginButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Input validation
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter all fields");
                return;
            }

            boolean success = authService.login(username, password);

            if (success) {

                JOptionPane.showMessageDialog(this,
                        "Login Successful");

                // Close login window
                dispose();

                // Open dashboard
                new DashboardFrame(username);

            } else {

                JOptionPane.showMessageDialog(this,
                        "Invalid Username or Password");
            }
        });

        /**
         * Signup Button Action
         * --------------------
         * Opens signup screen
         */
        signupButton.addActionListener(e -> {
            setVisible(false);
            new SignupFrame(this);
        });

        ThemeUtil.applyTheme(this);
        setVisible(true);
    }
}