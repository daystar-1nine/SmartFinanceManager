package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import model.Loan;
import model.Transaction;
import service.BudgetService;
import service.InsightService;
import service.LoanService;
import service.TransactionService;
import util.Constants;
import util.ThemeUtil;

/**
 * <h2>DashboardPanel</h2>
 * <p>
 * This panel represents the primary dashboard view of the Smart Finance Manager.
 * It integrates visual financial indicators, budget status gauges, transaction input cards,
 * dynamic transaction tables, and natural language diagnostic panels.
 * </p>
 * 
 * <h3>Architecture Role:</h3>
 * <p>
 * Part of the <b>UI (Presentation) Layer</b>. It receives injected services via constructor
 * Dependency Injection (DI) and coordinates event bindings, SwingWorker asynchronous loads,
 * and component state updates.
 * </p>
 * 
 * <h3>Key UI Design Aspects:</h3>
 * <ul>
 *   <li><b>Grid & Border Layouts:</b> Uses BorderLayout and multi-grid sub-panels for responsive desktop placement.</li>
 *   <li><b>Custom Summary Cards:</b> Renders 4 rounded accent cards for Income, Expenses, Balance, and Health Score.</li>
 *   <li><b>Dynamic JTable:</b> Renders zebra rows, soft status colors (Green/Red), and cursor-tracked hover highlighting.</li>
 *   <li><b>EDT Safety:</b> Offloads file system querying to background `SwingWorker` threads to prevent UI hangs.</li>
 * </ul>
 * 
 * @see javax.swing.JPanel
 * @see TransactionService
 * @see BudgetService
 * @see LoanService
 */
@SuppressWarnings({"serial", "this-escape"})
public class DashboardPanel extends JPanel implements Scrollable {

    // Injected Business Services
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final LoanService loanService;
    private final InsightService insightService;
    
    // User Context
    private final String username;
    
    /**
     * Local memory copy of user transaction ledger.
     */
    private List<Transaction> allTransactions = new ArrayList<>();
    
    // Original summary labels mapped to cards (for backwards compatibility)
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JLabel loansLabel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JProgressBar scoreBar;
    
    // Quick Add controls
    private JTextField amountField;
    private JComboBox<String> typeBox;
    private JComboBox<String> categoryBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea notificationArea;
    private JTextArea insightArea;
    
    // Layout containers
    private JPanel scorePanel;
    private RoundedCardPanel quickAddPanel;
    private JPanel recentTablePanel;
    private JPanel notificationsPanel;
    private JPanel insightsPanel;
    
    private JLabel amountLabel;
    private JLabel typeLabel;
    private JLabel categoryLabel;
    private JButton quickAddBtn;

    // Redesigned components
    private SummaryCard incomeCard;
    private SummaryCard expenseCard;
    private SummaryCard balanceCard;
    private SummaryCard scoreCard;
    private JScrollPane tableScrollPane;
    private JPanel emptyStatePanel;
    
    /**
     * Stores the row index currently hovered by the user's cursor.
     */
    private int hoveredRow = -1;

