package service;

import model.Goal;
import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalService
 * -----------
 * Manages loading and saving user savings goals.
 */
public class GoalService {

    private static final String GOAL_DIR = util.Constants.GOAL_DIR;

    /**
     * Loads savings goals for a specific user
     */
    public List<Goal> loadGoals(String username) {
        List<Goal> goals = new ArrayList<>();
        File file = new File(GOAL_DIR + username + "_goals.txt");
        if (!file.exists()) {
            return goals;
        }

        List<String> lines = FileUtil.readFromFile(file.getPath());
        for (String line : lines) {
            Goal goal = Goal.fromFileString(line);
            if (goal != null) {
                goals.add(goal);
            }
        }
        return goals;
    }

    /**
     * Saves savings goals for a specific user
     */
    public void saveGoals(String username, List<Goal> goals) {
        File dir = new File(GOAL_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String path = GOAL_DIR + username + "_goals.txt";
        File file = new File(path);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try {
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(tempFile))) {
                for (Goal goal : goals) {
                    if (goal != null) {
                        writer.write(goal.toFileString());
                        writer.newLine();
                    }
                }
            }
            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.io.IOException e) {
            System.out.println("Error saving goals: " + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
