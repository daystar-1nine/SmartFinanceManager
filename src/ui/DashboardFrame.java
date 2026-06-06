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
    private JComponent contentPanel; 
    private String username;

    public DashboardFrame(String username) {
        this.username = username;

        setTitle("Smart Finance Manager - Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar Panel
        ui.components.Sidebar sidebar = new ui.components.Sidebar();

        // Menu Buttons
        JButton dashboardBtn = createSidebarButton("Dashboard");
        JButton addBtn = createSidebarButton("Add Transaction");
        JButton reportBtn = createSidebarButton("Reports");
        JButton loansBtn = createSidebarButton("Loans");
        JButton profileBtn = createSidebarButton("Profile");
        JButton aboutBtn = createSidebarButton("About");

        sidebar.addSpacing(20);
        sidebar.addMenuItem(dashboardBtn);
        sidebar.addMenuItem(addBtn);
        sidebar.addMenuItem(reportBtn);
        sidebar.addMenuItem(loansBtn);
        sidebar.addMenuItem(profileBtn);
        sidebar.addMenuItem(aboutBtn);

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header Panel (Welcome text)
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Default Content Panel
        JScrollPane scrollPane = new JScrollPane(new DashboardPanel(username));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        contentPanel = scrollPane;
 
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add components to main layout
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Menu Button Action Listeners
        dashboardBtn.addActionListener(e -> switchPanel(new DashboardPanel(username)));
        addBtn.addActionListener(e -> switchPanel(new TransactionPanel(username)));
        reportBtn.addActionListener(e -> switchPanel(new ReportPanel(username)));
        loansBtn.addActionListener(e -> switchPanel(new LoanPanel(username)));
        profileBtn.addActionListener(e -> switchPanel(new ProfilePanel(username)));
        aboutBtn.addActionListener(e -> switchPanel(createSimplePanel("About Section Coming Soon")));

        setVisible(true);
    }

    /**
     * Switches center panel dynamically
     */
    private void switchPanel(JPanel newPanel) {
        mainPanel.remove(contentPanel);
        
        JScrollPane scrollPane = new JScrollPane(newPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        contentPanel = scrollPane;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
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
        button.setName("sidebarBtn");

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        Color defaultColor = new Color(35, 35, 60);
        Color hoverColor = new Color(60, 60, 90);

        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

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