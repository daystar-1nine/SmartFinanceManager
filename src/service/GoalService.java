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

    private static final String GOAL_DIR = "SmartFinanceManager/src/data/goals/";

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
        FileUtil.clearFile(path);

        for (Goal goal : goals) {
            if (goal != null) {
                FileUtil.writeToFile(path, goal.toFileString(), true);
            }
        }
    }
}
