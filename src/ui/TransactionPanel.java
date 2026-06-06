package ui;

import java.util.HashMap;
import java.util.Map;


//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.general.DefaultPieDataset;

import model.Transaction;
import service.TransactionService;
import service.ReportService;
import service.BudgetService;
import java.util.Map;
import service.InsightService;
import util.ThemeUtil;
import util.Constants;
import util.AlertUtil;
import util.CSVUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>TransactionPanel</h2>
 * <p>
 * This class represents the interactive transaction ledger interface of the Smart Finance Manager.
 * It provides users with controls to create, update, delete, search, and export transaction lists.
 * </p>
 * 
 * <h3>Architecture Role:</h3>
 * <p>
 * Fits into the <b>UI (Presentation) Layer</b>. It binds action listeners to input fields
 * and table records, routing mutation operations directly to {@link TransactionService}
 * and {@link BudgetService}.
 * </p>
 * 
 * <h3>Key Features & Systems:</h3>
 * <ul>
 *   <li><b>Data Ledger Table:</b> Displays lists of transactions with colored text highlights and mouse-event hover listeners.</li>
 *   <li><b>Input Sanitization:</b> Uses {@link util.CSVUtil} to strip commas from raw user notes before serializing to disk.</li>
 *   <li><b>Undo Support:</b> Captures deleted transactions to enable transient "Undo Delete" recovery.</li>
 *   <li><b>Export Engine:</b> Outputs current transaction list to formatted plain text (.txt) or comma-separated CSV spreadsheets.</li>
 * </ul>
 * 
 * @see javax.swing.JPanel
 * @see TransactionService
 * @see BudgetService
 * @see InsightService
 */
@SuppressWarnings({"serial", "this-escape"})
public class TransactionPanel extends JPanel implements Scrollable {

    // ================= SERVICES =================
    private InsightService insightService;

    // ================= INPUT COMPONENTS =================
    private JTextField amountField, noteField;
    private JComboBox<String> typeBox, categoryBox;

    // ================= FILTER COMPONENTS =================
    private JTextField searchField;
    private JComboBox<String> filterCategoryBox;

    // ================= TABLE COMPONENTS =================
    private JTable table;
    private DefaultTableModel tableModel;

    // ================= DATA LOGISTICS =================
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionService transactionService;
    private String username;
    private int transactionId = 1;

    // ================= SUMMARY WIDGETS =================
    private JLabel incomeLabel, expenseLabel, balanceLabel;

    // ================= HEALTH SCORE COMPONENTS =================
    private JLabel scoreLabel, statusLabel;
    private JProgressBar scoreBar;

    // ================= UNDO CACHE =================
    private Transaction lastDeletedTransaction;
    private int lastDeletedIndex;

    // ================= NOTIFICATION WIDGETS =================
    private JTextArea notificationArea;
    private BudgetService budgetService;

    // ================= INSIGHTS & GRAPHS =================
    private JTextArea insightArea;
    private PieChartPanel pieChartPanel;

    // ================= MULTI-LANGUAGE REFRESHABLE WIDGETS =================
    private JLabel amountLabel, typeLabel, categoryLabel, noteLabel;
    private JButton addBtn, editBtn, deleteBtn;
    private JLabel searchLabel, filterCategoryLabel;
    private JButton searchBtn, resetBtn;
    private JButton exportTxtBtn, exportCsvBtn;
    private JPanel notificationPanel, insightPanel;

