package main;

import ui.LoginFrame;

import javax.swing.*;

/**
 * Main Class
 * ----------
 * Entry point of the application.
 * Initializes the UI and launches the login screen.
 */
public class Main {

    public static void main(String[] args) {

        /**
         * Ensures that all Swing components are created
         * and updated on the Event Dispatch Thread (EDT).
         * This is a best practice for Swing applications.
         */
        SwingUtilities.invokeLater(() -> {

            // Start application with Login Screen
            new LoginFrame();

        });
    }
}