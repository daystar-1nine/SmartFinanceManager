package model;

/**
 * Goal Model Class
 * ----------------
 * Represents a user's savings goal.
 */
public class Goal {

    private String name;
    private double targetAmount;
    private double currentAmount;
    private String targetDate; // YYYY-MM-DD format

    public Goal(String name, double targetAmount, double currentAmount, String targetDate) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    public String getName() {
        return name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getTargetDate() {
        return targetDate;
    }

    /**
     * Converts goal object into file-friendly format
     * Example: New Laptop,50000,12000,2026-12-31
     */
    public String toFileString() {
        return name.replace(",", " ") + "," + targetAmount + "," + currentAmount + "," + targetDate;
    }

    /**
     * Creates a Goal object from file data
     */
    public static Goal fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",");
        if (parts.length == 4) {
            try {
                String name = parts[0].trim();
                double target = Double.parseDouble(parts[1].trim());
                double current = Double.parseDouble(parts[2].trim());
                String date = parts[3].trim();
                return new Goal(name, target, current, date);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
