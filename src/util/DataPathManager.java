package util;

import java.io.File;

/**
 * DataPathManager Class
 * --------------------
 * Manages the resolution of directories for user data.
 * Redirects storage from "src/data/" inside the IDE to the user's home folder.
 */
public class DataPathManager {

    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "SmartFinanceManager";

    public static String getBaseDir() {
        ensureDirectoryExists(BASE_DIR);
        return BASE_DIR;
    }

    public static String getUserFile() {
        String dir = BASE_DIR + File.separator + "users";
        ensureDirectoryExists(dir);
        return dir + File.separator + "users.txt";
    }

    public static String getBudgetDir() {
        String dir = BASE_DIR + File.separator + "budgets";
        ensureDirectoryExists(dir);
        return dir + File.separator;
    }

    public static String getGoalDir() {
        String dir = BASE_DIR + File.separator + "goals";
        ensureDirectoryExists(dir);
        return dir + File.separator;
    }

    public static String getTransactionDir() {
        String dir = BASE_DIR + File.separator + "transactions";
        ensureDirectoryExists(dir);
        return dir + File.separator;
    }

    public static String getLoanDir() {
        String dir = BASE_DIR + File.separator + "loans";
        ensureDirectoryExists(dir);
        return dir + File.separator;
    }

    private static void ensureDirectoryExists(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