    /**
     * Constructs the TransactionPanel, binding DI services and building sub-panels.
     * 
     * @param username The authenticated user profile username.
     * @param transactionService Injected Transaction Service.
     * @param budgetService Injected Budget Service.
     */
    public TransactionPanel(String username, TransactionService transactionService, BudgetService budgetService) {

        this.username = username;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.insightService = new InsightService();
        this.pieChartPanel = new PieChartPanel();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Outer margins

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(createTopForm(), BorderLayout.NORTH);
        center.add(createTable(), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Right-side auxiliary panels (Notifications, Insights, Category Pie Chart)
        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        rightPanel.add(createNotificationPanel());
        rightPanel.add(createInsightPanel());
        rightPanel.add(pieChartPanel);
        add(rightPanel, BorderLayout.EAST);

        // Load transaction data from services
        loadTransactions();

        // Recursively apply theme settings
        ThemeUtil.applyTheme(this);
    }

    // ================= SUMMARY PANEL =================
    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel summary = new JPanel(new GridLayout(1, 3, 10, 10));

        incomeLabel = new JLabel("Income: ₹0", SwingConstants.CENTER);
        expenseLabel = new JLabel("Expense: ₹0", SwingConstants.CENTER);
        balanceLabel = new JLabel("Balance: ₹0", SwingConstants.CENTER);

        summary.add(incomeLabel);
        summary.add(expenseLabel);
        summary.add(balanceLabel);

        // Score panel
        JPanel scorePanel = new JPanel(new BorderLayout());

        scoreLabel = new JLabel("Score: 0/100", SwingConstants.CENTER);
        statusLabel = new JLabel("Status: -", SwingConstants.CENTER);

        JPanel text = new JPanel(new GridLayout(1, 2));
        text.add(scoreLabel);
        text.add(statusLabel);

        scoreBar = new JProgressBar(0, 100);
        scoreBar.setStringPainted(true);

        scorePanel.add(text, BorderLayout.NORTH);
        scorePanel.add(scoreBar, BorderLayout.CENTER);

        panel.add(summary, BorderLayout.NORTH);
        panel.add(scorePanel, BorderLayout.SOUTH);

        return panel;
    }

    // ================= FORM =================
    private JPanel createTopForm() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 1. Transaction Input Panel (GridBagLayout for structured alignment)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels
        amountLabel = new JLabel("Amount");
        typeLabel = new JLabel("Type");
        categoryLabel = new JLabel("Category");
        noteLabel = new JLabel("Note");

        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.gridx = 0; inputPanel.add(amountLabel, gbc);
        gbc.gridx = 1; inputPanel.add(typeLabel, gbc);
        gbc.gridx = 2; inputPanel.add(categoryLabel, gbc);
        gbc.gridx = 3; inputPanel.add(noteLabel, gbc);

        // Inputs
        amountField = new JTextField(10);
        noteField = new JTextField(15);

        typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
        categoryBox = new JComboBox<>(Constants.CATEGORIES);

        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0.15; inputPanel.add(amountField, gbc);
        gbc.gridx = 1; gbc.weightx = 0.15; inputPanel.add(typeBox, gbc);
        gbc.gridx = 2; gbc.weightx = 0.15; inputPanel.add(categoryBox, gbc);
        gbc.gridx = 3; gbc.weightx = 0.35; inputPanel.add(noteField, gbc);

        // CRUD Buttons
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        crudPanel.add(addBtn);
        crudPanel.add(editBtn);
        crudPanel.add(deleteBtn);

        gbc.gridx = 4;
        gbc.weightx = 0.20;
        inputPanel.add(crudPanel, gbc);

        // 2. Filter & Export Panel (combined in one row using BorderLayout)
        JPanel filterExportPanel = new JPanel(new BorderLayout());
        filterExportPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Filter sub-panel (left aligned)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchField = new JTextField(12);
        filterCategoryBox = new JComboBox<>();
        filterCategoryBox.addItem("All");
        for (String cat : Constants.CATEGORIES) {
            filterCategoryBox.addItem(cat);
        }
        searchBtn = new JButton("Search");
        resetBtn = new JButton("Reset");

        searchLabel = new JLabel("Search:");
        filterCategoryLabel = new JLabel("Category:");

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(filterCategoryLabel);
        filterPanel.add(filterCategoryBox);
        filterPanel.add(searchBtn);
        filterPanel.add(resetBtn);

        // Export sub-panel (right aligned)
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        exportTxtBtn = new JButton("Export TXT");
        exportCsvBtn = new JButton("Export CSV");
        exportPanel.add(exportTxtBtn);
        exportPanel.add(exportCsvBtn);

        filterExportPanel.add(filterPanel, BorderLayout.WEST);
        filterExportPanel.add(exportPanel, BorderLayout.EAST);

