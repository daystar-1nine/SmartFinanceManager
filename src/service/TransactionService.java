package service;

import model.Transaction;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TransactionService Class
 * ------------------------
 * Manages user financial transactions.
 * Incorporates ReentrantReadWriteLock thread synchronization and local in-memory caching.
 */
public class TransactionService implements TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());
    private static final String BASE_PATH = util.Constants.TRANSACTION_DIR;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<String, List<Transaction>> transactionCache = new HashMap<>();

    private File getUserFile(String username) {
        File file = new File(BASE_PATH + username + "_transactions.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        return file;
    }

    @Override
    public void addTransaction(String username, Transaction transaction) {
        if (transaction == null) {
            LOGGER.warning("Null transaction passed. Skipping save.");
            return;
        }

        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(transaction.toFileString());
                writer.newLine();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error appending transaction for user: " + username, e);
            }
            // Update cache
            if (transactionCache.containsKey(username)) {
                transactionCache.get(username).add(transaction);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public List<Transaction> loadTransactions(String username) {
        if (username == null) return new ArrayList<>();

        // First attempt cache read
        rwLock.readLock().lock();
        try {
            if (transactionCache.containsKey(username)) {
                return new ArrayList<>(transactionCache.get(username));
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Cache miss: load from disk with write lock protection
        rwLock.writeLock().lock();
        try {
            // Double check cache
            if (transactionCache.containsKey(username)) {
                return new ArrayList<>(transactionCache.get(username));
            }

            List<Transaction> transactions = new ArrayList<>();
            File file = getUserFile(username);
            if (!file.exists()) {
                transactionCache.put(username, transactions);
                return transactions;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Transaction transaction = Transaction.fromFileString(line);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading transaction file for user: " + username, e);
            }
            transactionCache.put(username, transactions);
            return new ArrayList<>(transactions);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void saveAllTransactions(String username, List<Transaction> transactions) {
        if (username == null || transactions == null) return;

        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    for (Transaction transaction : transactions) {
                        if (transaction != null) {
                            writer.write(transaction.toFileString());
                            writer.newLine();
                        }
                    }
                }
                Files.move(tempFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error saving transactions atomically for user: " + username, e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            // Update cache
            transactionCache.put(username, new ArrayList<>(transactions));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteTransaction(String username, int txId) {
        List<Transaction> list = loadTransactions(username);
        list.removeIf(t -> t.getId() == txId);
        saveAllTransactions(username, list);
    }

    @Override
    public void updateTransaction(String username, Transaction updatedTx) {
        List<Transaction> list = loadTransactions(username);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == updatedTx.getId()) {
                list.set(i, updatedTx);
                break;
            }
        }
        saveAllTransactions(username, list);
    }

    /**
     * Clears in-memory cache for the user.
     */
    public void clearCache(String username) {
        rwLock.writeLock().lock();
        try {
            transactionCache.remove(username);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}