package main;

import ui.LoginFrame;
import service.*;

import javax.swing.*;

/**
 * Main Class
 * ----------
 * Entry point of the application.
 * Initializes the UI and launches the login screen.
 */
public class Main {

    public static void main(String[] args) {

        /*
         * Ensures that all Swing components are created
         * and updated on the Event Dispatch Thread (EDT).
         * This is a best practice for Swing applications.
         */
        SwingUtilities.invokeLater(() -> {

            // Initialize DAOs and Services
            AuthService authService = new AuthService();
            TransactionService transactionService = new TransactionService();
            LoanDAO loanDAO = new FileLoanDAO();
            LoanService loanService = new LoanService(loanDAO);
            BudgetDAO budgetDAO = new FileBudgetDAO();
            GoalDAO goalDAO = new FileGoalDAO();

            // Start application with Login Screen, passing in the dependencies
            new LoginFrame(authService, transactionService, loanService, budgetDAO, goalDAO);

        });
    }
}