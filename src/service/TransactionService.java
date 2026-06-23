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
import util.SecurityUtil;

/**
 * <h2>TransactionService</h2>
 * <p>
 * This class serves as the concrete implementation of the {@link TransactionDAO} interface.
 * It is responsible for orchestrating the retrieval, creation, deletion, and updating of
 * {@link Transaction} entities on behalf of the application's presentation layer.
 * </p>
 * 
 * <h3>Architecture Role:</h3>
 * <p>
 * Fits into the <b>Service/Data Access Object (DAO)</b> layer. It insulates the UI components
 * from flat-file parsing, directory checks, and resource locking.
 * </p>
 * 
 * <h3>Design & Performance Characteristics:</h3>
 * <ul>
 *   <li><b>Thread Safety:</b> Employs a {@link ReentrantReadWriteLock} to protect file access and cache
 *       reads/writes, allowing concurrent reads but exclusive writes across worker threads (e.g. SwingWorkers).</li>
 *   <li><b>In-Memory Caching:</b> Stores user transactions in a localized hash map to prevent constant,
 *       expensive disk I/O on dashboard refreshes.</li>
 *   <li><b>Atomic Filesystem Updates:</b> Employs double-buffered atomic writes using temporary files
 *       to prevent data corruption in the event of JVM or system crashes.</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * <pre>{@code
 *   TransactionDAO transactionService = new TransactionService();
 *   List<Transaction> userLedger = transactionService.loadTransactions("Suraj");
 * }</pre>
 * 
 * @see Transaction
 * @see TransactionDAO
 */
public class TransactionService implements TransactionDAO {

    /**
     * Logger instance for consolidating error diagnostics and warning outputs.
     */
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());

    /**
     * Base directory where flat-file user transaction records are stored.
     */
    private static final String BASE_PATH = util.Constants.TRANSACTION_DIR;

    /**
     * Read/Write lock to synchronize disk operations and prevent cache race conditions.
     */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * In-memory cache mapping lowercase usernames to their list of loaded Transactions.
     * // TODO: Implement cache eviction policy (e.g., LFU or LRU) to limit memory growth for large deployments.
     */
    private final Map<String, List<Transaction>> transactionCache = new HashMap<>();

    /**
     * Helper method to retrieve or construct the File handle for a user's transaction data.
     * Ensures parent directories exist on the local drive.
     * 
     * @param username The owner of the transaction log.
     * @return File object pointing to the user's data file.
     */
    private File getUserFile(String username) {
        File file = new File(BASE_PATH + username + "_transactions.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            // Ensure data directories are created recursively before writing
            parentDir.mkdirs();
        }
        return file;
    }

    /**
     * Appends a new financial transaction to the user's ledger file and updates the memory cache.
     * 
     * @param username The owner of the transaction ledger.
     * @param transaction The Transaction model object containing details.
     * @throws IllegalArgumentException if the transaction is null.
     */
    @Override
    public void addTransaction(String username, Transaction transaction) {
        if (transaction == null) {
            LOGGER.warning("Null transaction passed. Skipping save.");
            return;
        }

        // Acquire write lock to prevent concurrent modifications/dirty reads of the data file
        rwLock.writeLock().lock();
        try {
            List<Transaction> list = loadTransactions(username);
            list.add(transaction);
            saveAllTransactions(username, list);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Loads a user's financial transaction history. First checks the local memory cache,
     * falling back to read from the disk file if there is a cache miss.
     * 
     * @param username The owner of the transactions.
     * @return A list copy of the user's transaction ledger (never null).
     */
    @Override
    public List<Transaction> loadTransactions(String username) {
        if (username == null) return new ArrayList<>();

        // First attempt: Read lock to verify if the cache already contains the ledger
        rwLock.readLock().lock();
        try {
            if (transactionCache.containsKey(username)) {
                return new ArrayList<>(transactionCache.get(username));
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Second attempt (Cache Miss): Acquire write lock to populate cache from disk
        rwLock.writeLock().lock();
        try {
            // Double-checked locking pattern: Verify cache again to prevent redundant disk reads
            if (transactionCache.containsKey(username)) {
                return new ArrayList<>(transactionCache.get(username));
            }

            List<Transaction> transactions = new ArrayList<>();
            File file = getUserFile(username);
            if (!file.exists()) {
                // Populate empty cache if no file exists yet to prevent future disk hits
                transactionCache.put(username, transactions);
                return transactions;
            }

            // Parse CSV lines from decrypted bytes
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                byte[] decryptedBytes = SecurityUtil.decryptSafe(fileBytes);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(decryptedBytes), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Transaction transaction = Transaction.fromFileString(line);
                        if (transaction != null) {
                            transactions.add(transaction);
                        }
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

    /**
     * Overwrites the entire transaction ledger for a user atomically.
     * Uses double-buffering (.tmp file write then atomic swap) to prevent partial file corruption.
     * 
     * @param username The owner of the transaction ledger.
     * @param transactions The list of all transactions to persist.
     */
    @Override
    public void saveAllTransactions(String username, List<Transaction> transactions) {
        if (username == null || transactions == null) return;

        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            // Construct temp file name alongside primary database file
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try {
                // Write list contents into a byte stream in memory
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
                    for (Transaction transaction : transactions) {
                        if (transaction != null) {
                            writer.write(transaction.toFileString());
                            writer.newLine();
                        }
                    }
                }

                // Encrypt bytes and write to temp file
                byte[] plainBytes = baos.toByteArray();
                byte[] encryptedBytes = SecurityUtil.encrypt(plainBytes);
                Files.write(tempFile.toPath(), encryptedBytes);

                // Perform atomic OS-level file move. Prevents incomplete writes from ruining data.
                Files.move(tempFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving transactions atomically for user: " + username, e);
                // Clean up dangling temp files in case of operational failure
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            // Update in-memory cache to match saved records
            transactionCache.put(username, new ArrayList<>(transactions));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Deletes a specific transaction record by its identifier, saving updates atomically.
     * 
     * @param username The owner of the transaction record.
     * @param txId The unique transaction ID.
     */
    @Override
    public void deleteTransaction(String username, int txId) {
        // // TODO: Optimize by maintaining an indexed map within the cache to speed up target deletions
        List<Transaction> list = loadTransactions(username);
        list.removeIf(t -> t.getId() == txId);
        saveAllTransactions(username, list);
    }

    /**
     * Locates a transaction in the user's ledger and updates its details.
     * 
     * @param username The owner of the transaction ledger.
     * @param updatedTx The updated Transaction model object.
     */
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
     * Clears the in-memory cache entries for a specific user.
     * Primarily called upon logout routines to prevent memory accumulation.
     * 
     * @param username The user whose cache should be invalidated.
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