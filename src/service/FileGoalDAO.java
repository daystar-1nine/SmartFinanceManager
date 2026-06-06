package service;

import model.Goal;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileGoalDAO Class
 * ----------------
 * File-based implementation of GoalDAO.
 * Integrates native thread locks and atomic temporary files.
 */
public class FileGoalDAO implements GoalDAO {

    private static final Logger LOGGER = Logger.getLogger(FileGoalDAO.class.getName());
    private static final String GOAL_DIR = util.Constants.GOAL_DIR;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private File getUserFile(String username) {
        File file = new File(GOAL_DIR + username + "_goals.txt");
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        return file;
    }

    @Override
    public List<Goal> loadGoals(String username) {
        List<Goal> goals = new ArrayList<>();
        if (username == null) return goals;

        rwLock.readLock().lock();
        try {
            File file = getUserFile(username);
            if (!file.exists()) {
                return goals;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Goal goal = Goal.fromFileString(line);
                    if (goal != null) {
                        goals.add(goal);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading goals for user: " + username, e);
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return goals;
    }

    @Override
    public void saveGoals(String username, List<Goal> goals) {
        if (username == null || goals == null) return;

        rwLock.writeLock().lock();
        try {
            File file = getUserFile(username);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    for (Goal goal : goals) {
                        if (goal != null) {
                            writer.write(goal.toFileString());
                            writer.newLine();
                        }
                    }
                }
                Files.move(tempFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error saving goals for user: " + username, e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