    /**
     * Constructs the DashboardPanel, binding layouts, building sub-sections,
     * and launching background loaders.
     * 
     * @param username The currently authenticated user.
     * @param transactionService Injected Transaction Service.
     * @param budgetService Injected Budget Service.
     * @param loanService Injected Loan Service.
     */
    public DashboardPanel(String username, TransactionService transactionService, BudgetService budgetService, LoanService loanService) {
        this.username = username;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.loanService = loanService;
        this.insightService = new InsightService();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(ThemeUtil.getBackgroundColor());

        // NORTH: Welcome Header + Summary Cards
        add(createSummaryPanel(), BorderLayout.NORTH);

        // CENTER: Main Content (Quick Action Panel + Scrollable JTable)
        JPanel centerContainer = new JPanel(new BorderLayout(15, 15));
        centerContainer.setOpaque(false);
        centerContainer.add(createQuickActionPanel(), BorderLayout.NORTH);
        centerContainer.add(createTablePanel(), BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // EAST: Notice board style notifications panel
        add(createNotificationsPanel(), BorderLayout.EAST);

        // SOUTH: Natural language insights panel
        add(createInsightsPanel(), BorderLayout.SOUTH);

        // Populate widgets with asynchronous loaders
        refreshDashboard();
        ThemeUtil.applyTheme(this);
        updateCardColors();
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Personalization Header (Welcome + Theme Toggle)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + " 👋");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setForeground(ThemeUtil.getTextColor());

        JButton themeToggleBtn = new JButton(ThemeUtil.isDarkMode() ? "☀️ Light Mode" : "🌙 Dark Mode");
        themeToggleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        themeToggleBtn.setBackground(ThemeUtil.isDarkMode() ? new Color(50, 50, 70) : new Color(240, 240, 240));
        themeToggleBtn.setForeground(ThemeUtil.getTextColor());

        addHoverEffect(themeToggleBtn,
            ThemeUtil.isDarkMode() ? new Color(50, 50, 70) : new Color(240, 240, 240),
            ThemeUtil.isDarkMode() ? new Color(70, 70, 90) : new Color(220, 220, 220),
            ThemeUtil.getTextColor(),
            ThemeUtil.getTextColor()
        );

        themeToggleBtn.addActionListener(e -> {
            ThemeUtil.setDarkMode(!ThemeUtil.isDarkMode());
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                ThemeUtil.applyTheme(topFrame);
                themeToggleBtn.setText(ThemeUtil.isDarkMode() ? "☀️ Light Mode" : "🌙 Dark Mode");
                themeToggleBtn.setBackground(ThemeUtil.isDarkMode() ? new Color(50, 50, 70) : new Color(240, 240, 240));
                themeToggleBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                updateCardColors();
                topFrame.repaint();
            }
        });

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(themeToggleBtn, BorderLayout.EAST);

        // Grid of 4 Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);

        incomeCard = new SummaryCard("income");
        expenseCard = new SummaryCard("expense");
        balanceCard = new SummaryCard("balance");
        scoreCard = new SummaryCard("score");

