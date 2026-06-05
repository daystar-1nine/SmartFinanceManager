package ui;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardFrame Class
 * --------------------
 * Main application screen after login.
 * Handles navigation and dynamic panel switching.
 */
public class DashboardFrame extends JFrame {

    private JPanel mainPanel;
    private JPanel contentPanel;
    private String username;

    public DashboardFrame(String username) {

        this.username = username;

        setTitle("Smart Finance Manager - Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, 600));
        sidebar.setBackground(new Color(30, 30, 47));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Buttons
        JButton dashboardBtn = createSidebarButton("Dashboard");
        JButton addBtn = createSidebarButton("Add Transaction");
        JButton reportBtn = createSidebarButton("Reports");
        JButton profileBtn = createSidebarButton("Profile");
        JButton aboutBtn = createSidebarButton("About");

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(dashboardBtn);
        sidebar.add(addBtn);
        sidebar.add(reportBtn);
        sidebar.add(profileBtn);
        sidebar.add(aboutBtn);

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(welcome, BorderLayout.NORTH);

        // Default Content Panel
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(new JLabel("Dashboard Content Here"));

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add panels
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // 🔥 Button Actions

        // Dashboard
        dashboardBtn.addActionListener(e -> {
            switchPanel(getDashboardPanel());
        });

        // Add Transaction
        addBtn.addActionListener(e -> {
            switchPanel(new TransactionPanel(username));
        });

        // Reports
        reportBtn.addActionListener(e -> {
            switchPanel(createSimplePanel("Reports Section Coming Soon"));
        });

        // Profile
        profileBtn.addActionListener(e -> {
            switchPanel(createSimplePanel("Profile Section Coming Soon"));
        });

        // About
        aboutBtn.addActionListener(e -> {
            switchPanel(createSimplePanel("About Section Coming Soon"));
        });

        setVisible(true);
    }

    /**
     * Switches center panel dynamically
     */
    private void switchPanel(JPanel newPanel) {
        mainPanel.remove(contentPanel);
        contentPanel = newPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Default dashboard panel
     */
    private JPanel getDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel("Dashboard Content Here"));
        return panel;
    }

    /**
     * Creates simple placeholder panel
     */
    private JPanel createSimplePanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(text));
        return panel;
    }

    /**
     * Sidebar button styling
     */
    private JButton createSidebarButton(String text) {

        JButton button = new JButton(text);

        // Remove default styling
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        // Colors
        Color defaultColor = new Color(35, 35, 60);
        Color hoverColor = new Color(60, 60, 90);

        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);

        // Font
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Size
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Padding
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 🔥 Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor);
            }
        });

        return button;
    }
}