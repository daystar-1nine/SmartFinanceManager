package service;

import model.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionService Class
 * ------------------------
 * Handles all transaction-related file operations.
 *
 * Responsibilities:
 * - Add transaction
 * - Load transactions
 * - Save all transactions (overwrite)
 */
public class TransactionService {

    // ================= CONSTANT PATH =================

    /**
     * Base directory for storing transaction files
     */
    private static final String BASE_PATH =
            "SmartFinanceManager/src/data/transactions/";

    /**
     * Returns file object for a user
     */
    private File getUserFile(String username) {

        File file = new File(BASE_PATH + username + "_transactions.txt");

        // Ensure directory exists
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        return file;
    }

    // ================= ADD TRANSACTION =================

    /**
     * Appends a new transaction to file
     */
    public void addTransaction(String username, Transaction transaction) {

        if (transaction == null) {
            System.out.println("Transaction is null. Skipping save.");
            return;
        }

        File file = getUserFile(username);

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, true))) {

            writer.write(transaction.toFileString());
            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    // ================= LOAD TRANSACTIONS =================

    /**
     * Reads all transactions from file
     */
    public List<Transaction> loadTransactions(String username) {

        List<Transaction> transactions = new ArrayList<>();

        File file = getUserFile(username);

        // If file does not exist → return empty list
        if (!file.exists()) {
            return transactions;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                Transaction transaction =
                        Transaction.fromFileString(line);

                // Avoid invalid or null transactions
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }

        return transactions;
    }

    // ================= SAVE ALL (IMPORTANT FOR DELETE/EDIT) =================

    /**
     * Rewrites entire file with updated transaction list
     *
     * Used for:
     * - Delete operation
     * - Edit operation
     */
    public void saveAllTransactions(String username,
                                    List<Transaction> transactions) {

        File file = getUserFile(username);

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file))) {

            for (Transaction transaction : transactions) {

                if (transaction != null) {
                    writer.write(transaction.toFileString());
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("Error saving all transactions: " + e.getMessage());
        }
    }
}