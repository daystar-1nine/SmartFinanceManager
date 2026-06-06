package service;

import model.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * BudgetService Class
 * -------------------
 * Manages custom user budget configurations and spending alerts.
 * Utilizes constructor dependency injection for BudgetDAO.
 */
public class BudgetService {

    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    private final BudgetDAO budgetDAO;
    private final Map<String, Double> categoryBudgets = new HashMap<>();
    private String username;

    /**
     * Constructor for non-user-specific defaults
     */
    public BudgetService(BudgetDAO budgetDAO) {
        this.budgetDAO = budgetDAO;
        setDefaultBudgets();
    }

    /**
     * Constructor for user-specific budgets
     */
    public BudgetService(String username, BudgetDAO budgetDAO) {
        this.username = username;
        this.budgetDAO = budgetDAO;
        setDefaultBudgets();
        loadUserBudgets();
    }

    private void setDefaultBudgets() {
        categoryBudgets.put("Food", 5000.0);
        categoryBudgets.put("Transport", 3000.0);
        categoryBudgets.put("Shopping", 7000.0);
        categoryBudgets.put("Bills", 10000.0);
        categoryBudgets.put("Other", 5000.0);
    }

    private void loadUserBudgets() {
        if (username == null) return;
        Map<String, Double> userBudgets = budgetDAO.loadUserBudgets(username);
        categoryBudgets.putAll(userBudgets);
    }

    /**
     * Saves user budget limits and updates local cache.
     */
    public synchronized void saveUserBudgets(Map<String, Double> newBudgets) {
        if (username == null || newBudgets == null) {
            LOGGER.warning("Save requested with null user or budgets. Skipping.");
            return;
        }
        categoryBudgets.putAll(newBudgets);
        budgetDAO.saveUserBudgets(username, categoryBudgets);
    }

    /**
     * Returns a copy of the active category budgets.
     */
    public synchronized Map<String, Double> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }

    /**
     * Aggregates expenses by category.
     */
    public Map<String, Double> calculateCategoryExpenses(List<Transaction> transactions) {
        Map<String, Double> spent = new HashMap<>();
        if (transactions == null) return spent;

        for (Transaction t : transactions) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                String category = t.getCategory();
                double amount = t.getAmount();
                spent.put(category, spent.getOrDefault(category, 0.0) + amount);
            }
        }
        return spent;
    }

    /**
     * Computes spending warnings based on threshold checks.
     */
    public String getBudgetAlert(String category, double spent) {
        double limit = categoryBudgets.getOrDefault(category, 0.0);
        if (limit == 0) return null;

        double percent = (spent / limit) * 100;

        if (percent >= 100) {
            return "🚨 " + category + " budget exceeded!";
        } else if (percent >= 80) {
            return "⚠️ " + category + " nearing budget (" + (int) percent + "%)";
        }
        return null;
    }
}