        cardsPanel.add(incomeCard);
        cardsPanel.add(expenseCard);
        cardsPanel.add(balanceCard);
        cardsPanel.add(scoreCard);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(cardsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQuickActionPanel() {
        quickAddPanel = new RoundedCardPanel(12);
        quickAddPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        quickAddPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        quickAddPanel.setCustomBackground(ThemeUtil.getCardBackgroundColor());

        JLabel title = new JLabel("⚡ Quick Add:");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ThemeUtil.getTextColor());
        quickAddPanel.add(title);

        amountLabel = new JLabel("Amount (₹):");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        amountField = new JTextField(8);
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
        typeBox.setFont(new Font("SansSerif", Font.PLAIN, 12));

        categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryBox = new JComboBox<>(Constants.CATEGORIES);
        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 12));

        quickAddBtn = new JButton("Add Transaction");
        quickAddBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        quickAddBtn.setFocusPainted(false);
        quickAddBtn.setBackground(ThemeUtil.isDarkMode() ? new Color(50, 50, 70) : new Color(240, 240, 240));
        quickAddBtn.setForeground(ThemeUtil.getTextColor());
        quickAddBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        addHoverEffect(quickAddBtn,
            ThemeUtil.isDarkMode() ? new Color(50, 50, 70) : new Color(240, 240, 240),
            ThemeUtil.isDarkMode() ? new Color(70, 70, 90) : new Color(220, 220, 220),
            ThemeUtil.getTextColor(),
            ThemeUtil.getTextColor()
        );

        quickAddBtn.addActionListener(e -> quickAddTransaction());

        quickAddPanel.add(amountLabel);
        quickAddPanel.add(amountField);
        quickAddPanel.add(typeLabel);
        quickAddPanel.add(typeBox);
        quickAddPanel.add(categoryLabel);
        quickAddPanel.add(categoryBox);
        quickAddPanel.add(quickAddBtn);

        return quickAddPanel;
    }

    private JPanel createTablePanel() {
        recentTablePanel = new JPanel(new BorderLayout());
        recentTablePanel.setOpaque(false);

        tableModel = new DefaultTableModel(new String[]{"Type", "Amount", "Category", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                boolean selected = isRowSelected(row);
                boolean dark = ThemeUtil.isDarkMode();

                if (selected) {
                    comp.setBackground(dark ? new Color(60, 60, 90) : new Color(187, 222, 251));
                    comp.setForeground(ThemeUtil.getTextColor());
                } else if (row == hoveredRow) {
                    comp.setBackground(dark ? new Color(50, 50, 70) : new Color(240, 240, 255));
                    comp.setForeground(ThemeUtil.getTextColor());
                } else {
                    try {
                        String type = getValueAt(row, 0).toString();
                        if ("Income".equalsIgnoreCase(type)) {
                            comp.setBackground(dark ? new Color(25, 45, 30) : new Color(230, 245, 230));
                            comp.setForeground(dark ? new Color(165, 214, 167) : new Color(46, 125, 50));
                        } else {
                            comp.setBackground(dark ? new Color(45, 25, 25) : new Color(255, 230, 230));
                            comp.setForeground(dark ? new Color(239, 154, 154) : new Color(198, 40, 40));
                        }
                    } catch (Exception e) {
                        comp.setBackground(row % 2 == 0 ? ThemeUtil.getCardBackgroundColor() : ThemeUtil.getBackgroundColor());
                        comp.setForeground(ThemeUtil.getTextColor());
                    }
                }
                return comp;
            }
        };

        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Center alignment renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Row Hover Interaction
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(500, 160));
        tableScrollPane.setBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true));

        // Setup Empty State Panel
        emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel emptyIcon = new JLabel("📭");
        emptyIcon.setFont(new Font("SansSerif", Font.PLAIN, 40));
        emptyStatePanel.add(emptyIcon, gbc);

        gbc.gridy = 1;
        JLabel emptyTitle = new JLabel("No transactions yet");
        emptyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        emptyTitle.setForeground(ThemeUtil.getTextColor());
        emptyStatePanel.add(emptyTitle, gbc);

        gbc.gridy = 2;
        JLabel emptyDesc = new JLabel("Start by adding your first entry using the Quick Add form!");
        emptyDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emptyDesc.setForeground(ThemeUtil.getSecondaryTextColor());
        emptyStatePanel.add(emptyDesc, gbc);

        recentTablePanel.add(tableScrollPane, BorderLayout.CENTER);
        return recentTablePanel;
    }

    private JPanel createNotificationsPanel() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setPreferredSize(new Dimension(260, 0));
        notificationsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
            "Notifications",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            ThemeUtil.getTextColor()
        ));

        notificationArea = new JTextArea();
        notificationArea.setEditable(false);
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);
        notificationArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        notificationArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(notificationArea);
        scroll.setBorder(null);
        notificationsPanel.add(scroll, BorderLayout.CENTER);
        return notificationsPanel;
    }

    private JPanel createInsightsPanel() {
        insightsPanel = new JPanel(new BorderLayout());
        insightsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
            "Insights & Analytics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            ThemeUtil.getTextColor()
        ));

        insightArea = new JTextArea(3, 40);
        insightArea.setEditable(false);
        insightArea.setLineWrap(true);
        insightArea.setWrapStyleWord(true);
        insightArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        insightArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(insightArea);
        scroll.setBorder(null);
        insightsPanel.add(scroll, BorderLayout.CENTER);
        return insightsPanel;
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
                    incomeLabel.setText(String.format("₹%,.2f", income));
                    expenseLabel.setText(String.format("₹%,.2f", expense));
                    balanceLabel.setText(String.format("₹%,.2f", balance));
                    
                    if (loansLabel != null) {
                        if (netPendingLoans >= 0.0) {
                            loansLabel.setText(String.format("Loans: +₹%,.2f", netPendingLoans));
                        } else {
                            loansLabel.setText(String.format("Loans: -₹%,.2f", Math.abs(netPendingLoans)));
                        }
                    }

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

                    // Populate JTable
                    tableModel.setRowCount(0);
                    int startIndex = Math.max(0, allTransactions.size() - 5);
                    for (int i = allTransactions.size() - 1; i >= startIndex; --i) {
                        Transaction t = allTransactions.get(i);
                        tableModel.addRow(new Object[]{t.getType(), String.format("₹%,.2f", t.getAmount()), t.getCategory(), t.getDate()});
                    }

                    // Swap Empty State View
                    if (allTransactions.isEmpty()) {
                        tableScrollPane.setViewportView(emptyStatePanel);
                    } else {
                        tableScrollPane.setViewportView(table);
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
                        notificationContent.append("⚠️ BUDGET ALERTS ⚠️\n").append((CharSequence)alerts).append("\n");
                        notificationArea.setForeground(new Color(198, 40, 40));
                    } else {
                        notificationArea.setForeground(ThemeUtil.getTextColor());
                    }
                    notificationContent.append("💡 SMART INSIGHTS 💡\n").append(insights);
                    notificationArea.setText(notificationContent.toString());
                } catch (Exception e) {
                    System.out.println("Error loading dashboard data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCardColors() {
        setBackground(ThemeUtil.getBackgroundColor());
        if (incomeCard != null) incomeCard.updateColors();
        if (expenseCard != null) expenseCard.updateColors();
        if (balanceCard != null) balanceCard.updateColors();
        if (scoreCard != null) scoreCard.updateColors();

        if (quickAddPanel != null) {
            quickAddPanel.setBackground(ThemeUtil.getCardBackgroundColor());
            quickAddPanel.setCustomBackground(ThemeUtil.getCardBackgroundColor());
            for (Component child : quickAddPanel.getComponents()) {
                if (child instanceof JLabel) {
                    child.setForeground(ThemeUtil.getTextColor());
                }
            }
        }
        if (recentTablePanel != null) {
            recentTablePanel.setBackground(ThemeUtil.getCardBackgroundColor());
        }
        if (emptyStatePanel != null) {
            for (Component child : emptyStatePanel.getComponents()) {
                if (child instanceof JLabel) {
                    JLabel lbl = (JLabel) child;
                    if (lbl.getFont().isBold()) {
                        lbl.setForeground(ThemeUtil.getTextColor());
                    } else {
                        lbl.setForeground(ThemeUtil.getSecondaryTextColor());
                    }
                }
            }
        }
        if (notificationsPanel != null) {
            notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
                "Notifications",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                ThemeUtil.getTextColor()
            ));
        }
        if (insightsPanel != null) {
            insightsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true),
                "Insights & Analytics",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                ThemeUtil.getTextColor()
            ));
        }
        if (tableScrollPane != null) {
            tableScrollPane.setBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true));
        }
    }

    private void addHoverEffect(JButton button, Color normalBg, Color hoverBg, Color normalFg, Color hoverFg) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
                button.setForeground(hoverFg);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalBg);
                button.setForeground(normalFg);
            }
        });
    }

    private Color getIncomeCardBg() {
        return ThemeUtil.isDarkMode() ? new Color(27, 62, 37) : new Color(232, 245, 233);
    }
    private Color getIncomeCardFg() {
        return ThemeUtil.isDarkMode() ? new Color(165, 214, 167) : new Color(46, 125, 50);
    }
    private Color getExpenseCardBg() {
        return ThemeUtil.isDarkMode() ? new Color(74, 30, 32) : new Color(255, 235, 238);
    }
    private Color getExpenseCardFg() {
        return ThemeUtil.isDarkMode() ? new Color(239, 154, 154) : new Color(198, 40, 40);
    }
    private Color getBalanceCardBg() {
        return ThemeUtil.isDarkMode() ? new Color(24, 53, 92) : new Color(227, 242, 253);
    }
    private Color getBalanceCardFg() {
        return ThemeUtil.isDarkMode() ? new Color(144, 202, 249) : new Color(21, 101, 192);
    }
    private Color getScoreCardBg() {
        return ThemeUtil.isDarkMode() ? new Color(56, 31, 71) : new Color(243, 229, 245);
    }
    private Color getScoreCardFg() {
        return ThemeUtil.isDarkMode() ? new Color(206, 147, 216) : new Color(106, 27, 154);
    }

    // ---------- JTABLE SCROLLABLE INTERFACE ----------
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

    // ---------- HELPER CLASS FOR SOLID ROUNDED JPANEL ----------
    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private Color customBgColor;

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setCustomBackground(Color color) {
            this.customBgColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(customBgColor != null ? customBgColor : getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // ---------- SUMMARY CARD INNER CLASS ----------
    private class SummaryCard extends RoundedCardPanel {
        private final String type;
        private JLabel cardIcon;
        private JLabel cardTitleLabel;
        private JLabel cardValueLabel;
        private JLabel cardSubLabel; 
        private JPanel innerScorePanel; 

        public SummaryCard(String type) {
            super(16);
            this.type = type;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

            cardIcon = new JLabel();
            cardIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));
            cardIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

            cardTitleLabel = new JLabel();
            cardTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cardTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            cardValueLabel = new JLabel();
            cardValueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            cardValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(cardIcon);
            add(Box.createVerticalStrut(4));
            add(cardTitleLabel);
            add(Box.createVerticalStrut(4));

            if ("income".equals(type)) {
                cardIcon.setText("📥");
                cardTitleLabel.setText("Total Income");
                incomeLabel = cardValueLabel;
                add(cardValueLabel);
            } else if ("expense".equals(type)) {
                cardIcon.setText("📤");
                cardTitleLabel.setText("Total Expense");
                expenseLabel = cardValueLabel;
                add(cardValueLabel);
            } else if ("balance".equals(type)) {
                cardIcon.setText("💰");
                cardTitleLabel.setText("Net Balance");
                balanceLabel = cardValueLabel;
                add(cardValueLabel);

                cardSubLabel = new JLabel("Loans: ₹0.00");
                cardSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
                cardSubLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                loansLabel = cardSubLabel;
                add(Box.createVerticalStrut(4));
                add(cardSubLabel);
            } else if ("score".equals(type)) {
                cardIcon.setText("🛡️");
                cardTitleLabel.setText("Health Score");

                innerScorePanel = new JPanel(new BorderLayout());
                innerScorePanel.setOpaque(false);
                innerScorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                scoreLabel = new JLabel("Score: 0/100");
                scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                scoreLabel.setName("customColorLabel");

                statusLabel = new JLabel("Status: -");
                statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
                statusLabel.setName("customColorLabel");

                innerScorePanel.add(scoreLabel, BorderLayout.WEST);
                innerScorePanel.add(statusLabel, BorderLayout.EAST);

                scoreBar = new JProgressBar(0, 100);
                scoreBar.setStringPainted(true);
                scoreBar.setFont(new Font("SansSerif", Font.BOLD, 9));
                scoreBar.setAlignmentX(Component.LEFT_ALIGNMENT);

                add(innerScorePanel);
                add(Box.createVerticalStrut(6));
                add(scoreBar);
            }

            updateColors();
        }

        public void updateColors() {
            Color bg, fg;
            switch (type) {
                case "income":
                    bg = getIncomeCardBg();
                    fg = getIncomeCardFg();
                    break;
                case "expense":
                    bg = getExpenseCardBg();
                    fg = getExpenseCardFg();
                    break;
                case "balance":
                    bg = getBalanceCardBg();
                    fg = getBalanceCardFg();
                    break;
                case "score":
                    bg = getScoreCardBg();
                    fg = getScoreCardFg();
                    break;
                default:
                    bg = ThemeUtil.getCardBackgroundColor();
                    fg = ThemeUtil.getTextColor();
            }
            setCustomBackground(bg);

            cardIcon.setForeground(fg);
            cardTitleLabel.setForeground(ThemeUtil.isDarkMode() ? ThemeUtil.DARK_SECONDARY_TEXT : ThemeUtil.LIGHT_SECONDARY_TEXT);
            cardValueLabel.setForeground(fg);

            if (cardSubLabel != null) {
                cardSubLabel.setForeground(fg);
            }
            if (innerScorePanel != null) {
                scoreLabel.setForeground(fg);
                statusLabel.setForeground(fg);
            }
        }
    }
}
