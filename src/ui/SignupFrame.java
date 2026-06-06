package ui;

import service.AuthService;
import util.ThemeUtil;

import javax.swing.*;
import java.awt.*;

/**
 * SignupFrame Class
 * -----------------
 * This class represents the user registration screen.
 * It allows new users to create an account.
 */
public class SignupFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JFrame parent; // Keep track of parent to restore it later

    public SignupFrame(JFrame parent) {
        this.parent = parent;

        setTitle("Smart Finance Manager - Signup");
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Restore parent LoginFrame if this signup window is closed
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (parent != null) {
                    parent.setVisible(true);
                }
            }
        });

        // Main panel with spacing
        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(ThemeUtil.getBackgroundColor());
        getContentPane().setBackground(ThemeUtil.getBackgroundColor());

        // Input fields
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        JButton createButton = new JButton("Create Account");

        // UI Components
        panel.add(new JLabel("Username"));
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password"));
        panel.add(confirmPasswordField);

        panel.add(createButton);

        add(panel);

        AuthService authService = new AuthService();

        /**
         * Create Account Button Action
         * ----------------------------
         * Validates input and registers user
         */
        createButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validation checks
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill all fields");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match");
                return;
            }

            if (password.length() < 4) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 4 characters");
                return;
            }

            boolean created = authService.signup(username, password);

            if (created) {

                JOptionPane.showMessageDialog(this,
                        "Account Created Successfully");

                dispose();

                // Redirect back to login by restoring parent
                if (parent != null) {
                    parent.setVisible(true);
                } else {
                    new LoginFrame();
                }

            } else {

                JOptionPane.showMessageDialog(this,
                        "Username already exists or signup failed");
            }

        });

        ThemeUtil.applyTheme(this);
        setVisible(true);
    }
}