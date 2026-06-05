package model;

/**
 * Budget Model
 * ------------
 * Stores budget details for a category
 */
public class Budget {

    private String category;
    private double monthlyLimit;

    public Budget(String category, double monthlyLimit) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }

    public String getCategory() {
        return category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }
}