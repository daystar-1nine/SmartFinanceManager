package service;

import model.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BudgetService
 * -------------
 * Handles budget logic & alerts
 */
public class BudgetService {

    // 🔥 Hardcoded budgets (later you can make dynamic)
    private Map<String, Double> categoryBudgets = new HashMap<>();

    public BudgetService() {
        categoryBudgets.put("Food", 5000.0);
        categoryBudgets.put("Transport", 3000.0);
        categoryBudgets.put("Shopping", 7000.0);
        categoryBudgets.put("Bills", 10000.0);
        categoryBudgets.put("Other", 5000.0);
    }

    /**
     * Calculate total spent per category
     */
    public Map<String, Double> calculateCategoryExpenses(List<Transaction> transactions) {

        Map<String, Double> spent = new HashMap<>();

        for (Transaction t : transactions) {

            if (t.getType().equals("Expense")) {

                String category = t.getCategory();
                double amount = t.getAmount();

                spent.put(category,
                        spent.getOrDefault(category, 0.0) + amount);
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