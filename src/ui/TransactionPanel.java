package ui;

import model.Transaction;
import service.TransactionService;
import service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionPanel (FINAL CLEAN VERSION)
 * --------------------------------------
 * ✔ Add / Edit / Delete
 * ✔ Undo Delete
 * ✔ Search + Filter
 * ✔ Summary + Financial Score
 * ✔ Export TXT / CSV
 */
public class TransactionPanel extends JPanel {

    // ================= INPUT =================
    private JTextField amountField, noteField;
    private JComboBox<String> typeBox, categoryBox;

    // ================= FILTER =================
    private JTextField searchField;
    private JComboBox<String> filterCategoryBox;

    // ================= TABLE =================
    private JTable table;
    private DefaultTableModel tableModel;

    // ================= DATA =================
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionService transactionService;
    private String username;
    private int transactionId = 1;

    // ================= SUMMARY =================
    private JLabel incomeLabel, expenseLabel, balanceLabel;

    // ================= SCORE =================
    private JLabel scoreLabel, statusLabel;
    private JProgressBar scoreBar;

    // ================= UNDO =================
    private Transaction lastDeletedTransaction;
    private int lastDeletedIndex;

    // ================= CONSTRUCTOR =================
    public TransactionPanel(String username) {

        this.username = username;
        this.transactionService = new TransactionService();

        setLayout(new BorderLayout(10, 10));

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(createTopForm(), BorderLayout.NORTH);
        center.add(createTable(), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        loadTransactions();
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

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ================= INPUT =================
        amountField = new JTextField(10);
        noteField = new JTextField(10);

        typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
        categoryBox = new JComboBox<>(new String[]{
                "Food", "Transport", "Shopping", "Bills", "Other"
        });

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        // ================= FILTER =================
        searchField = new JTextField(10);

        filterCategoryBox = new JComboBox<>(new String[]{
                "All", "Food", "Transport", "Shopping", "Bills", "Other"
        });

        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");

        JButton exportTxtBtn = new JButton("Export TXT");
        JButton exportCsvBtn = new JButton("Export CSV");

        // ================= ROW 1 =================
        gbc.gridy = 0;

        gbc.gridx = 0; panel.add(new JLabel("Amount"), gbc);
        gbc.gridx = 1; panel.add(new JLabel("Type"), gbc);
        gbc.gridx = 2; panel.add(new JLabel("Category"), gbc);
        gbc.gridx = 3; panel.add(new JLabel("Note"), gbc);

        // ================= ROW 2 =================
        gbc.gridy = 1;

        gbc.gridx = 0; panel.add(amountField, gbc);
        gbc.gridx = 1; panel.add(typeBox, gbc);
        gbc.gridx = 2; panel.add(categoryBox, gbc);
        gbc.gridx = 3; panel.add(noteField, gbc);

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        crudPanel.add(addBtn);
        crudPanel.add(editBtn);
        crudPanel.add(deleteBtn);

        gbc.gridx = 4;
        panel.add(crudPanel, gbc);

        // ================= ROW 3 =================
        gbc.gridy = 2;

        gbc.gridx = 0; panel.add(new JLabel("Search"), gbc);
        gbc.gridx = 1; panel.add(searchField, gbc);
        gbc.gridx = 2; panel.add(filterCategoryBox, gbc);
        gbc.gridx = 3; panel.add(searchBtn, gbc);
        gbc.gridx = 4; panel.add(resetBtn, gbc);

        // ================= ROW 4 (EXPORT) =================
        gbc.gridy = 3;
        gbc.gridx = 3;

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        exportPanel.add(exportTxtBtn);
        exportPanel.add(exportCsvBtn);

        gbc.gridwidth = 2;
        panel.add(exportPanel, gbc);

        // ================= ACTIONS =================
        addBtn.addActionListener(e -> addTransaction());
        editBtn.addActionListener(e -> editTransaction());
        deleteBtn.addActionListener(e -> deleteTransaction());

        searchBtn.addActionListener(e -> applyFilter());
        resetBtn.addActionListener(e -> resetFilter());

        exportTxtBtn.addActionListener(e -> exportTXT());
        exportCsvBtn.addActionListener(e -> exportCSV());

        return panel;
    }

    // ================= TABLE =================
    private JPanel createTable() {

        tableModel = new DefaultTableModel(
                new String[]{"ID","Type","Amount","Category","Note","Date"}, 0
        );

        table = new JTable(tableModel);

        // 🔥 1. Enable horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // 🔥 2. Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);

        // 🔥 Fix size (important for centering)
        scrollPane.setPreferredSize(new Dimension(750, 300));

        // 🔥 CENTER WRAPPER
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(scrollPane);

        return wrapper;
    }

    // ================= CRUD =================
    private void addTransaction() {

        try {
            double amount = Double.parseDouble(amountField.getText());

            Transaction t = new Transaction(
                    transactionId++,
                    typeBox.getSelectedItem().toString(),
                    amount,
                    categoryBox.getSelectedItem().toString(),
                    noteField.getText(),
                    LocalDate.now()
            );

            allTransactions.add(t);
            transactionService.saveAllTransactions(username, allTransactions);

            refreshTable(allTransactions);
            clearFields();

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Invalid input");
        }
    }

    private void editTransaction() {

        int row = table.getSelectedRow();
        if(row == -1) return;

        int id = Integer.parseInt(tableModel.getValueAt(row,0).toString());

        for(int i=0;i<allTransactions.size();i++){
            if(allTransactions.get(i).getId()==id){

                allTransactions.set(i, new Transaction(
                        id,
                        typeBox.getSelectedItem().toString(),
                        Double.parseDouble(amountField.getText()),
                        categoryBox.getSelectedItem().toString(),
                        noteField.getText(),
                        LocalDate.now()
                ));
                break;
            }
        }

        transactionService.saveAllTransactions(username, allTransactions);
        refreshTable(allTransactions);
    }

    private void deleteTransaction() {

        int row = table.getSelectedRow();
        if(row==-1) return;

        int id = Integer.parseInt(tableModel.getValueAt(row,0).toString());

        for(int i=0;i<allTransactions.size();i++){
            if(allTransactions.get(i).getId()==id){

                lastDeletedTransaction = allTransactions.get(i);
                lastDeletedIndex = i;

                allTransactions.remove(i);
                break;
            }
        }

        transactionService.saveAllTransactions(username, allTransactions);
        refreshTable(allTransactions);

        int undo = JOptionPane.showConfirmDialog(this,"Undo delete?");
        if(undo==0){
            allTransactions.add(lastDeletedIndex,lastDeletedTransaction);
            transactionService.saveAllTransactions(username, allTransactions);
            refreshTable(allTransactions);
        }
    }

    // ================= FILTER =================
    private void applyFilter() {

        String keyword = searchField.getText().toLowerCase();
        String category = filterCategoryBox.getSelectedItem().toString();

        List<Transaction> filtered = new ArrayList<>();

        for(Transaction t : allTransactions){

            boolean match = t.getNote().toLowerCase().contains(keyword);
            boolean cat = category.equals("All") || t.getCategory().equals(category);

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
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getType(),
                    String.format("%.2f", t.getAmount()),
                    t.getCategory(),
                    t.getNote(),
                    t.getDate()
            });
        }

