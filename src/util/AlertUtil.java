package util;

import javax.swing.*;
import java.awt.*;

/**
 * AlertUtil Class
 * ---------------
 * Centralizes JOptionPane dialogue messages for consistent user alert visuals.
 */
public class AlertUtil {

    /**
     * Shows a standard information dialog.
     */
    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows an error dialog.
     */
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     */
    public static int showConfirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }
}
