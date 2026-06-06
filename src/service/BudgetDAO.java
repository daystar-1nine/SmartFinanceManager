package service;

import java.util.Map;

/**
 * BudgetDAO Interface
 * -------------------
 * Abstraction layer for user budget configuration persistence.
 */
public interface BudgetDAO {

    /**
     * Loads custom budget limits for the specified user.
     * If no limits are saved, returns an empty map.
     */
    Map<String, Double> loadUserBudgets(String username);

    /**
     * Saves custom budget limits for the specified user.
     */
    void saveUserBudgets(String username, Map<String, Double> budgets);
}