        updateSummary();
    }

    // ================= SUMMARY =================
    private void updateSummary() {

        double income = 0, expense = 0;

        for (Transaction t : allTransactions) {
            if ("Income".equals(t.getType())) income += t.getAmount();
            else expense += t.getAmount();
        }

        double balance = income - expense;

        incomeLabel.setText("Income: ₹" + String.format("%,.2f", income));
        expenseLabel.setText("Expense: ₹" + String.format("%,.2f", expense));
        balanceLabel.setText("Balance: ₹" + String.format("%,.2f", balance));

        updateScore(income, expense);
    }

    // ================= SCORE =================
    private void updateScore(double income, double expense) {

        int score = 100;

        if(income==0){
            scoreBar.setValue(0);
            scoreLabel.setText("Score: 0");
            statusLabel.setText("No Income");
            return;
        }

        double savings = income - expense;
        double percent = (savings/income)*100;

        if(percent<10) score -=30;
        else if(percent<20) score -=20;

        score = Math.max(score,0);

        scoreBar.setValue(score);
        scoreLabel.setText("Score: "+score+"/100");

        statusLabel.setText(percent>=40?"Excellent":percent>=20?"Good":"Poor");
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

        allTransactions = transactionService.loadTransactions(username);
        refreshTable(allTransactions);

        transactionId = allTransactions.size()+1;
    }

    private void clearFields(){
        amountField.setText("");
        noteField.setText("");
    }
}