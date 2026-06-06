package ui;

import model.Transaction;
import service.TransactionService;
import util.ThemeUtil;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportPanel
 * -----------
 * Provides a clean and analytical user interface for financial reporting.
 * Allows date, type, and category filtering, summary calculations,
 * category breakdowns, and smart rule-based insights.
 */
public class ReportPanel extends JPanel implements Scrollable {

    // ================= SERVICES =================
    private final TransactionService transactionService;
    private final String username;

    // ================= DATA =================
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();

    // ================= UI COMPONENTS =================
    private JTextField fromDateField, toDateField;
    private JComboBox<String> typeBox, categoryBox;

    private JLabel totalIncomeLabel, totalExpenseLabel, netBalanceLabel;

    private JTable table;
    private DefaultTableModel tableModel;

    private JPanel breakdownContentPanel;
    private JTextArea insightsArea;

    // Palette for Category breakdown indicator dots
    private static final Map<String, Color> COLOR_MAP = Map.of(
            "Food", new Color(255, 107, 107),
            "Transport", new Color(77, 150, 255),
            "Shopping", new Color(255, 217, 61),
            "Bills", new Color(107, 203, 119),
            "Other", new Color(155, 93, 229)
    );
    private static final Color DEFAULT_COLOR = new Color(170, 170, 170);

    public ReportPanel(String username) {
        this.username = username;
        this.transactionService = new TransactionService();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // NORTH: Filters Panel
        add(createFiltersPanel(), BorderLayout.NORTH);

        // CENTER: Summary Panel + (Table + Category Breakdown)
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setBackground(Color.WHITE);

        centerContainer.add(createSummaryPanel(), BorderLayout.NORTH);

        // Sub-panel containing Table (Left) and Breakdown (Right)
        JPanel dataPanel = new JPanel(new BorderLayout(15, 0));
        dataPanel.setBackground(Color.WHITE);
        dataPanel.add(createTablePanel(), BorderLayout.CENTER);
        dataPanel.add(createCategoryBreakdownPanel(), BorderLayout.EAST);

        centerContainer.add(dataPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // SOUTH: Insights Panel
        add(createInsightsPanel(), BorderLayout.SOUTH);

        // Initialize values
        resetFilters();

        // Apply active theme colors recursively
        ThemeUtil.applyTheme(this);
    }

    // ================= FILTERS PANEL (NORTH) =================
    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filtersPanel.setBackground(Color.WHITE);
        filtersPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);

        typeBox = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        categoryBox = new JComboBox<>();
        categoryBox.addItem("All");
        for (String cat : Constants.CATEGORIES) {
            categoryBox.addItem(cat);
        }

        JButton applyBtn = new JButton("Apply Filter");
        applyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        applyBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        resetBtn.addActionListener(e -> resetFilters());

        JLabel fromDateLabel = new JLabel("From (YYYY-MM-DD):");
        JLabel toDateLabel = new JLabel("To (YYYY-MM-DD):");
        JLabel typeLabel = new JLabel("Type:");
        JLabel categoryLabel = new JLabel("Category:");

        filtersPanel.add(fromDateLabel);
        filtersPanel.add(fromDateField);
        filtersPanel.add(toDateLabel);
        filtersPanel.add(toDateField);
        filtersPanel.add(typeLabel);
        filtersPanel.add(typeBox);
        filtersPanel.add(categoryLabel);
        filtersPanel.add(categoryBox);
        filtersPanel.add(applyBtn);
        filtersPanel.add(resetBtn);

