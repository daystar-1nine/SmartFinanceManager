package service;

import model.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InsightService
 * --------------
 * Generates smart spending insights (rule-based)
 */
public class InsightService {

    /**
     * Generate insights based on transactions
     */
    public String generateInsights(List<Transaction> transactions) {

        double income = 0;
        double expense = 0;

        Map<String, Double> categorySpend = new HashMap<>();

        // 🔥 Calculate totals
        for (Transaction t : transactions) {

            if ("Income".equals(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();

                String category = t.getCategory();
                categorySpend.put(
                        category,
                        categorySpend.getOrDefault(category, 0.0) + t.getAmount()
                );
            }
        }

        StringBuilder insights = new StringBuilder();

        // ================= RULE 1: SAVINGS =================
        if (income > 0) {

            double savingsPercent = ((income - expense) / income) * 100;

            if (savingsPercent >= 40) {
                insights.append("✅ Great job! You are saving well.\n");
            } else if (savingsPercent < 10) {
                insights.append("⚠️ Low savings. Try to reduce expenses.\n");
            }
        }

        // ================= RULE 2: FOOD =================
        double food = categorySpend.getOrDefault("Food", 0.0);

        if (income > 0 && (food / income) * 100 > 30) {
            insights.append("🍔 Food expenses are higher than usual.\n");
        }

        // ================= RULE 3: ENTERTAINMENT =================
        double entertainment = categorySpend.getOrDefault("Entertainment", 0.0);

        if (income > 0 && (entertainment / income) * 100 > 20) {
            insights.append("🎬 Reduce entertainment spending.\n");
        }

        // ================= RULE 4: HIGH EXPENSE =================
        if (income > 0 && expense > income) {
            insights.append("🚨 You are overspending!\n");
        }

        // Default message
        if (insights.length() == 0) {
            insights.append("👍 Your spending looks balanced.");
        }

        return insights.toString();
    }
}