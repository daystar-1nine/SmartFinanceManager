package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileBudgetDAO Class
 * -------------------
 * File-based implementation of BudgetDAO.
 * Integrates native thread locks and atomic temporary files.
 */
public class FileBudgetDAO implements BudgetDAO {

    private static final Logger LOGGER = Logger.getLogger(FileBudgetDAO.class.getName());
    private static final String BUDGET_DIR = util.Constants.BUDGET_DIR;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private File getUserFile(String username) {
        File file = new File(BUDGET_DIR + username + "_budgets.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        return file;
    }

    @Override
    public Map<String, Double> loadUserBudgets(String username) {
        Map<String, Double> budgets = new HashMap<>();
        if (username == null) return budgets;

        rwLock.readLock().lock();
        try {
            File file = getUserFile(username);
            if (!file.exists()) {
                return budgets;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            String category = parts[0].trim();
                            double limit = Double.parseDouble(parts[1].trim());
                            budgets.put(category, limit);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading budgets for user: " + username, e);
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return budgets;
    }

    @Override
    public void saveUserBudgets(String username, Map<String, Double> budgets) {
        if (username == null || budgets == null) return;

        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    for (Map.Entry<String, Double> entry : budgets.entrySet()) {
                        writer.write(entry.getKey() + "," + entry.getValue());
                        writer.newLine();
                    }
                }
                Files.move(tempFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error saving budgets for user: " + username, e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