        return filtersPanel;
    }

    // ================= SUMMARY PANEL =================
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        totalIncomeLabel = new JLabel("Total Income: ₹0.00", SwingConstants.CENTER);
        totalIncomeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalIncomeLabel.setForeground(new Color(46, 125, 50)); // Green
        totalIncomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalExpenseLabel = new JLabel("Total Expense: ₹0.00", SwingConstants.CENTER);
        totalExpenseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalExpenseLabel.setForeground(new Color(198, 40, 40)); // Red
        totalExpenseLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        netBalanceLabel = new JLabel("Net Balance: ₹0.00", SwingConstants.CENTER);
        netBalanceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        netBalanceLabel.setForeground(new Color(21, 101, 192)); // Blue
        netBalanceLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        summaryPanel.add(totalIncomeLabel);
        summaryPanel.add(totalExpenseLabel);
        summaryPanel.add(netBalanceLabel);

        return summaryPanel;
    }

    // ================= TRANSACTIONS TABLE PANEL =================
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transactions"));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Type", "Amount", "Category", "Note", "Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(520, 200));

        // Center table properly
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(scrollPane, gbc);

        return tablePanel;
    }

    // ================= CATEGORY BREAKDOWN PANEL =================
    private JPanel createCategoryBreakdownPanel() {
        JPanel breakdownPanel = new JPanel(new BorderLayout());
        breakdownPanel.setPreferredSize(new Dimension(240, 0));
        breakdownPanel.setBackground(Color.WHITE);
        breakdownPanel.setBorder(BorderFactory.createTitledBorder("Category Breakdown"));

        breakdownContentPanel = new JPanel();
        breakdownContentPanel.setLayout(new BoxLayout(breakdownContentPanel, BoxLayout.Y_AXIS));
        breakdownContentPanel.setBackground(Color.WHITE);
        breakdownContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(breakdownContentPanel);
        scroll.setBorder(null);
        breakdownPanel.add(scroll, BorderLayout.CENTER);

        return breakdownPanel;
    }

    // ================= INSIGHTS PANEL (SOUTH) =================
    private JPanel createInsightsPanel() {
        JPanel insightsPanel = new JPanel(new BorderLayout());
        insightsPanel.setBackground(Color.WHITE);
        insightsPanel.setBorder(BorderFactory.createTitledBorder("Insights"));

        insightsArea = new JTextArea(4, 50);
        insightsArea.setEditable(false);
        insightsArea.setLineWrap(true);
        insightsArea.setWrapStyleWord(true);
        insightsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        insightsArea.setBackground(new Color(245, 247, 250));
        insightsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        insightsPanel.add(new JScrollPane(insightsArea), BorderLayout.CENTER);

        return insightsPanel;
    }

    // ================= ACTIONS & LOGIC =================
    private void applyFilters() {
        LocalDate fromDate;
        LocalDate toDate;

        try {
            fromDate = LocalDate.parse(fromDateField.getText().trim());
            toDate = LocalDate.parse(toDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fromDate.isAfter(toDate)) {
            JOptionPane.showMessageDialog(this, "'From' date cannot be after 'To' date", "Invalid Range", JOptionPane.ERROR_MESSAGE);
            return;
        }

        filterAndRefresh(fromDate, toDate);
    }

    private void resetFilters() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        fromDateField.setText(start.toString());
        toDateField.setText(end.toString());
        typeBox.setSelectedIndex(0);
        categoryBox.setSelectedIndex(0);

        filterAndRefresh(start, end);
    }

    private void filterAndRefresh(LocalDate from, LocalDate to) {
        allTransactions = transactionService.loadTransactions(username);
        filteredTransactions.clear();

        int typeIdx = typeBox.getSelectedIndex();
        int catIdx = categoryBox.getSelectedIndex();

        for (Transaction t : allTransactions) {
            LocalDate tDate = t.getDate();
            boolean dateMatch = (tDate.isEqual(from) || tDate.isAfter(from)) && (tDate.isEqual(to) || tDate.isBefore(to));
            boolean typeMatch = (typeIdx <= 0) || 
                                (typeIdx == 1 && "Income".equalsIgnoreCase(t.getType())) ||
                                (typeIdx == 2 && "Expense".equalsIgnoreCase(t.getType()));
            boolean catMatch = (catIdx <= 0) || 
                               t.getCategory().equalsIgnoreCase(Constants.CATEGORIES[catIdx - 1]);

            if (dateMatch && typeMatch && catMatch) {
                filteredTransactions.add(t);
            }
        }

        updateUIComponents();
    }

    private void updateUIComponents() {
        double income = 0;
        double expense = 0;

        // Reset Table
        tableModel.setRowCount(0);

        for (Transaction t : filteredTransactions) {
            if ("Income".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }

            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getType(),
                    String.format("₹%,.2f", t.getAmount()),
                    t.getCategory(),
                    t.getNote(),
                    t.getDate()
            });
        }

        // 1. Update Summary Card Values
        totalIncomeLabel.setText(String.format("Total Income: ₹%,.2f", income));
        totalExpenseLabel.setText(String.format("Total Expense: ₹%,.2f", expense));
        netBalanceLabel.setText(String.format("Net Balance: ₹%,.2f", (income - expense)));

        // 2. Update Category Breakdown (Right panel)
        updateCategoryBreakdown();

        // 3. Update Smart Insights (Bottom panel)
        updateInsights(income, expense);
    }

    private void updateCategoryBreakdown() {
        breakdownContentPanel.removeAll();

        Map<String, Double> categorySums = new HashMap<>();
        // Initialize all standard categories
        for (String cat : new String[]{"Food", "Transport", "Shopping", "Bills", "Other"}) {
            categorySums.put(cat, 0.0);
        }

        double totalExpense = 0.0;
        for (Transaction t : filteredTransactions) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                String cat = t.getCategory();
                double amt = t.getAmount();
                categorySums.put(cat, categorySums.getOrDefault(cat, 0.0) + amt);
                totalExpense += amt;
            }
        }

        // Create list of indicators dynamically
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            String cat = entry.getKey();
            double amt = entry.getValue();
            double percent = totalExpense > 0 ? (amt / totalExpense) * 100 : 0;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(220, 25));

            // Custom indicator dot
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(COLOR_MAP.getOrDefault(cat, DEFAULT_COLOR));
                    g2d.fillOval(3, 8, 8, 8);
                }
            };
            dot.setPreferredSize(new Dimension(15, 25));
            dot.setBackground(Color.WHITE);

            JLabel label = new JLabel(String.format("%s: ₹%,.2f (%.0f%%)", cat, amt, percent));
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));

            row.add(dot, BorderLayout.WEST);
            row.add(label, BorderLayout.CENTER);

            breakdownContentPanel.add(row);
            breakdownContentPanel.add(Box.createVerticalStrut(5)); // spacer
        }

        breakdownContentPanel.revalidate();
        breakdownContentPanel.repaint();
    }

    private void updateInsights(double income, double expense) {
        StringBuilder insights = new StringBuilder();

        insights.append(String.format("• Filtered Summary: Total earnings are ₹%,.2f with expenses totaling ₹%,.2f.\n", income, expense));

        if (income > 0) {
            double ratio = (expense / income) * 100;
            insights.append(String.format("• Savings Ratio: You spent %.1f%% of your earnings in this filtered range.\n", ratio));
            if (ratio > 80) {
                insights.append("  ⚠️ High spending relative to income in this period. Consider limiting non-essential expenses.\n");
            } else if (ratio < 40) {
                insights.append("  ✅ Strong financial health! Your savings rate is excellent.\n");
            }
        }

        // Calculate maximum category expense
        Map<String, Double> categorySums = new HashMap<>();
        for (Transaction t : filteredTransactions) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                categorySums.put(t.getCategory(), categorySums.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }

        String maxCategory = null;
        double maxAmt = 0.0;
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            if (entry.getValue() > maxAmt) {
                maxAmt = entry.getValue();
                maxCategory = entry.getKey();
            }
        }

        if (maxCategory != null && maxAmt > 0) {
            insights.append(String.format("• Spending Peak: Your largest expense category is %s at ₹%,.2f.\n", maxCategory, maxAmt));
            if (maxCategory.equalsIgnoreCase("Food") && income > 0 && (maxAmt / income) > 0.25) {
                insights.append("  🍔 Food costs represent more than 25% of your income. Preparing home-cooked meals could yield easy savings.\n");
            } else if (maxCategory.equalsIgnoreCase("Bills") && maxAmt > (income * 0.40)) {
                insights.append("  💡 Fixed bills are occupying a large portion of your income. Look into subscription audits or rate plans.\n");
            }
        }

        if (insights.length() == 0 || filteredTransactions.isEmpty()) {
            insights.append("• No transaction history fits the selected filters. Change filter controls above to view statistics!");
        }

        insightsArea.setText(insights.toString());
    }

    // ================= SCROLLABLE IMPLEMENTATION =================
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
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
        if (getParent() instanceof JViewport) {
            return getParent().getWidth() > getPreferredSize().width;
        }
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return getParent().getHeight() > getPreferredSize().height;
        }
        return false;
    }
}
