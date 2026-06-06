package util;

/**
 * Constants Class
 * ---------------
 * Centralizes directory paths, file paths, and core application settings.
 */
public class Constants {

    public static final String USER_FILE = DataPathManager.getUserFile();
    
    public static final String BUDGET_DIR = DataPathManager.getBudgetDir();
    public static final String GOAL_DIR = DataPathManager.getGoalDir();
    public static final String TRANSACTION_DIR = DataPathManager.getTransactionDir();
    public static final String LOAN_DIR = DataPathManager.getLoanDir();

    public static final String[] CATEGORIES = {"Food", "Transport", "Shopping", "Bills", "Other"};
}
