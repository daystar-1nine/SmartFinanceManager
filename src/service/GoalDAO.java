package service;

import model.Goal;
import java.util.List;

/**
 * GoalDAO Interface
 * -----------------
 * Abstraction layer for savings goals data persistence.
 */
public interface GoalDAO {

    /**
     * Loads savings goals for the specified user.
     */
    List<Goal> loadGoals(String username);

    /**
     * Saves savings goals for the specified user.
     */
    void saveGoals(String username, List<Goal> goals);
}
