package service;

import model.Loan;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.SecurityUtil;

/**
 * FileLoanDAO Class
 * -----------------
 * File-based implementation of LoanDAO.
 * Integrates native thread locks and atomic temporary files.
 */
public class FileLoanDAO implements LoanDAO {

    private static final Logger LOGGER = Logger.getLogger(FileLoanDAO.class.getName());
    private static final String BASE_PATH = util.Constants.LOAN_DIR;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private File getUserFile(String username) {
        File file = new File(BASE_PATH + username + "_loans.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        return file;
    }

    @Override
    public void addLoan(String username, Loan loan) {
        if (loan == null) {
            LOGGER.warning("Null loan object passed. Skipping add.");
            return;
        }

        rwLock.writeLock().lock();
        try {
            List<Loan> loans = loadLoans(username);
            loans.add(loan);
            saveAllLoans(username, loans);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public List<Loan> loadLoans(String username) {
        List<Loan> loans = new ArrayList<>();
        rwLock.readLock().lock();
        try {
            File file = getUserFile(username);
            if (!file.exists()) {
                return loans;
            }

            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                byte[] decryptedBytes = SecurityUtil.decryptSafe(fileBytes);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(decryptedBytes), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Loan loan = Loan.fromFileString(line);
                        if (loan != null) {
                            loans.add(loan);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading loans for user: " + username, e);
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return loans;
    }

    @Override
    public void saveAllLoans(String username, List<Loan> loans) {
        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, java.nio.charset.StandardCharsets.UTF_8))) {
                    for (Loan loan : loans) {
                        if (loan != null) {
                            writer.write(loan.toFileString());
                            writer.newLine();
                        }
                    }
                }

                byte[] plainBytes = baos.toByteArray();
                byte[] encryptedBytes = SecurityUtil.encrypt(plainBytes);
                Files.write(tempFile.toPath(), encryptedBytes);

                Files.move(tempFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving all loans atomically for user: " + username, e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
