package service;

import model.Goal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * GoalService Class
 * -----------------
 * Manages savings goal logic and cached states.
 * Utilizes constructor dependency injection for GoalDAO.
 */
public class GoalService {

    private static final Logger LOGGER = Logger.getLogger(GoalService.class.getName());
    private final GoalDAO goalDAO;
    private final Map<String, List<Goal>> goalCache = new HashMap<>();

    /**
     * Dependency Injected Constructor
     */
    public GoalService(GoalDAO goalDAO) {
        this.goalDAO = goalDAO;
    }

    /**
     * Loads user savings goals using local caching.
     */
    public synchronized List<Goal> loadGoals(String username) {
        if (username == null) return new ArrayList<>();
        if (goalCache.containsKey(username)) {
            return new ArrayList<>(goalCache.get(username));
        }
        List<Goal> goals = goalDAO.loadGoals(username);
        goalCache.put(username, goals);
        return new ArrayList<>(goals);
    }

    /**
     * Saves user savings goals and updates cache.
     */
    public synchronized void saveGoals(String username, List<Goal> goals) {
        if (username == null || goals == null) {
            LOGGER.warning("Save requested with null user or goals. Skipping.");
            return;
        }
        goalDAO.saveGoals(username, goals);
        goalCache.put(username, new ArrayList<>(goals));
    }

    /**
     * Clears local cache for a user.
     */
    public synchronized void clearCache(String username) {
        goalCache.remove(username);
    }
}
