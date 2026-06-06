package service;

import model.Transaction;
import util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BudgetService
 * -------------
 * Handles budget logic, alerts, and saving/loading user-specific budget limits.
 */
public class BudgetService {

    private static final String BUDGET_DIR = "SmartFinanceManager/src/data/budgets/";
    private final Map<String, Double> categoryBudgets = new HashMap<>();
    private String username;

    /**
     * Default constructor (uses default budget limits)
     */
    public BudgetService() {
        setDefaultBudgets();
    }

    /**
     * User-specific constructor (loads budgets from file, falls back to defaults)
     */
    public BudgetService(String username) {
        this.username = username;
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
        File file = new File(BUDGET_DIR + username + "_budgets.txt");
        if (!file.exists()) {
            return;
        }

        List<String> lines = FileUtil.readFromFile(file.getPath());
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                try {
                    String category = parts[0].trim();
                    double limit = Double.parseDouble(parts[1].trim());
                    categoryBudgets.put(category, limit);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /**
     * Saves custom budget limits for the current user
     */
    public void saveUserBudgets(Map<String, Double> newBudgets) {
        if (username == null) return;
        
        File dir = new File(BUDGET_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String path = BUDGET_DIR + username + "_budgets.txt";
        FileUtil.clearFile(path);

        categoryBudgets.putAll(newBudgets);

        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            FileUtil.writeToFile(path, entry.getKey() + "," + entry.getValue(), true);
        }
    }

    public Map<String, Double> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }

    /**
     * Calculate total spent per category
     */
    public Map<String, Double> calculateCategoryExpenses(List<Transaction> transactions) {
        Map<String, Double> spent = new HashMap<>();

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
     * Generate budget alerts
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