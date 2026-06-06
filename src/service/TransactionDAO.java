package service;

import model.Transaction;
import java.util.List;

/**
 * TransactionDAO Interface
 * ------------------------
 * Decouples transaction operations from direct file access,
 * easing migration to databases in the future.
 */
public interface TransactionDAO {

    /**
     * Appends a transaction for the specified user.
     */
    void addTransaction(String username, Transaction transaction);

    /**
     * Loads all transactions for the specified user.
     */
    List<Transaction> loadTransactions(String username);

    /**
     * Saves all transactions for the specified user (overwriting current data).
     */
    void saveAllTransactions(String username, List<Transaction> transactions);
}
