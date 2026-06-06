package ui;

import model.Transaction;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PieChartPanel
 * -------------
 * A custom Swing component that draws a beautiful, anti-aliased pie chart
 * showing expense category distribution.
 */
public class PieChartPanel extends JPanel {
    private final Map<String, Double> expenses = new HashMap<>();
    private double totalExpense = 0.0;

    // A modern, soft color palette for categories
    private static final Map<String, Color> COLOR_MAP = Map.of(
        "Food", new Color(255, 107, 107),       // Coral red
        "Transport", new Color(77, 150, 255),  // Soft blue
        "Shopping", new Color(255, 217, 61),   // Pastel yellow
        "Bills", new Color(107, 203, 119),      // Soft green
        "Other", new Color(155, 93, 229)       // Pastel purple
    );
    private static final Color DEFAULT_COLOR = new Color(170, 170, 170); // Medium grey for custom/unknown categories

    public PieChartPanel() {
        setPreferredSize(new Dimension(250, 200));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Spending Chart"));
    }

    public void updateData(List<Transaction> transactions) {
        expenses.clear();
        totalExpense = 0.0;
        for (Transaction t : transactions) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                String cat = t.getCategory();
                double amt = t.getAmount();
                expenses.put(cat, expenses.getOrDefault(cat, 0.0) + amt);
                totalExpense += amt;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smooth, high-quality circular drawing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        
        Insets insets = getInsets();
        int contentWidth = width - insets.left - insets.right;
        int contentHeight = height - insets.top - insets.bottom;

        // If no expenses are present, draw an empty/neutral state
        if (totalExpense == 0.0) {
            g2d.setColor(new Color(240, 240, 240));
            int size = Math.min(contentWidth, contentHeight) - 50;
            if (size < 10) size = 10;
            int x = insets.left + (contentWidth - size) / 2;
            int y = insets.top + (contentHeight - size) / 2;
            g2d.fillOval(x, y, size, size);
            
            g2d.setColor(new Color(120, 120, 120));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String msg = "No expenses recorded";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, x + (size - msgWidth) / 2, y + size / 2 + 5);
            return;
        }

        // Draw the custom pie chart
        int pieSize = Math.min(contentWidth, contentHeight) - 55;
        if (pieSize < 10) pieSize = 10;
        int px = insets.left + (contentWidth - pieSize) / 2;
        int py = insets.top + (contentHeight - pieSize) / 2 - 10; // offset slightly upwards to leave room for the legend

        int startAngle = 0;
        int remainingAngle = 360;
        int index = 0;
        int totalItems = expenses.size();

        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            index++;
            int angle;
            if (index == totalItems) {
                angle = remainingAngle; // Ensure no round-off gaps at the end
            } else {
                angle = (int) Math.round((entry.getValue() / totalExpense) * 360);
                remainingAngle -= angle;
            }
            
            g2d.setColor(COLOR_MAP.getOrDefault(entry.getKey(), DEFAULT_COLOR));
            g2d.fillArc(px, py, pieSize, pieSize, startAngle, angle);
            
            // Draw a clean white separator border between slices
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawArc(px, py, pieSize, pieSize, startAngle, angle);
            
            startAngle += angle;
        }

        // Draw a compact wrapping legend at the bottom
        int legendY = py + pieSize + 15;
        int legendX = insets.left + 5;
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        
        int itemX = legendX;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            String category = entry.getKey();
            double value = entry.getValue();
            double pct = (value / totalExpense) * 100.0;
            String text = String.format("%s (%.0f%%)", category, pct);
            
            Color color = COLOR_MAP.getOrDefault(category, DEFAULT_COLOR);
            int textWidth = fm.stringWidth(text);
            
            // Wrap to next line if it exceeds panel width
            if (itemX + 12 + textWidth > width - insets.right - 5) {
                itemX = legendX;
                legendY += 12;
            }
            
            // Draw colored indicator circle
            g2d.setColor(color);
            g2d.fillOval(itemX, legendY - 7, 7, 7);
            
            // Draw category label with percentage
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawString(text, itemX + 10, legendY);
            
            itemX += 12 + textWidth + 12; // spacing to next legend item
        }
    }
}
