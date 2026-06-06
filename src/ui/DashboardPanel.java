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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import model.Loan;
import model.Transaction;
import service.BudgetService;
import service.InsightService;
import service.LoanService;
import service.TransactionService;

import util.Constants;

import util.ThemeUtil;

@SuppressWarnings({"serial", "this-escape"})
public class DashboardPanel
extends JPanel
implements Scrollable {
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final InsightService insightService;
    private final String username;
    private List<Transaction> allTransactions = new ArrayList<Transaction>();
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JLabel loansLabel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JProgressBar scoreBar;
    private JTextField amountField;
    private JComboBox<String> typeBox;
    private JComboBox<String> categoryBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea notificationArea;
    private JTextArea insightArea;
    private JPanel scorePanel;
    private JPanel quickAddPanel;
    private JPanel recentTablePanel;
    private JPanel notificationsPanel;
    private JPanel insightsPanel;
    private JLabel amountLabel;
    private JLabel typeLabel;
    private JLabel categoryLabel;
    private JButton quickAddBtn;

    public DashboardPanel(String username) {
        this.username = username;
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService(username);
        this.insightService = new InsightService();
        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.setBackground(Color.WHITE);
        this.add((Component)this.createSummaryPanel(), "North");
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBackground(Color.WHITE);
        centerContainer.add((Component)this.createQuickAddPanel(), "North");
        centerContainer.add((Component)this.createRecentTablePanel(), "Center");
        centerContainer.add((Component)this.createInsightsPanel(), "South");
        this.add((Component)centerContainer, "Center");
        this.add((Component)this.createNotificationsPanel(), "East");
        this.refreshDashboard();
        ThemeUtil.applyTheme(this);
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
        summaryPanel.setBackground(Color.WHITE);
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setBackground(Color.WHITE);
        this.incomeLabel = new JLabel("Income: \u20b90.00", 0);
        this.incomeLabel.setFont(new Font("SansSerif", 1, 16));
        this.incomeLabel.setForeground(new Color(46, 125, 50));
        this.incomeLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 230, 201), 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.expenseLabel = new JLabel("Expense: \u20b90.00", 0);
        this.expenseLabel.setFont(new Font("SansSerif", 1, 16));
        this.expenseLabel.setForeground(new Color(198, 40, 40));
        this.expenseLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 205, 210), 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.balanceLabel = new JLabel("Balance: \u20b90.00", 0);
        this.balanceLabel.setFont(new Font("SansSerif", 1, 16));
        this.balanceLabel.setForeground(new Color(21, 101, 192));
        this.balanceLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(187, 222, 251), 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.loansLabel = new JLabel("Loans: \u20b90.00", 0);
        this.loansLabel.setName("customColorLabel");
        this.loansLabel.setFont(new Font("SansSerif", 1, 16));
        this.loansLabel.setForeground(new Color(103, 58, 183));
        this.loansLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209, 196, 233), 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        cardsPanel.add(this.incomeLabel);
        cardsPanel.add(this.expenseLabel);
        cardsPanel.add(this.balanceLabel);
        cardsPanel.add(this.loansLabel);
        this.scorePanel = new JPanel(new BorderLayout(5, 5));
        this.scorePanel.setBackground(Color.WHITE);
        this.scorePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Financial Health Score"), BorderFactory.createEmptyBorder(5, 10, 10, 10)));
        this.scoreLabel = new JLabel("Score: 0/100", 2);
        this.scoreLabel.setFont(new Font("SansSerif", 1, 12));
        this.statusLabel = new JLabel("Status: -", 4);
        this.statusLabel.setFont(new Font("SansSerif", 1, 12));
        JPanel scoreTextPanel = new JPanel(new BorderLayout());
        scoreTextPanel.setBackground(Color.WHITE);
        scoreTextPanel.add((Component)this.scoreLabel, "West");
        scoreTextPanel.add((Component)this.statusLabel, "East");
        this.scoreBar = new JProgressBar(0, 100);
        this.scoreBar.setStringPainted(true);
        this.scoreBar.setFont(new Font("SansSerif", 1, 11));
        this.scoreBar.setForeground(new Color(77, 182, 172));
        this.scorePanel.add((Component)scoreTextPanel, "North");
        this.scorePanel.add((Component)this.scoreBar, "Center");
        summaryPanel.add((Component)cardsPanel, "Center");
        summaryPanel.add((Component)this.scorePanel, "South");
        return summaryPanel;
    }

    private JPanel createQuickAddPanel() {
        this.quickAddPanel = new JPanel(new FlowLayout(0, 10, 5));
        this.quickAddPanel.setBackground(Color.WHITE);
        this.quickAddPanel.setBorder(BorderFactory.createTitledBorder("Quick Add Transaction"));
        this.amountField = new JTextField(8);
        this.typeBox = new JComboBox<String>(new String[]{"Income", "Expense"});
        this.categoryBox = new JComboBox<String>(Constants.CATEGORIES);
        this.quickAddBtn = new JButton("Add");
        this.quickAddBtn.setFont(new Font("SansSerif", 1, 12));
        this.quickAddBtn.addActionListener(e -> this.quickAddTransaction());
        this.amountLabel = new JLabel("Amount" + ":");
        this.typeLabel = new JLabel("Type" + ":");
        this.categoryLabel = new JLabel("Category" + ":");
        this.quickAddPanel.add(this.amountLabel);
        this.quickAddPanel.add(this.amountField);
        this.quickAddPanel.add(this.typeLabel);
        this.quickAddPanel.add(this.typeBox);
        this.quickAddPanel.add(this.categoryLabel);
        this.quickAddPanel.add(this.categoryBox);
        this.quickAddPanel.add(this.quickAddBtn);
        return this.quickAddPanel;
    }

    private JPanel createRecentTablePanel() {
        this.recentTablePanel = new JPanel(new GridBagLayout());
        this.recentTablePanel.setBackground(Color.WHITE);
        this.recentTablePanel.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        this.tableModel = new DefaultTableModel(new String[]{"Type", "Amount", "Category", "Date"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(this.tableModel);
        this.table.setRowHeight(25);
        this.table.getTableHeader().setFont(new Font("SansSerif", 1, 12));
        this.table.setFont(new Font("SansSerif", 0, 12));
        this.table.getColumnModel().getColumn(0).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(100);
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = 0;
        gbc.anchor = 10;
        this.recentTablePanel.add((Component)scrollPane, gbc);
        return this.recentTablePanel;
    }

    private JPanel createNotificationsPanel() {
        this.notificationsPanel = new JPanel(new BorderLayout());
        this.notificationsPanel.setPreferredSize(new Dimension(260, 0));
        this.notificationsPanel.setBorder(BorderFactory.createTitledBorder("Notifications"));
        this.notificationArea = new JTextArea();
        this.notificationArea.setEditable(false);
        this.notificationArea.setLineWrap(true);
        this.notificationArea.setWrapStyleWord(true);
        this.notificationArea.setFont(new Font("SansSerif", 0, 12));
        this.notificationArea.setBackground(new Color(250, 250, 250));
        JScrollPane scroll = new JScrollPane(this.notificationArea);
        this.notificationsPanel.add((Component)scroll, "Center");
        return this.notificationsPanel;
    }

    private JPanel createInsightsPanel() {
        this.insightsPanel = new JPanel(new BorderLayout());
        this.insightsPanel.setBackground(Color.WHITE);
        this.insightsPanel.setBorder(BorderFactory.createTitledBorder("Insights"));
        this.insightArea = new JTextArea(3, 40);
        this.insightArea.setEditable(false);
        this.insightArea.setLineWrap(true);
        this.insightArea.setWrapStyleWord(true);
        this.insightArea.setFont(new Font("SansSerif", 0, 12));
        this.insightArea.setBackground(new Color(245, 247, 250));
        this.insightArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.insightsPanel.add((Component)new JScrollPane(this.insightArea), "Center");
        return this.insightsPanel;
    }

    private void quickAddTransaction() {
        try {
            double amount = Double.parseDouble(this.amountField.getText().trim());
            if (amount <= 0.0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", 0);
                return;
            }
            int maxId = 0;
            for (Transaction t : this.allTransactions) {
                if (t.getId() <= maxId) continue;
                maxId = t.getId();
            }
            Transaction newTx = new Transaction(maxId + 1, this.typeBox.getSelectedItem().toString(), amount, this.categoryBox.getSelectedItem().toString(), "Quick Add", LocalDate.now());
            
            javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    transactionService.addTransaction(username, newTx);
                    return null;
                }

                @Override
                protected void done() {
                    amountField.setText("");
                    refreshDashboard();
                    JOptionPane.showMessageDialog(DashboardPanel.this, "Transaction added successfully!", "Success", 1);
                }
            };
            worker.execute();
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Error", 0);
        }
    }

    private void refreshDashboard() {
        javax.swing.SwingWorker<Map<String, Object>, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() {
                List<Transaction> transactions = transactionService.loadTransactions(username);
                LoanService loanService = new LoanService();
                List<Loan> loans = loanService.loadLoans(username);
                
                java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                map.put("transactions", transactions);
                map.put("loans", loans);
                return map;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> map = get();
                    @SuppressWarnings("unchecked")
                    List<Transaction> transactions = (List<Transaction>) map.get("transactions");
                    @SuppressWarnings("unchecked")
                    List<Loan> loans = (List<Loan>) map.get("loans");
                    
                    allTransactions = transactions;
                    double income = 0.0;
                    double expense = 0.0;
                    for (Transaction t : allTransactions) {
                        if ("Income".equalsIgnoreCase(t.getType())) {
                            income += t.getAmount();
                            continue;
                        }
                        expense += t.getAmount();
                    }
                    double balance = income - expense;
                    double netPendingLoans = 0.0;
                    for (Loan l : loans) {
                        if (!"Active".equalsIgnoreCase(l.getStatus())) continue;
                        double remaining = l.getRemainingAmount();
                        if ("Given".equalsIgnoreCase(l.getType())) {
                            netPendingLoans += remaining;
                            continue;
                        }
                        netPendingLoans -= remaining;
                    }
                    incomeLabel.setText(String.format("Income: \u20b9%,.2f", income));
                    expenseLabel.setText(String.format("Expense: \u20b9%,.2f", expense));
                    balanceLabel.setText(String.format("Balance: \u20b9%,.2f", balance));
                    if (netPendingLoans >= 0.0) {
                        loansLabel.setText(String.format("Loans: +\u20b9%,.2f", netPendingLoans));
                    } else {
                        loansLabel.setText(String.format("Loans: -\u20b9%,.2f", Math.abs(netPendingLoans)));
                    }
                    boolean dark = ThemeUtil.isDarkMode();
                    Color loansColor = dark ? new Color(179, 157, 219) : new Color(103, 58, 183);
                    Color loansBorder = dark ? new Color(75, 60, 110) : new Color(209, 196, 233);
                    loansLabel.setForeground(loansColor);
                    loansLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(loansBorder, 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
                    int score = 100;
                    if (income == 0.0) {
                        scoreBar.setValue(0);
                        scoreLabel.setText("Score: 0/100");
                        statusLabel.setText("Status: No Income");
                    } else {
                        double savingsPercent = balance / income * 100.0;
                        if (savingsPercent < 10.0) {
                            score -= 30;
                        } else if (savingsPercent < 20.0) {
                            score -= 20;
                        }
                        score = Math.max(score, 0);
                        scoreBar.setValue(score);
                        scoreLabel.setText("Score: " + score + "/100");
                        String statusWord = savingsPercent >= 40.0 ? "Excellent" : (savingsPercent >= 20.0 ? "Good" : "Poor");
                        statusLabel.setText("Status: " + statusWord);
                    }
                    tableModel.setRowCount(0);
                    int startIndex = Math.max(0, allTransactions.size() - 5);
                    for (int i = allTransactions.size() - 1; i >= startIndex; --i) {
                        Transaction t = allTransactions.get(i);
                        tableModel.addRow(new Object[]{t.getType(), String.format("\u20b9%,.2f", t.getAmount()), t.getCategory(), t.getDate()});
                    }
                    Map<String, Double> spentMap = budgetService.calculateCategoryExpenses(allTransactions);
                    StringBuilder alerts = new StringBuilder();
                    for (String category : spentMap.keySet()) {
                        double spent = spentMap.get(category).doubleValue();
                        String alert = budgetService.getBudgetAlert(category, spent);
                        if (alert == null) continue;
                        alerts.append(alert).append("\n");
                    }
                    String insights = insightService.generateInsights(allTransactions);
                    insightArea.setText(insights);
                    StringBuilder notificationContent = new StringBuilder();
                    if (alerts.length() > 0) {
                        notificationContent.append("--- BUDGET ALERTS ---\n").append((CharSequence)alerts).append("\n");
                        notificationArea.setForeground(new Color(198, 40, 40));
                    } else {
                        notificationArea.setForeground(new Color(60, 60, 60));
                    }
                    notificationContent.append("--- SMART INSIGHTS ---\n").append(insights);
                    notificationArea.setText(notificationContent.toString());
                } catch (Exception e) {
                    System.out.println("Error loading dashboard data: " + e.getMessage());
                }
            }
        };
        worker.execute();
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
        if (this.getParent() instanceof JViewport) {
            return this.getParent().getWidth() > this.getPreferredSize().width;
        }
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
