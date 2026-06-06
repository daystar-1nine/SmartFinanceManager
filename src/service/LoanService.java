package service;

import model.Loan;
import model.Payment;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanService Class
 * -----------------
 * Handles file operations for user loan data, including updating payment logs.
 */
public class LoanService {

    private static final String BASE_PATH = util.Constants.LOAN_DIR;

    /**
     * Resolves the loan data file for a user and creates parent directories if needed.
     */
    private File getUserFile(String username) {
        File file = new File(BASE_PATH + username + "_loans.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        return file;
    }

    /**
     * Appends a new loan to the user's data file.
     */
    public void addLoan(String username, Loan loan) {
        if (loan == null) {
            System.out.println("Loan object is null. Skipping save.");
            return;
        }

        File file = getUserFile(username);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(loan.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving loan: " + e.getMessage());
        }
    }

    /**
     * Loads all loans for a given username.
     */
    public List<Loan> loadLoans(String username) {
        List<Loan> loans = new ArrayList<>();
        File file = getUserFile(username);

        if (!file.exists()) {
            return loans;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Loan loan = Loan.fromFileString(line);
                if (loan != null) {
                    loans.add(loan);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading loans: " + e.getMessage());
        }

        return loans;
    }

    /**
     * Rewrites the user's data file with the updated list of loans.
     */
    public void saveAllLoans(String username, List<Loan> loans) {
        File file = getUserFile(username);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (Loan loan : loans) {
                    if (loan != null) {
                        writer.write(loan.toFileString());
                        writer.newLine();
                    }
                }
            }
            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.out.println("Error writing all loans: " + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Updates the paid amount of a specific loan, capping it at total payable and appending a Payment entry.
     */
    public void updateLoanPayment(String username, int loanId, double paymentAmount) {
        List<Loan> loans = loadLoans(username);
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                double maxPayable = loan.getTotalPayable();
                double newPaid = loan.getPaidAmount() + paymentAmount;
                
                // Cap the paid amount to avoid overpayment
                if (newPaid > maxPayable) {
                    paymentAmount = Math.max(0.0, maxPayable - loan.getPaidAmount());
                    newPaid = maxPayable;
                }
                
                loan.setPaidAmount(newPaid);
                
                // Record the payment in history if it's positive
                if (paymentAmount > 0.0) {
                    loan.getPaymentHistory().add(new Payment(paymentAmount, LocalDate.now()));
                }
                break;
            }
        }
        saveAllLoans(username, loans);
    }

    /**
     * Marks a specific loan as fully paid.
     */
    public void markAsPaid(String username, int loanId) {
        List<Loan> loans = loadLoans(username);
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                double remaining = loan.getRemainingAmount();
                if (remaining > 0.0) {
                    loan.setPaidAmount(loan.getTotalPayable());
                    loan.getPaymentHistory().add(new Payment(remaining, LocalDate.now()));
                }
                break;
            }
        }
        saveAllLoans(username, loans);
    }

    /**
     * Deletes a loan by ID.
     */
    public void deleteLoan(String username, int loanId) {
        List<Loan> loans = loadLoans(username);
        loans.removeIf(loan -> loan.getId() == loanId);
        saveAllLoans(username, loans);
    }
}
