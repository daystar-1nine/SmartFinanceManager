package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Sidebar
 * -------
 * A reusable sidebar panel containing navigation elements.
 */
@SuppressWarnings({"serial", "this-escape"})
public class Sidebar extends JPanel {

    public Sidebar() {
        setName("sidebar"); // Mark it for ThemeUtil sidebar color scheme
        setPreferredSize(new Dimension(200, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30, 30, 47)); // Standard dark sidebar base
    }

    /**
     * Adds menu items or buttons aligned with spacing.
     */
    public void addMenuItem(JButton button) {
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(button);
    }

    /**
     * Adds vertical space between elements.
     */
    public void addSpacing(int height) {
        add(Box.createVerticalStrut(height));
    }
}
