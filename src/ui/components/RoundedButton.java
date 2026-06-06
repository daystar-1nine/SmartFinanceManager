package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * RoundedButton
 * -------------
 * A customized Swing button with rounded borders and clean hover highlights.
 */
@SuppressWarnings({"serial", "this-escape"})
public class RoundedButton extends JButton {

    private int radius = 15;
    private Color hoverBgColor;
    private Color normalBgColor;

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public RoundedButton(String text, int radius) {
        this(text);
        this.radius = radius;
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        this.normalBgColor = bg;
        if (bg != null) {
            // Setup a slightly lighter color for hover state
            this.hoverBgColor = new Color(
                    Math.min(255, bg.getRed() + 20),
                    Math.min(255, bg.getGreen() + 20),
                    Math.min(255, bg.getBlue() + 20)
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine background color based on hover state
        Color bg = getModel().isRollover() ? hoverBgColor : normalBgColor;
        if (bg == null) bg = getBackground();
        
        g2d.setColor(bg);
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        g2d.dispose();

        super.paintComponent(g);
    }
}
