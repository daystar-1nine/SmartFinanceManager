package util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * ThemeUtil
 * ---------
 * Manages color schemes and recursively applies Light/Dark theme styles
 * across all Swing UI components.
 */
public class ThemeUtil {

    private static boolean isDarkMode = false;

    // ================= LIGHT COLOR PALETTE =================
    public static final Color LIGHT_BG = Color.WHITE;
    public static final Color LIGHT_TEXT = new Color(33, 33, 33);
    public static final Color LIGHT_SECONDARY_TEXT = new Color(100, 100, 100);
    public static final Color LIGHT_CARD_BG = Color.WHITE;
    public static final Color LIGHT_BORDER = new Color(220, 220, 220);

    // ================= DARK COLOR PALETTE =================
    public static final Color DARK_BG = new Color(24, 24, 35);
    public static final Color DARK_TEXT = new Color(240, 240, 240);
    public static final Color DARK_SECONDARY_TEXT = new Color(170, 170, 170);
    public static final Color DARK_CARD_BG = new Color(35, 35, 48);
    public static final Color DARK_BORDER = new Color(60, 60, 75);

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    public static Color getBackgroundColor() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }

    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getSecondaryTextColor() {
        return isDarkMode ? DARK_SECONDARY_TEXT : LIGHT_SECONDARY_TEXT;
    }

    public static Color getCardBackgroundColor() {
        return isDarkMode ? DARK_CARD_BG : LIGHT_CARD_BG;
    }

    public static Color getBorderColor() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    /**
     * Recursively applies the active theme colors to a component and all of its children.
     */
    public static void applyTheme(Component comp) {
        if (comp == null) return;

        // Enforce logical SansSerif font to ensure Hindi Devanagari character support
        Font currentFont = comp.getFont();
        if (currentFont != null && !currentFont.getName().equalsIgnoreCase("SansSerif") 
                && !currentFont.getName().equalsIgnoreCase("Dialog")
                && !currentFont.getName().equalsIgnoreCase("Monospaced")) {
            comp.setFont(new Font("SansSerif", currentFont.getStyle(), currentFont.getSize()));
        }

        Color bg = getBackgroundColor();
        Color text = getTextColor();
        Color cardBg = getCardBackgroundColor();
        Color border = getBorderColor();

        // 1. Style component based on type
        if (comp instanceof JLabel) {
            if (!"customColorLabel".equals(comp.getName())) {
                comp.setForeground(text);
            }
        } else if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            table.setBackground(cardBg);
            table.setForeground(text);
            table.setGridColor(border);
            table.getTableHeader().setBackground(border);
            table.getTableHeader().setForeground(text);
        } else if (comp instanceof JTextArea) {
            JTextArea ta = (JTextArea) comp;
            ta.setBackground(isDarkMode ? new Color(30, 30, 42) : new Color(250, 250, 250));
            ta.setForeground(text);
            ta.setCaretColor(text);
        } else if (comp instanceof JTextField) {
            JTextField tf = (JTextField) comp;
            tf.setBackground(isDarkMode ? new Color(30, 30, 42) : Color.WHITE);
            tf.setForeground(text);
            tf.setCaretColor(text);
            tf.setBorder(BorderFactory.createLineBorder(border, 1));
        } else if (comp instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) comp;
            cb.setBackground(cardBg);
            cb.setForeground(text);
        } else if (comp instanceof JProgressBar) {
            JProgressBar pb = (JProgressBar) comp;
            pb.setBackground(isDarkMode ? new Color(45, 45, 60) : new Color(230, 230, 230));
        } else if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            // Style standard buttons, skip special red buttons (logout/delete) and sidebar buttons
            if (btn.getBackground() != null && !btn.getBackground().equals(new Color(211, 47, 47)) 
                    && !btn.getBackground().equals(new Color(239, 83, 80)) && !"sidebarBtn".equals(btn.getName())) {
                btn.setBackground(isDarkMode ? new Color(50, 50, 70) : new Color(240, 240, 240));
                btn.setForeground(text);
            }
        } else if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            String name = panel.getName();
            
            if ("sidebar".equals(name)) {
                // Keep sidebar dark always or toggle style slightly
                panel.setBackground(isDarkMode ? new Color(20, 20, 30) : new Color(30, 30, 47));
            } else if ("card".equals(name) || panel.getBorder() instanceof TitledBorder) {
                panel.setBackground(cardBg);
                if ("card".equals(name) && !(panel.getBorder() instanceof TitledBorder)) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(border, 1, true),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            } else {
                panel.setBackground(bg);
            }
        } else if (comp instanceof JViewport) {
            comp.setBackground(bg);
        } else if (comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) comp;
            sp.setBackground(bg);
            sp.getViewport().setBackground(bg);
            sp.setBorder(null);
        }

        // 2. Walk titled borders
        if (comp instanceof JComponent) {
            JComponent jc = (JComponent) comp;
            if (jc.getBorder() instanceof TitledBorder) {
                TitledBorder tb = (TitledBorder) jc.getBorder();
                tb.setTitleColor(text);
                tb.setBorder(BorderFactory.createLineBorder(border, 1, true));
                Font titleFont = tb.getTitleFont();
                if (titleFont != null) {
                    tb.setTitleFont(new Font("SansSerif", titleFont.getStyle(), titleFont.getSize()));
                } else {
                    tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
                }
            }
        }

        // 3. Recurse down containers
        if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                applyTheme(child);
            }
        }
    }
}
