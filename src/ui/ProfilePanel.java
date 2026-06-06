/*
 * Decompiled with CFR 0.152.
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.time.LocalDate;
import java.awt.Insets;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import model.Goal;
import model.Transaction;
import model.User;
import service.AuthService;
import service.BudgetService;
import service.GoalService;
import service.TransactionService;
import ui.LoginFrame;
import util.ThemeUtil;

public class ProfilePanel
extends JPanel
implements Scrollable {
    private final String username;
    private final AuthService authService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private User currentUser;
    private List<Goal> userGoals;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> themeBox;
    private JLabel totalTxLabel;
    private JLabel regDateLabel;
    private JLabel maxExpenseLabel;
    private JLabel maxIncomeLabel;
    private final Map<String, JTextField> budgetFields = new HashMap<String, JTextField>();
    private JPanel goalsListPanel;
    private JTextField goalNameField;
    private JTextField goalTargetField;
    private JTextField goalDateField;
    private JLabel titleLabel;
    private JTabbedPane tabbedPane;
    private JPanel credentialsCard;
    private JPanel statsCard;
    private JPanel budgetCard;
    private JPanel listCard;
    private JPanel formCard;
    private JLabel userLabel;
    private JLabel usernameVal;
    private JLabel emailLabel;
    private JLabel passLabel;
    private JLabel themeLabel;
    private JButton saveProfileBtn;
    private JLabel memberSinceTitleLabel;
    private JLabel totalTxTitleLabel;
    private JLabel largestExpenseTitleLabel;
    private JLabel largestIncomeTitleLabel;
    private final Map<String, JLabel> budgetLabels = new HashMap<String, JLabel>();
    private JButton saveBudgetsBtn;
    private JLabel goalNameLabel;
    private JLabel goalTargetLabel;
    private JLabel goalDateLabel;
    private JButton createGoalBtn;
    private JButton logoutBtn;
    private JButton resetBtn;

    public ProfilePanel(String username) {
        this.username = username;
        this.authService = new AuthService();
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService(username);
        this.goalService = new GoalService();
        this.currentUser = this.authService.getUser(username);
        this.userGoals = this.goalService.loadGoals(username);
        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.setBackground(ThemeUtil.getBackgroundColor());
        this.add((Component)this.createHeaderPanel(), "North");
        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setFont(new Font("SansSerif", 1, 12));
        this.tabbedPane.addTab("Account & Stats", this.createAccountStatsTab());
        this.tabbedPane.addTab("Monthly Budgets", this.createBudgetsTab());
        this.tabbedPane.addTab("Savings Goals", this.createGoalsTab());
        this.add((Component)this.tabbedPane, "Center");
        this.add((Component)this.createActionPanel(), "South");
        ThemeUtil.applyTheme(this);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(0, 10, 5));
        panel.setOpaque(false);
        this.titleLabel = new JLabel("User Profile & Settings");
        this.titleLabel.setFont(new Font("SansSerif", 1, 22));
        this.titleLabel.setForeground(ThemeUtil.getTextColor());
        panel.add(this.titleLabel);
        return panel;
    }

    private JPanel createAccountStatsTab() {
        JPanel tab = new JPanel(new GridLayout(1, 2, 20, 0));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.credentialsCard = new JPanel();
        this.credentialsCard.setLayout(new BoxLayout(this.credentialsCard, 1));
        this.credentialsCard.setName("card");
        this.credentialsCard.setBackground(ThemeUtil.getCardBackgroundColor());

        JLabel credTitle = new JLabel("Profile Details");
        credTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        credTitle.setAlignmentX(0.5f);
        this.credentialsCard.add(Box.createVerticalStrut(10));
        this.credentialsCard.add(credTitle);
        this.credentialsCard.add(Box.createVerticalStrut(15));

        JPanel avatarPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw circular background with gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(77, 150, 255), 70, 70, new Color(41, 121, 255));
                g2d.setPaint(gp);
                g2d.fillOval(0, 0, 70, 70);
                
                // Draw border
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(1, 1, 68, 68);
                
                // Draw initial letter
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
                String initial = username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase();
                FontMetrics fm = g2d.getFontMetrics();
                int x = (70 - fm.stringWidth(initial)) / 2;
                int y = (70 - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(initial, x, y);
            }
        };
        avatarPanel.setPreferredSize(new Dimension(70, 70));
        avatarPanel.setMaximumSize(new Dimension(70, 70));
        avatarPanel.setBackground(ThemeUtil.getCardBackgroundColor());
        avatarPanel.setAlignmentX(0.5f);

        this.credentialsCard.add(avatarPanel);
        this.credentialsCard.add(Box.createVerticalStrut(15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        // Initialize input components
        this.emailField = new JTextField();
        this.passwordField = new JPasswordField();
        this.themeBox = new JComboBox<>(new String[]{"Light", "Dark"});
        this.themeBox.addActionListener(e -> toggleTheme());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Dimension inputDim = new Dimension(160, 30);
        this.emailField.setPreferredSize(inputDim);
        this.passwordField.setPreferredSize(inputDim);
        this.themeBox.setPreferredSize(inputDim);

        this.userLabel = new JLabel("Username:");
        this.userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.userLabel.setForeground(ThemeUtil.getTextColor());
        this.usernameVal = new JLabel(this.username);
        this.usernameVal.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.usernameVal.setForeground(ThemeUtil.getTextColor());

        this.emailLabel = new JLabel("Email:");
        this.emailLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.emailLabel.setForeground(ThemeUtil.getTextColor());
        this.emailField.setFont(new Font("SansSerif", 0, 12));

        this.passLabel = new JLabel("Password:");
        this.passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.passLabel.setForeground(ThemeUtil.getTextColor());
        this.passwordField.setFont(new Font("SansSerif", 0, 12));

        this.themeLabel = new JLabel("Theme Mode:");
        this.themeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.themeLabel.setForeground(ThemeUtil.getTextColor());

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(this.userLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(this.usernameVal, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(this.emailLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(this.emailField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(this.passLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(this.passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(this.themeLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(this.themeBox, gbc);

        this.credentialsCard.add(form);
        this.credentialsCard.add(Box.createVerticalStrut(15));

        this.saveProfileBtn = new JButton("Update Credentials");
        this.saveProfileBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.saveProfileBtn.setAlignmentX(0.5f);
        this.saveProfileBtn.addActionListener(e -> this.updateCredentials());
        this.credentialsCard.add(this.saveProfileBtn);
        this.credentialsCard.add(Box.createVerticalGlue());

        this.statsCard = new JPanel();
        this.statsCard.setLayout(new BoxLayout(this.statsCard, 1));
        this.statsCard.setName("card");
        this.statsCard.setBackground(ThemeUtil.getCardBackgroundColor());

        JLabel statsTitle = new JLabel("Account Statistics");
        statsTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        statsTitle.setAlignmentX(0.5f);
        this.statsCard.add(Box.createVerticalStrut(10));
        this.statsCard.add(statsTitle);
        this.statsCard.add(Box.createVerticalStrut(15));

        JPanel statsGrid = new JPanel(new GridBagLayout());
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbcStats = new GridBagConstraints();
        gbcStats.insets = new Insets(12, 10, 12, 10);
        gbcStats.fill = GridBagConstraints.HORIZONTAL;

        this.memberSinceTitleLabel = new JLabel("Member Since:");
        this.memberSinceTitleLabel.setForeground(ThemeUtil.getTextColor());
        this.regDateLabel = new JLabel(this.currentUser.getRegistrationDate());
        this.regDateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.regDateLabel.setForeground(ThemeUtil.getTextColor());

        this.totalTxTitleLabel = new JLabel("Total Transactions:");
        this.totalTxTitleLabel.setForeground(ThemeUtil.getTextColor());
        this.totalTxLabel = new JLabel("0");
        this.totalTxLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.totalTxLabel.setForeground(ThemeUtil.getTextColor());

        this.largestExpenseTitleLabel = new JLabel("Largest Single Expense:");
        this.largestExpenseTitleLabel.setForeground(ThemeUtil.getTextColor());
        this.maxExpenseLabel = new JLabel("₹0.00");
        this.maxExpenseLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.maxExpenseLabel.setForeground(new Color(198, 40, 40));

        this.largestIncomeTitleLabel = new JLabel("Largest Single Income:");
        this.largestIncomeTitleLabel.setForeground(ThemeUtil.getTextColor());
        this.maxIncomeLabel = new JLabel("₹0.00");
        this.maxIncomeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        this.maxIncomeLabel.setForeground(new Color(46, 125, 50));

        gbcStats.gridy = 0;
        gbcStats.gridx = 0;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.WEST;
        this.memberSinceTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.memberSinceTitleLabel.setForeground(ThemeUtil.getSecondaryTextColor());
        statsGrid.add(this.memberSinceTitleLabel, gbcStats);
        gbcStats.gridx = 1;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.EAST;
        this.regDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statsGrid.add(this.regDateLabel, gbcStats);

        gbcStats.gridy = 1;
        gbcStats.gridx = 0;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.WEST;
        this.totalTxTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.totalTxTitleLabel.setForeground(ThemeUtil.getSecondaryTextColor());
        statsGrid.add(this.totalTxTitleLabel, gbcStats);
        gbcStats.gridx = 1;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.EAST;
        this.totalTxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statsGrid.add(this.totalTxLabel, gbcStats);

        gbcStats.gridy = 2;
        gbcStats.gridx = 0;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.WEST;
        this.largestExpenseTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.largestExpenseTitleLabel.setForeground(ThemeUtil.getSecondaryTextColor());
        statsGrid.add(this.largestExpenseTitleLabel, gbcStats);
        gbcStats.gridx = 1;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.EAST;
        this.maxExpenseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statsGrid.add(this.maxExpenseLabel, gbcStats);

        gbcStats.gridy = 3;
        gbcStats.gridx = 0;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.WEST;
        this.largestIncomeTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.largestIncomeTitleLabel.setForeground(ThemeUtil.getSecondaryTextColor());
        statsGrid.add(this.largestIncomeTitleLabel, gbcStats);
        gbcStats.gridx = 1;
        gbcStats.weightx = 0.5;
        gbcStats.anchor = GridBagConstraints.EAST;
        this.maxIncomeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statsGrid.add(this.maxIncomeLabel, gbcStats);

        this.statsCard.add(statsGrid);
        this.statsCard.add(Box.createVerticalGlue());

        tab.add(this.credentialsCard);
        tab.add(this.statsCard);
        this.refreshStatistics();
        return tab;
    }

    private JPanel createBudgetsTab() {
        JPanel tab = new JPanel(new GridBagLayout());
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.budgetCard = new JPanel();
        this.budgetCard.setLayout(new BoxLayout(this.budgetCard, 1));
        this.budgetCard.setName("card");
        this.budgetCard.setBackground(ThemeUtil.getCardBackgroundColor());
        this.budgetCard.setPreferredSize(new Dimension(400, 320));

        JLabel budgetTitle = new JLabel("Category Budget Limits (Monthly)");
        budgetTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        budgetTitle.setAlignmentX(0.5f);
        this.budgetCard.add(Box.createVerticalStrut(10));
        this.budgetCard.add(budgetTitle);
        this.budgetCard.add(Box.createVerticalStrut(10));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 10, 10));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(15, 20, 15, 20));
        Map<String, Double> currentBudgets = this.budgetService.getCategoryBudgets();
        for (String category : new String[]{"Food", "Transport", "Shopping", "Bills", "Other"}) {
            JLabel label = new JLabel(category + " (\u20b9):");
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setForeground(ThemeUtil.getTextColor());
            double currentLimit = currentBudgets.getOrDefault(category, 0.0);
            JTextField field = new JTextField(String.format("%.0f", currentLimit), 8);
            field.setFont(new Font("SansSerif", 0, 12));
            formGrid.add(label);
            formGrid.add(field);
            this.budgetFields.put(category, field);
            this.budgetLabels.put(category, label);
        }
        this.budgetCard.add(formGrid);

        this.saveBudgetsBtn = new JButton("Save Budget Settings");
        this.saveBudgetsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.saveBudgetsBtn.setAlignmentX(0.5f);
        this.saveBudgetsBtn.addActionListener(e -> this.saveBudgets());
        this.budgetCard.add(this.saveBudgetsBtn);
        this.budgetCard.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        tab.add((Component)this.budgetCard, gbc);
        return tab;
    }

    private JPanel createGoalsTab() {
        JPanel tab = new JPanel(new GridLayout(1, 2, 20, 0));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.listCard = new JPanel(new BorderLayout());
        this.listCard.setName("card");
        this.listCard.setBackground(ThemeUtil.getCardBackgroundColor());

        JLabel listTitle = new JLabel("Current Savings Goals", SwingConstants.CENTER);
        listTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        listTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.listCard.add((Component)listTitle, "North");

        this.goalsListPanel = new JPanel();
        this.goalsListPanel.setLayout(new BoxLayout(this.goalsListPanel, 1));
        this.goalsListPanel.setBackground(ThemeUtil.getCardBackgroundColor());
        this.goalsListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(this.goalsListPanel);
        scrollPane.setBorder(null);
        this.listCard.add((Component)scrollPane, "Center");

        this.formCard = new JPanel();
        this.formCard.setLayout(new BoxLayout(this.formCard, 1));
        this.formCard.setName("card");
        this.formCard.setBackground(ThemeUtil.getCardBackgroundColor());

        JLabel formTitle = new JLabel("Add New Goal");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        formTitle.setAlignmentX(0.5f);
        this.formCard.add(Box.createVerticalStrut(10));
        this.formCard.add(formTitle);
        this.formCard.add(Box.createVerticalStrut(15));

        JPanel formGrid = new JPanel(new GridLayout(4, 1, 10, 10));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(15, 20, 15, 20));
        this.goalNameField = new JTextField(12);
        this.goalTargetField = new JTextField(12);
        this.goalDateField = new JTextField(LocalDate.now().plusMonths(6L).toString(), 12);

        JPanel r1 = new JPanel(new BorderLayout());
        r1.setOpaque(false);
        this.goalNameLabel = new JLabel("Goal Name (e.g. Travel):");
        this.goalNameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.goalNameLabel.setForeground(ThemeUtil.getTextColor());
        r1.add((Component)this.goalNameLabel, "North");
        r1.add((Component)this.goalNameField, "South");

        JPanel r2 = new JPanel(new BorderLayout());
        r2.setOpaque(false);
        this.goalTargetLabel = new JLabel("Target Amount (\u20b9):");
        this.goalTargetLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.goalTargetLabel.setForeground(ThemeUtil.getTextColor());
        r2.add((Component)this.goalTargetLabel, "North");
        r2.add((Component)this.goalTargetField, "South");

        JPanel r3 = new JPanel(new BorderLayout());
        r3.setOpaque(false);
        this.goalDateLabel = new JLabel("Target Date (YYYY-MM-DD):");
        this.goalDateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.goalDateLabel.setForeground(ThemeUtil.getTextColor());
        r3.add((Component)this.goalDateLabel, "North");
        r3.add((Component)this.goalDateField, "South");

        this.createGoalBtn = new JButton("Create Savings Goal");
        this.createGoalBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        this.createGoalBtn.addActionListener(e -> this.createGoal());

        formGrid.add(r1);
        formGrid.add(r2);
        formGrid.add(r3);
        formGrid.add(this.createGoalBtn);

        this.formCard.add(formGrid);
        this.formCard.add(Box.createVerticalGlue());

        tab.add(this.listCard);
        tab.add(this.formCard);
        this.refreshGoalsList();
        return tab;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(1, 20, 5));
        panel.setOpaque(false);
        this.logoutBtn = new JButton("Logout");
        this.logoutBtn.setFont(new Font("SansSerif", 1, 13));
        this.logoutBtn.setBackground(new Color(211, 47, 47));
        this.logoutBtn.setForeground(Color.WHITE);
        this.logoutBtn.setFocusPainted(false);
        this.logoutBtn.addActionListener(e -> this.logout());
        this.resetBtn = new JButton("Reset Data");
        this.resetBtn.setFont(new Font("SansSerif", 1, 13));
        this.resetBtn.setFocusPainted(false);
        this.resetBtn.addActionListener(e -> this.resetData());
        panel.add(this.logoutBtn);
        panel.add(this.resetBtn);
        return panel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", 0);
        if (confirm == 0) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow != null) {
                parentWindow.dispose();
            }
            new LoginFrame();
        }
    }

    private void resetData() {
        int confirm = JOptionPane.showConfirmDialog(this, "Warning: This will delete all transactions and savings goals for " + this.username + " forever. Continue?", "Reset All User Data", 0, 2);
        if (confirm == 0) {
            this.transactionService.saveAllTransactions(this.username, new ArrayList<Transaction>());
            this.goalService.saveGoals(this.username, new ArrayList<Goal>());
            this.refreshStatistics();
            this.refreshGoalsList();
            JOptionPane.showMessageDialog(this, "All your data has been reset.", "Data Cleared", 1);
        }
    }

    private void updateCredentials() {
        String email = this.emailField.getText().trim();
        String password = new String(this.passwordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty", "Error", 0);
            return;
        }
        boolean success = this.authService.updateUserCredentials(this.username, password, email);
        if (success) {
            this.currentUser = this.authService.getUser(this.username);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", 1);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile", "Error", 0);
        }
    }

    private void saveBudgets() {
        HashMap<String, Double> newBudgets = new HashMap<String, Double>();
        try {
            for (Map.Entry<String, JTextField> entry : this.budgetFields.entrySet()) {
                double val = Double.parseDouble(entry.getValue().getText().trim());
                if (val < 0.0) {
                    JOptionPane.showMessageDialog(this, "Limits must be positive values", "Error", 0);
                    return;
                }
                newBudgets.put(entry.getKey(), val);
            }
            this.budgetService.saveUserBudgets(newBudgets);
            JOptionPane.showMessageDialog(this, "Category budgets saved!", "Success", 1);
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric limits", "Error", 0);
        }
    }

    private void createGoal() {
        String name = this.goalNameField.getText().trim();
        String targetStr = this.goalTargetField.getText().trim();
        String dateStr = this.goalDateField.getText().trim();
        if (name.isEmpty() || targetStr.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled", "Error", 0);
            return;
        }
        try {
            double target = Double.parseDouble(targetStr);
            if (target <= 0.0) {
                JOptionPane.showMessageDialog(this, "Target must be positive", "Error", 0);
                return;
            }
            LocalDate.parse(dateStr);
            Goal goal = new Goal(name, target, 0.0, dateStr);
            this.userGoals.add(goal);
            this.goalService.saveGoals(this.username, this.userGoals);
            this.goalNameField.setText("");
            this.goalTargetField.setText("");
            this.goalDateField.setText(LocalDate.now().plusMonths(6L).toString());
            this.refreshGoalsList();
            JOptionPane.showMessageDialog(this, "Savings Goal created successfully!", "Success", 1);
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric target amount", "Error", 0);
        }
        catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Dates must match YYYY-MM-DD format", "Error", 0);
        }
    }

    private void refreshGoalsList() {
        this.goalsListPanel.removeAll();
        if (this.userGoals.isEmpty()) {
            JLabel emptyLabel = new JLabel("No active savings goals. Create one on the right!");
            emptyLabel.setFont(new Font("SansSerif", 2, 12));
            emptyLabel.setForeground(ThemeUtil.getSecondaryTextColor());
            emptyLabel.setAlignmentX(0.5f);
            this.goalsListPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < this.userGoals.size(); ++i) {
                int idx = i;
                Goal g = this.userGoals.get(i);
                JPanel row = new JPanel();
                row.setLayout(new BoxLayout(row, 1));
                row.setBackground(ThemeUtil.getCardBackgroundColor());
                row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);
                JLabel title = new JLabel(g.getName());
                title.setFont(new Font("SansSerif", 1, 13));
                title.setForeground(ThemeUtil.getTextColor());
                JLabel date = new JLabel("By: " + g.getTargetDate());
                date.setFont(new Font("SansSerif", 0, 11));
                date.setForeground(ThemeUtil.getSecondaryTextColor());
                header.add((Component)title, "West");
                header.add((Component)date, "East");
                double current = g.getCurrentAmount();
                double target = g.getTargetAmount();
                int pct = (int)Math.round(current / target * 100.0);
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(Math.min(pct, 100));
                bar.setStringPainted(true);
                bar.setFont(new Font("SansSerif", 1, 10));
                bar.setForeground(new Color(77, 182, 172));
                bar.setString(String.format("\u20b9%,.0f / \u20b9%,.0f (%d%%)", current, target, pct));
                JPanel actions = new JPanel(new FlowLayout(2, 5, 0));
                actions.setOpaque(false);
                JButton addFundsBtn = new JButton("Add Savings");
                addFundsBtn.setFont(new Font("SansSerif", 0, 11));
                addFundsBtn.addActionListener(e -> this.addSavingsToGoal(idx));
                JButton deleteGoalBtn = new JButton("Delete");
                deleteGoalBtn.setFont(new Font("SansSerif", 0, 11));
                deleteGoalBtn.setBackground(new Color(239, 83, 80));
                deleteGoalBtn.setForeground(Color.WHITE);
                deleteGoalBtn.addActionListener(e -> this.deleteGoal(idx));
                actions.add(addFundsBtn);
                actions.add(deleteGoalBtn);
                row.add(header);
                row.add(Box.createVerticalStrut(5));
                row.add(bar);
                row.add(Box.createVerticalStrut(5));
                row.add(actions);
                this.goalsListPanel.add(row);
                this.goalsListPanel.add(Box.createVerticalStrut(10));
            }
        }
        this.goalsListPanel.revalidate();
        this.goalsListPanel.repaint();
    }

    private void addSavingsToGoal(int index) {
        Goal g = this.userGoals.get(index);
        String input = JOptionPane.showInputDialog(this, "Enter savings amount to add to " + g.getName() + ":", "Add Savings", 3);
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        try {
            double funds = Double.parseDouble(input.trim());
            if (funds <= 0.0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", 0);
                return;
            }
            g.setCurrentAmount(g.getCurrentAmount() + funds);
            this.goalService.saveGoals(this.username, this.userGoals);
            this.refreshGoalsList();
            JOptionPane.showMessageDialog(this, "Savings added to " + g.getName() + "!", "Success", 1);
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value", "Error", 0);
        }
    }

    private void deleteGoal(int index) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete savings goal: " + this.userGoals.get(index).getName() + "?", "Delete Goal", 0);
        if (confirm == 0) {
            this.userGoals.remove(index);
            this.goalService.saveGoals(this.username, this.userGoals);
            this.refreshGoalsList();
        }
    }

    private void refreshStatistics() {
        List<Transaction> txs = this.transactionService.loadTransactions(this.username);
        this.totalTxLabel.setText(String.valueOf(txs.size()));
        double maxExpense = 0.0;
        double maxIncome = 0.0;
        for (Transaction t : txs) {
            if ("Income".equalsIgnoreCase(t.getType())) {
                if (!(t.getAmount() > maxIncome)) continue;
                maxIncome = t.getAmount();
                continue;
            }
            if (!(t.getAmount() > maxExpense)) continue;
            maxExpense = t.getAmount();
        }
        this.maxExpenseLabel.setText(String.format("\u20b9%,.2f", maxExpense));
        this.maxIncomeLabel.setText(String.format("\u20b9%,.2f", maxIncome));
    }

    private void toggleTheme() {
        boolean dark = this.themeBox.getSelectedIndex() == 1;
        ThemeUtil.setDarkMode(dark);
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            ThemeUtil.applyTheme(window);
            window.revalidate();
            window.repaint();
        }
    }

    private void applyThemeSettings() {
        ThemeUtil.applyTheme(this);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (this.getParent() instanceof JViewport) {
            return this.getParent().getHeight() > this.getPreferredSize().height;
        }
        return false;
    }

}