        // Add both rows to main panel
        mainPanel.add(inputPanel);
        mainPanel.add(filterExportPanel);

        // Actions
        addBtn.addActionListener(e -> addTransaction());
        editBtn.addActionListener(e -> editTransaction());
        deleteBtn.addActionListener(e -> deleteTransaction());

        searchBtn.addActionListener(e -> applyFilter());
        resetBtn.addActionListener(e -> resetFilter());

        exportTxtBtn.addActionListener(e -> exportTXT());
        exportCsvBtn.addActionListener(e -> exportCSV());

        return mainPanel;
    }

    // ================= TABLE =================
    private JPanel createTable() {

        tableModel = new DefaultTableModel(
                new String[]{"ID","Type","Amount","Category","Note","Date"}, 0
        );

        table = new JTable(tableModel);

        // Populate form fields on row selection (UX improvement)
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    try {
                        String type = tableModel.getValueAt(row, 1).toString();
                        String amountStr = tableModel.getValueAt(row, 2).toString();
                        String category = tableModel.getValueAt(row, 3).toString();
                        String note = tableModel.getValueAt(row, 4).toString();

                        amountField.setText(amountStr);
                        noteField.setText(note);

                        // Set Type ComboBox
                        if ("Income".equalsIgnoreCase(type)) {
                            typeBox.setSelectedIndex(0);
                        } else {
                            typeBox.setSelectedIndex(1);
                        }

                        // Set Category ComboBox
                        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
                            if (Constants.CATEGORIES[i].equalsIgnoreCase(category)) {
                                categoryBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        // ignore parsing or bounds errors
                    }
                }
            }
        });

        // 🔥 1. Enable horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 🔥 2. Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);

        // 🔥 FULL-WIDTH BORDERLAYOUT WRAPPER
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    // ================= CRUD =================
    private void addTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                AlertUtil.showError(this, "Amount must be a positive value", "Error");
                return;
            }

            String txType = typeBox.getSelectedIndex() == 0 ? "Income" : "Expense";
            String txCategory = Constants.CATEGORIES[categoryBox.getSelectedIndex()];
            String note = CSVUtil.sanitize(noteField.getText());

            Transaction t = new Transaction(
                    transactionId++,
                    txType,
                    amount,
                    txCategory,
                    note,
                    LocalDate.now()
            );

            SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Transaction> doInBackground() {
                    transactionService.addTransaction(username, t);
                    return transactionService.loadTransactions(username);
                }

                @Override
                protected void done() {
                    try {
                        allTransactions = get();
                        refreshTable(allTransactions);
                        clearFields();
                    } catch (Exception e) {
                        AlertUtil.showError(TransactionPanel.this, "Error adding transaction: " + e.getMessage(), "Error");
                    }
                }
            };
            worker.execute();

        } catch(NumberFormatException e){
            AlertUtil.showError(this, "Please enter a valid numeric amount", "Error");
        } catch(Exception e){
            AlertUtil.showError(this, "An error occurred: " + e.getMessage(), "Error");
        }
    }

    private void editTransaction() {
        int row = table.getSelectedRow();
        if(row == -1) return;

        int id = Integer.parseInt(tableModel.getValueAt(row,0).toString());

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                AlertUtil.showError(this, "Amount must be a positive value", "Error");
                return;
            }
            String note = CSVUtil.sanitize(noteField.getText());

            LocalDate originalDate = null;
            for (Transaction t : allTransactions) {
                if (t.getId() == id) {
                    originalDate = t.getDate();
                    break;
                }
            }
            if (originalDate == null) {
                originalDate = LocalDate.now();
            }

            String txType = typeBox.getSelectedIndex() == 0 ? "Income" : "Expense";
            String txCategory = Constants.CATEGORIES[categoryBox.getSelectedIndex()];
            Transaction updated = new Transaction(
                    id,
                    txType,
                    amount,
                    txCategory,
                    note,
                    originalDate
            );

            SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Transaction> doInBackground() {
                    transactionService.updateTransaction(username, updated);
                    return transactionService.loadTransactions(username);
                }

                @Override
                protected void done() {
                    try {
                        allTransactions = get();
                        refreshTable(allTransactions);
                    } catch (Exception e) {
                        AlertUtil.showError(TransactionPanel.this, "Error updating transaction: " + e.getMessage(), "Error");
                    }
                }
            };
            worker.execute();
        } catch(NumberFormatException e){
            AlertUtil.showError(this, "Please enter a valid numeric amount", "Error");
        } catch(Exception e){
            AlertUtil.showError(this, "An error occurred: " + e.getMessage(), "Error");
        }
    }

    private void deleteTransaction() {
        int row = table.getSelectedRow();
        if(row==-1) return;

        int id = Integer.parseInt(tableModel.getValueAt(row,0).toString());

        Transaction toDelete = null;
        for (Transaction t : allTransactions) {
            if (t.getId() == id) {
                toDelete = t;
                break;
            }
        }
        if (toDelete == null) return;
        final Transaction finalToDelete = toDelete;

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() {
                transactionService.deleteTransaction(username, id);
                return transactionService.loadTransactions(username);
            }

            @Override
            protected void done() {
                try {
                    allTransactions = get();
                    refreshTable(allTransactions);

                    int undo = JOptionPane.showConfirmDialog(TransactionPanel.this, "Undo delete?");
                    if (undo == 0) {
                        SwingWorker<List<Transaction>, Void> undoWorker = new SwingWorker<>() {
                            @Override
                            protected List<Transaction> doInBackground() {
                                transactionService.addTransaction(username, finalToDelete);
                                return transactionService.loadTransactions(username);
                            }

                            @Override
                            protected void done() {
                                try {
                                    allTransactions = get();
                                    allTransactions.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
                                    SwingWorker<Void, Void> saveWorker = new SwingWorker<>() {
                                        @Override
                                        protected Void doInBackground() {
                                            transactionService.saveAllTransactions(username, allTransactions);
                                            return null;
                                        }

                                        @Override
                                        protected void done() {
                                            refreshTable(allTransactions);
                                        }
                                    };
                                    saveWorker.execute();
                                } catch (Exception e) {
                                    AlertUtil.showError(TransactionPanel.this, "Error restoring transaction: " + e.getMessage(), "Error");
                                }
                            }
                        };
                        undoWorker.execute();
                    }
                } catch (Exception e) {
                    AlertUtil.showError(TransactionPanel.this, "Error deleting transaction: " + e.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }

    // ================= FILTER =================
    private void applyFilter() {

        String keyword = searchField.getText().toLowerCase();
        int catIdx = filterCategoryBox.getSelectedIndex();

        List<Transaction> filtered = new ArrayList<>();

        for(Transaction t : allTransactions){

            boolean match = t.getNote().toLowerCase().contains(keyword);
            boolean cat = (catIdx == 0) || t.getCategory().equals(Constants.CATEGORIES[catIdx - 1]);

            if(match && cat) filtered.add(t);
        }

        refreshTable(filtered);
    }

    private void resetFilter() {
        searchField.setText("");
        filterCategoryBox.setSelectedIndex(0);
        refreshTable(allTransactions);
    }

    // ================= REFRESH =================
    private void refreshTable(List<Transaction> list) {

        tableModel.setRowCount(0);

        for (Transaction t : list) {
            String localizedType = "Income".equalsIgnoreCase(t.getType()) ? "Income" : "Expense";
            String localizedCategory = t.getCategory();
            tableModel.addRow(new Object[]{
                    t.getId(),
                    localizedType,
                    String.format("%.2f", t.getAmount()),
                    localizedCategory,
                    t.getNote(),
                    t.getDate()
            });
        }

        updateSummary();
        checkBudgetAlerts();
        updateInsights(); // 🔥 ADD THIS
        pieChartPanel.updateData(list); // 🔥 Update the custom Pie Chart drawing
        revalidate();
        repaint();
    }

    // ================= SUMMARY =================
    private void updateSummary() {

        double income = 0, expense = 0;

        for (Transaction t : allTransactions) {
            if ("Income".equalsIgnoreCase(t.getType())) income += t.getAmount();
            else expense += t.getAmount();
        }

        double balance = income - expense;

        incomeLabel.setText("Income" + ": ₹" + String.format("%,.2f", income));
        expenseLabel.setText("Expense" + ": ₹" + String.format("%,.2f", expense));
        balanceLabel.setText("Balance" + ": ₹" + String.format("%,.2f", balance));

        updateScore(income, expense);
    }

    // ================= SCORE =================
    private void updateScore(double income, double expense) {

        int score = 100;

        if(income==0){
            scoreBar.setValue(0);
            scoreLabel.setText("Score" + ": 0/100");
            statusLabel.setText("No Income");
            return;
        }

        double savings = income - expense;
        double percent = (savings/income)*100;

        if(percent<10) score -=30;
        else if(percent<20) score -=20;

        score = Math.max(score,0);

        scoreBar.setValue(score);
        scoreLabel.setText("Score" + ": " + score + "/100");

        String status = percent >= 40 ? "Excellent" : percent >= 20 ? "Good" : "Poor";
        statusLabel.setText(status);
    }

    // ================= EXPORT =================
    private void exportTXT() {
        JFileChooser fc = new JFileChooser();
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            new ReportService().exportToTXT(fc.getSelectedFile(), allTransactions);
        }
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            new ReportService().exportToCSV(fc.getSelectedFile(), allTransactions);
        }
    }

    // ================= LOAD =================
    private void loadTransactions() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Loading...", "", "", "", "", ""});

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() {
                return transactionService.loadTransactions(username);
            }

            @Override
            protected void done() {
                try {
                    allTransactions = get();
                    refreshTable(allTransactions);

                    int maxId = 0;
                    for (Transaction t : allTransactions) {
                        if (t.getId() > maxId) {
                            maxId = t.getId();
                        }
                    }
                    transactionId = maxId + 1;
                } catch (Exception e) {
                    AlertUtil.showError(TransactionPanel.this, "Failed to load transactions: " + e.getMessage(), "Error");
                }
            }
        };
        worker.execute();
    }

    private void clearFields(){
        amountField.setText("");
        noteField.setText("");
        if (typeBox.getItemCount() > 0) typeBox.setSelectedIndex(0);
        if (categoryBox.getItemCount() > 0) categoryBox.setSelectedIndex(0);
    }

    private JPanel createNotificationPanel() {

        notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setPreferredSize(new Dimension(250, 0)); // 🔥 FIX WIDTH

        notificationPanel.setBorder(BorderFactory.createTitledBorder("Notifications"));

        notificationArea = new JTextArea();
        notificationArea.setEditable(false);
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);

        notificationArea.setForeground(Color.RED);
        notificationArea.setBackground(new Color(250, 250, 250));

        JScrollPane scroll = new JScrollPane(notificationArea);

        notificationPanel.add(scroll, BorderLayout.CENTER);

        return notificationPanel;
    }

    private void checkBudgetAlerts() {

        Map<String, Double> spentMap =
                budgetService.calculateCategoryExpenses(allTransactions);

        StringBuilder alerts = new StringBuilder();

        for (String category : spentMap.keySet()) {

            double spent = spentMap.get(category);

            String alert = budgetService.getBudgetAlert(category, spent);

            if (alert != null) {
                alerts.append(alert).append("\n");
            }
        }

        // Show in notification panel
        notificationArea.setText(alerts.toString());

        // Optional popup (only if alert exists)
        if (alerts.length() > 0) {
            JOptionPane.showMessageDialog(this, alerts.toString());
        }
    }

    private JPanel createInsightPanel() {

        insightPanel = new JPanel(new BorderLayout());
        insightPanel.setBorder(BorderFactory.createTitledBorder("Insights"));

        insightArea = new JTextArea(); // ✅ store reference
        insightArea.setEditable(false);
        insightArea.setLineWrap(true);
        insightArea.setWrapStyleWord(true);

        insightArea.setBackground(new Color(245, 245, 245));

        insightPanel.add(new JScrollPane(insightArea), BorderLayout.CENTER);

        return insightPanel;
    }

    private void updateInsights() {

        String insights =
                insightService.generateInsights(allTransactions);

        insightArea.setText(insights);
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
