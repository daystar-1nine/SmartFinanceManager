/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.ChartFactory
 *  org.jfree.chart.ChartPanel
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.plot.PiePlot
 *  org.jfree.data.general.DefaultPieDataset
 *  org.jfree.data.general.PieDataset
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import model.Loan;
import model.Payment;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import service.LoanService;
import util.ThemeUtil;

public class LoanPanel
extends JPanel
implements Scrollable {
    private final String username;
    private final LoanService loanService;
    private List<Loan> allLoans = new ArrayList<Loan>();
    private JLabel givenLabel;
    private JLabel takenLabel;
    private JLabel pendingLabel;
    private JLabel healthLabel;
    private JTextField personNameField;
    private JComboBox<String> typeBox;
    private JTextField totalAmountField;
    private JTextField paidAmountField;
    private JTextField dueDateField;
    private JTextField interestRateField;
    private JComboBox<String> interestTypeBox;
    private JTextField emiField;
    private JTextField noteField;
    private JLabel previewLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> typeFilterBox;
    private JComboBox<String> statusFilterBox;
    private JTextArea insightsArea;
    private ChartPanel chartPanel;
    private DefaultPieDataset dataset;
    private JPanel summaryPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    private JPanel insightsPanel;
    private JLabel personNameLabel;
    private JLabel typeLabel;
    private JLabel principalLabel;
    private JLabel paidAmountLabel;
    private JLabel dueDateLabel;
    private JLabel interestRateLabel;
    private JLabel interestTypeLabel;
    private JLabel emiLabel;
    private JLabel noteLabel;
    private JButton addBtn;
    private JLabel searchFieldLabel;
    private JLabel typeFilterLabel;
    private JLabel statusFilterLabel;
    private JButton paymentBtn;
    private JButton markPaidBtn;
    private JButton historyBtn;
    private JButton deleteBtn;
    private JButton exportCsvBtn;
    private JButton exportTxtBtn;

    public LoanPanel(String username) {
        this.username = username;
        this.loanService = new LoanService();
        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.setBackground(ThemeUtil.getBackgroundColor());
        this.add((Component)this.createSummaryPanel(), "North");
        JPanel centerContainer = new JPanel(new BorderLayout(15, 15));
        centerContainer.setOpaque(false);
        centerContainer.add((Component)this.createFormPanel(), "North");
        centerContainer.add((Component)this.createTablePanel(), "Center");
        this.add((Component)centerContainer, "Center");
        this.add((Component)this.createInsightsChartPanel(), "East");
        this.refreshData();
        ThemeUtil.applyTheme(this);
        this.updateSummaryColors();
    }

    private JPanel createSummaryPanel() {
        this.summaryPanel = new JPanel(new BorderLayout(10, 10));
        this.summaryPanel.setName("card");
        this.summaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), "Loan Summary", 1, 2, new Font("SansSerif", 1, 12), ThemeUtil.getTextColor()));
        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        gridPanel.setOpaque(false);
        this.givenLabel = new JLabel("Total Given: \u20b90.00", 0);
        this.givenLabel.setName("customColorLabel");
        this.givenLabel.setFont(new Font("SansSerif", 1, 14));
        this.takenLabel = new JLabel("Total Taken: \u20b90.00", 0);
        this.takenLabel.setName("customColorLabel");
        this.takenLabel.setFont(new Font("SansSerif", 1, 14));
        this.pendingLabel = new JLabel("Net Pending: \u20b90.00", 0);
        this.pendingLabel.setName("customColorLabel");
        this.pendingLabel.setFont(new Font("SansSerif", 1, 14));
        this.healthLabel = new JLabel("Health Score: 100/100", 0);
        this.healthLabel.setName("customColorLabel");
        this.healthLabel.setFont(new Font("SansSerif", 1, 14));
        gridPanel.add(this.givenLabel);
        gridPanel.add(this.takenLabel);
        gridPanel.add(this.pendingLabel);
        gridPanel.add(this.healthLabel);
        this.summaryPanel.add((Component)gridPanel, "Center");
        return this.summaryPanel;
    }

    private JPanel createFormPanel() {
        this.formPanel = new JPanel(new GridBagLayout());
        this.formPanel.setName("card");
        this.formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), "Add Loan", 1, 2, new Font("SansSerif", 1, 12), ThemeUtil.getTextColor()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.fill = 2;
        this.personNameLabel = new JLabel("Person Name:");
        this.typeLabel = new JLabel("Type:");
        this.principalLabel = new JLabel("Principal (\u20b9):");
        this.paidAmountLabel = new JLabel("Paid Amount (\u20b9):");
        gbc.gridy = 0;
        gbc.gridx = 0;
        this.formPanel.add((Component)this.personNameLabel, gbc);
        gbc.gridx = 1;
        this.formPanel.add((Component)this.typeLabel, gbc);
        gbc.gridx = 2;
        this.formPanel.add((Component)this.principalLabel, gbc);
        gbc.gridx = 3;
        this.formPanel.add((Component)this.paidAmountLabel, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        this.personNameField = new JTextField(12);
        this.formPanel.add((Component)this.personNameField, gbc);
        gbc.gridx = 1;
        this.typeBox = new JComboBox<>(new String[]{"Given (Lent)", "Taken (Borrowed)"});
        this.formPanel.add(this.typeBox, gbc);
        gbc.gridx = 2;
        this.totalAmountField = new JTextField(10);
        this.formPanel.add((Component)this.totalAmountField, gbc);
        gbc.gridx = 3;
        this.paidAmountField = new JTextField("0.0", 10);
        this.formPanel.add((Component)this.paidAmountField, gbc);
        this.dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):");
        this.interestRateLabel = new JLabel("Interest Rate (%):");
        this.interestTypeLabel = new JLabel("Interest Type:");
        this.emiLabel = new JLabel("Monthly EMI (\u20b9):");
        gbc.gridy = 2;
        gbc.gridx = 0;
        this.formPanel.add((Component)this.dueDateLabel, gbc);
        gbc.gridx = 1;
        this.formPanel.add((Component)this.interestRateLabel, gbc);
        gbc.gridx = 2;
        this.formPanel.add((Component)this.interestTypeLabel, gbc);
        gbc.gridx = 3;
        this.formPanel.add((Component)this.emiLabel, gbc);
        gbc.gridy = 3;
        gbc.gridx = 0;
        this.dueDateField = new JTextField(LocalDate.now().plusMonths(1L).toString(), 12);
        this.formPanel.add((Component)this.dueDateField, gbc);
        gbc.gridx = 1;
        this.interestRateField = new JTextField("0.0", 10);
        this.formPanel.add((Component)this.interestRateField, gbc);
        gbc.gridx = 2;
        this.interestTypeBox = new JComboBox<>(new String[]{"Simple", "Compound"});
        this.formPanel.add(this.interestTypeBox, gbc);
        gbc.gridx = 3;
        this.emiField = new JTextField("0.0", 10);
        this.formPanel.add((Component)this.emiField, gbc);
        this.noteLabel = new JLabel("Note:");
        gbc.gridy = 4;
        gbc.gridx = 0;
        this.formPanel.add((Component)this.noteLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        this.noteField = new JTextField(25);
        this.formPanel.add((Component)this.noteField, gbc);
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        this.addBtn = new JButton("Add Loan");
        this.addBtn.setFont(new Font("SansSerif", 1, 12));
        this.addBtn.addActionListener(e -> this.addLoan());
        this.formPanel.add((Component)this.addBtn, gbc);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
        this.previewLabel = new JLabel("Enter Principal and Interest details for preview.");
        this.previewLabel.setFont(new Font("SansSerif", 2, 11));
        this.previewLabel.setForeground(ThemeUtil.getSecondaryTextColor());
        this.formPanel.add((Component)this.previewLabel, gbc);
        DocumentListener dListener = new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFormPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFormPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFormPreview();
            }
        };
        this.totalAmountField.getDocument().addDocumentListener(dListener);
        this.interestRateField.getDocument().addDocumentListener(dListener);
        this.interestTypeBox.addActionListener(e -> this.updateFormPreview());
        return this.formPanel;
    }

    private JPanel createTablePanel() {
        this.tablePanel = new JPanel(new BorderLayout(5, 5));
        this.tablePanel.setName("card");
        this.tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), "Loan Records", 1, 2, new Font("SansSerif", 1, 12), ThemeUtil.getTextColor()));
        JPanel filterPanel = new JPanel(new FlowLayout(0, 15, 5));
        filterPanel.setOpaque(false);
        this.searchFieldLabel = new JLabel("Search Name:");
        filterPanel.add(this.searchFieldLabel);
        this.searchField = new JTextField(10);
        this.searchField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(this.searchField);
        this.typeFilterLabel = new JLabel("Type:");
        filterPanel.add(this.typeFilterLabel);
        this.typeFilterBox = new JComboBox<>(new String[]{"All", "Given (Lent)", "Taken (Borrowed)"});
        this.typeFilterBox.addActionListener(e -> this.applyFilters());
        filterPanel.add(this.typeFilterBox);
        this.statusFilterLabel = new JLabel("Status:");
        filterPanel.add(this.statusFilterLabel);
        this.statusFilterBox = new JComboBox<>(new String[]{"All", "Active", "Closed", "Overdue"});
        this.statusFilterBox.addActionListener(e -> this.applyFilters());
        filterPanel.add(this.statusFilterBox);
        this.tablePanel.add((Component)filterPanel, "North");
        this.tableModel = new DefaultTableModel(new String[]{"ID", "Person", "Type", "Principal (\u20b9)", "Paid (\u20b9)", "Remaining (\u20b9)", "Status", "Due Date"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        this.table = new JTable(this.tableModel){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                boolean selected = this.isRowSelected(row);
                boolean dark = ThemeUtil.isDarkMode();
                if (!selected) {
                    try {
                        String type = this.getValueAt(row, 2).toString();
                        String status = this.getValueAt(row, 6).toString();
                        boolean isOverdue = "Overdue".equalsIgnoreCase(status);
                        if (isOverdue) {
                            comp.setBackground(dark ? new Color(90, 30, 30) : new Color(255, 230, 230));
                            comp.setForeground(dark ? new Color(255, 180, 180) : new Color(198, 40, 40));
                        } else {
                            comp.setBackground(ThemeUtil.getCardBackgroundColor());
                            boolean isClosed = "Closed".equalsIgnoreCase(status);
                            boolean isGiven = "Given".equalsIgnoreCase(type) || "Given (Lent)".equalsIgnoreCase(type);
                            boolean isTaken = "Taken".equalsIgnoreCase(type) || "Taken (Borrowed)".equalsIgnoreCase(type);
                            if (isClosed) {
                                comp.setForeground(dark ? new Color(150, 150, 150) : new Color(120, 120, 120));
                            } else if (isGiven) {
                                comp.setForeground(dark ? new Color(102, 187, 106) : new Color(46, 125, 50));
                            } else if (isTaken) {
                                comp.setForeground(dark ? new Color(239, 83, 80) : new Color(198, 40, 40));
                            } else {
                                comp.setForeground(ThemeUtil.getTextColor());
                            }
                        }
                    }
                    catch (Exception e) {
                        comp.setForeground(ThemeUtil.getTextColor());
                    }
                }
                return comp;
            }
        };
        this.table.setRowHeight(28);
        this.table.getTableHeader().setFont(new Font("SansSerif", 1, 12));
        this.table.setFont(new Font("SansSerif", 0, 12));
        this.table.getColumnModel().getColumn(0).setPreferredWidth(45);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(125);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(95);
        this.table.getColumnModel().getColumn(4).setPreferredWidth(95);
        this.table.getColumnModel().getColumn(5).setPreferredWidth(100);
        this.table.getColumnModel().getColumn(6).setPreferredWidth(85);
        this.table.getColumnModel().getColumn(7).setPreferredWidth(85);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(0);
        this.table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        this.table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        this.table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setPreferredSize(new Dimension(620, 200));
        this.tablePanel.add((Component)scrollPane, "Center");
        JPanel buttonsRow = new JPanel(new FlowLayout(1, 10, 5));
        buttonsRow.setOpaque(false);
        this.paymentBtn = new JButton("Add Payment");
        this.paymentBtn.setFont(new Font("SansSerif", 1, 12));
        this.paymentBtn.addActionListener(e -> this.addPayment());
        this.markPaidBtn = new JButton("Mark as Paid");
        this.markPaidBtn.setFont(new Font("SansSerif", 1, 12));
        this.markPaidBtn.addActionListener(e -> this.markAsPaid());
        this.historyBtn = new JButton("View History");
        this.historyBtn.setFont(new Font("SansSerif", 1, 12));
        this.historyBtn.addActionListener(e -> this.viewHistory());
        this.deleteBtn = new JButton("Delete");
        this.deleteBtn.setFont(new Font("SansSerif", 1, 12));
        this.deleteBtn.setBackground(new Color(211, 47, 47));
        this.deleteBtn.setForeground(Color.WHITE);
        this.deleteBtn.addActionListener(e -> this.deleteLoan());
        this.exportCsvBtn = new JButton("Export CSV");
        this.exportCsvBtn.setFont(new Font("SansSerif", 1, 12));
        this.exportCsvBtn.addActionListener(e -> this.exportCSV());
        this.exportTxtBtn = new JButton("Export TXT");
        this.exportTxtBtn.setFont(new Font("SansSerif", 1, 12));
        this.exportTxtBtn.addActionListener(e -> this.exportTXT());
        buttonsRow.add(this.paymentBtn);
        buttonsRow.add(this.markPaidBtn);
        buttonsRow.add(this.historyBtn);
        buttonsRow.add(this.deleteBtn);
        buttonsRow.add(this.exportCsvBtn);
        buttonsRow.add(this.exportTxtBtn);
        this.tablePanel.add((Component)buttonsRow, "South");
        return this.tablePanel;
    }

    private JPanel createInsightsChartPanel() {
        JPanel eastContainer = new JPanel(new GridLayout(2, 1, 10, 10));
        eastContainer.setPreferredSize(new Dimension(280, 0));
        eastContainer.setOpaque(false);
        eastContainer.add(this.createInsightsPanel());
        eastContainer.add(this.createChartPanel());
        return eastContainer;
    }

    private JPanel createInsightsPanel() {
        this.insightsPanel = new JPanel(new BorderLayout(5, 5));
        this.insightsPanel.setName("card");
        this.insightsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), "Insights", 1, 2, new Font("SansSerif", 1, 12), ThemeUtil.getTextColor()));
        this.insightsArea = new JTextArea();
        this.insightsArea.setEditable(false);
        this.insightsArea.setLineWrap(true);
        this.insightsArea.setWrapStyleWord(true);
        this.insightsArea.setFont(new Font("SansSerif", 0, 12));
        this.insightsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(this.insightsArea);
        scroll.setBorder(null);
        this.insightsPanel.add((Component)scroll, "Center");
        return this.insightsPanel;
    }

    private JPanel createChartPanel() {
        this.dataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart((String)"Lending Distribution", (PieDataset)this.dataset, (boolean)true, (boolean)true, (boolean)false);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setName("card");
        this.chartPanel.setBorder((Border)BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeUtil.getBorderColor(), 1, true), "Distribution", 1, 2, new Font("SansSerif", 1, 12), ThemeUtil.getTextColor()));
        this.chartPanel.setPreferredSize(new Dimension(260, 200));
        return this.chartPanel;
    }

    private void updateSummaryColors() {
        Color healthBorder;
        Color healthColor;
        if (this.givenLabel == null) {
            return;
        }
        boolean dark = ThemeUtil.isDarkMode();
        Color givenColor = dark ? new Color(129, 199, 132) : new Color(46, 125, 50);
        Color givenBorder = dark ? new Color(60, 90, 65) : new Color(200, 230, 201);
        Color takenColor = dark ? new Color(239, 83, 80) : new Color(198, 40, 40);
        Color takenBorder = dark ? new Color(100, 50, 50) : new Color(255, 205, 210);
        Color pendingColor = dark ? new Color(100, 181, 246) : new Color(21, 101, 192);
        Color pendingBorder = dark ? new Color(45, 65, 95) : new Color(187, 222, 251);
        this.givenLabel.setForeground(givenColor);
        this.givenLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(givenBorder, 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.takenLabel.setForeground(takenColor);
        this.takenLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(takenBorder, 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.pendingLabel.setForeground(pendingColor);
        this.pendingLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(pendingBorder, 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        int health = this.calculateLoanHealthScore(this.allLoans);
        this.healthLabel.setText("Health Score: " + health + "/100");
        if (health >= 80) {
            healthColor = dark ? new Color(129, 199, 132) : new Color(46, 125, 50);
            healthBorder = dark ? new Color(60, 90, 65) : new Color(200, 230, 201);
        } else if (health >= 50) {
            healthColor = dark ? new Color(255, 183, 77) : new Color(245, 124, 0);
            healthBorder = dark ? new Color(110, 80, 40) : new Color(255, 224, 178);
        } else {
            healthColor = dark ? new Color(239, 83, 80) : new Color(198, 40, 40);
            healthBorder = dark ? new Color(100, 50, 50) : new Color(255, 205, 210);
        }
        this.healthLabel.setForeground(healthColor);
        this.healthLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(healthBorder, 2, true), BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        this.insightsArea.setBackground(dark ? new Color(30, 30, 42) : new Color(245, 247, 250));
        this.insightsArea.setForeground(ThemeUtil.getTextColor());
        this.updateChartStyle();
    }

    private void updateChartStyle() {
        if (this.chartPanel == null || this.chartPanel.getChart() == null) {
            return;
        }
        JFreeChart chart = this.chartPanel.getChart();
        boolean dark = ThemeUtil.isDarkMode();
        Color bg = ThemeUtil.getCardBackgroundColor();
        Color text = ThemeUtil.getTextColor();
        Color border = ThemeUtil.getBorderColor();
        chart.setBackgroundPaint((Paint)bg);
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint((Paint)text);
            chart.getTitle().setFont(new Font("SansSerif", 1, 12));
        }
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setBackgroundPaint((Paint)bg);
        plot.setOutlinePaint((Paint)border);
        plot.setLabelFont(new Font("SansSerif", 0, 10));
        plot.setLabelPaint((Paint)text);
        plot.setLabelBackgroundPaint((Paint)bg);
        plot.setLabelOutlinePaint((Paint)border);
        plot.setSectionPaint((Comparable)((Object)"Given (Lent)"), (Paint)(dark ? new Color(102, 187, 106) : new Color(46, 125, 50)));
        plot.setSectionPaint((Comparable)((Object)"Taken (Borrowed)"), (Paint)(dark ? new Color(239, 83, 80) : new Color(198, 40, 40)));
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint((Paint)bg);
            chart.getLegend().setItemPaint((Paint)text);
            chart.getLegend().setItemFont(new Font("SansSerif", 0, 10));
        }
    }

    private void updateFormPreview() {
        try {
            boolean isSimple;
            double principal = Double.parseDouble(this.totalAmountField.getText().trim());
            double rate = Double.parseDouble(this.interestRateField.getText().trim());
            boolean bl = isSimple = this.interestTypeBox.getSelectedIndex() == 0;
            if (principal > 0.0 && rate >= 0.0) {
                double estInterest = isSimple ? principal * (rate / 100.0) * 1.0 : principal * Math.pow(1.0 + rate / 100.0, 1.0) - principal;
                this.previewLabel.setText(String.format("Est. 1-Yr Interest: \u20b9%,.2f | Est. Total: \u20b9%,.2f", estInterest, principal + estInterest));
            } else {
                this.previewLabel.setText("Enter Principal and Interest details for preview.");
            }
        }
        catch (NumberFormatException e) {
            this.previewLabel.setText("Enter valid numeric values for preview.");
        }
    }

    private void applyFilters() {
        if (this.allLoans == null) {
            return;
        }
        String query = this.searchField.getText().trim().toLowerCase();
        int typeIdx = this.typeFilterBox.getSelectedIndex();
        int statusIdx = this.statusFilterBox.getSelectedIndex();
        this.tableModel.setRowCount(0);
        for (Loan l : this.allLoans) {
            boolean matchesStatus;
            boolean matchesName = query.isEmpty() || l.getPersonName().toLowerCase().contains(query);
            boolean matchesType = typeIdx == 0 || typeIdx == 1 && "Given".equalsIgnoreCase(l.getType()) || typeIdx == 2 && "Taken".equalsIgnoreCase(l.getType());
            boolean bl = matchesStatus = statusIdx == 0 || statusIdx == 1 && "Active".equalsIgnoreCase(l.getStatus()) || statusIdx == 2 && "Closed".equalsIgnoreCase(l.getStatus()) || statusIdx == 3 && "Overdue".equalsIgnoreCase(l.getStatus());
            if (!matchesName || !matchesType || !matchesStatus) continue;
            String displayType = "Given".equalsIgnoreCase(l.getType()) ? "Given (Lent)" : "Taken (Borrowed)";
            String displayStatus = l.getStatus();
            this.tableModel.addRow(new Object[]{l.getId(), l.getPersonName(), displayType, l.getTotalAmount(), l.getPaidAmount(), l.getRemainingAmount(), displayStatus, l.getDueDate() != null ? l.getDueDate().toString() : ""});
        }
    }

    private int calculateLoanHealthScore(List<Loan> loans) {
        int score = 100;
        int overdueCount = 0;
        double totalTakenPending = 0.0;
        double totalGivenPending = 0.0;
        for (Loan l : loans) {
            if (!"Active".equalsIgnoreCase(l.getStatus()) && !"Overdue".equalsIgnoreCase(l.getStatus())) continue;
            double remaining = l.getRemainingAmount();
            if ("Taken".equalsIgnoreCase(l.getType())) {
                totalTakenPending += remaining;
            } else {
                totalGivenPending += remaining;
            }
            if (!"Overdue".equalsIgnoreCase(l.getStatus())) continue;
            ++overdueCount;
        }
        score -= overdueCount * 15;
        score -= Math.min(30, (int)(totalTakenPending / 2000.0));
        score -= Math.min(20, (int)(totalGivenPending / 5000.0));
        if (totalTakenPending > totalGivenPending) {
            score -= 10;
        }
        return Math.max(0, Math.min(100, score));
    }

    private void refreshData() {
        double totalOutstanding;
        this.allLoans = this.loanService.loadLoans(this.username);
        double totalGiven = 0.0;
        double totalTaken = 0.0;
        for (Loan l : this.allLoans) {
            if (!"Active".equalsIgnoreCase(l.getStatus()) && !"Overdue".equalsIgnoreCase(l.getStatus())) continue;
            if ("Given".equalsIgnoreCase(l.getType())) {
                totalGiven += l.getRemainingAmount();
                continue;
            }
            totalTaken += l.getRemainingAmount();
        }
        double netPending = totalGiven - totalTaken;
        this.givenLabel.setText(String.format("Total Given: \u20b9%,.2f", totalGiven));
        this.takenLabel.setText(String.format("Total Taken: \u20b9%,.2f", totalTaken));
        if (netPending >= 0.0) {
            this.pendingLabel.setText(String.format("Net Pending: +\u20b9%,.2f", netPending));
        } else {
            this.pendingLabel.setText(String.format("Net Pending: -\u20b9%,.2f", Math.abs(netPending)));
        }
        this.applyFilters();
        this.updateSummaryColors();
        this.dataset.clear();
        this.dataset.setValue((Comparable)((Object)"Given (Lent)"), totalGiven);
        this.dataset.setValue((Comparable)((Object)"Taken (Borrowed)"), totalTaken);
        this.updateChartStyle();
        StringBuilder insights = new StringBuilder();
        int activeOverdueCount = 0;
        double highestGivenAmount = 0.0;
        String highestDebtor = "Nobody";
        int totalActiveCount = 0;
        int activeGivenCount = 0;
        int activeTakenCount = 0;
        for (Loan l : this.allLoans) {
            if ("Closed".equalsIgnoreCase(l.getStatus())) continue;
            ++totalActiveCount;
            double remaining = l.getRemainingAmount();
            if ("Given".equalsIgnoreCase(l.getType())) {
                ++activeGivenCount;
                if (remaining > highestGivenAmount) {
                    highestGivenAmount = remaining;
                    highestDebtor = l.getPersonName();
                }
            } else {
                ++activeTakenCount;
            }
            if ("Overdue".equalsIgnoreCase(l.getStatus())) {
                ++activeOverdueCount;
                long daysOverdue = ChronoUnit.DAYS.between(l.getDueDate(), LocalDate.now());
                insights.append(String.format("  ❌ %s's loan is overdue by %d days\n", l.getPersonName(), daysOverdue));
                continue;
            }
            if (!(l.getMonthlyEMI() > 0.0)) continue;
            insights.append(String.format("  📅 Next Payment: \u20b9%,.2f on %s (%s)\n", l.getMonthlyEMI(), l.getNextPaymentDate().toString(), l.getPersonName()));
        }
        insights.append("\n");
        if (highestGivenAmount > 0.0) {
            insights.append(String.format("👤 %s owes you the most (\u20b9%,.2f).\n\n", highestDebtor, highestGivenAmount));
        }
        if ((totalOutstanding = totalGiven + totalTaken) > 0.0) {
            insights.append(String.format("💰 \u20b9%,.2f is active in loans.\n\n", totalOutstanding));
        }
        if (activeOverdueCount > 0) {
            insights.append(String.format("⚠️ Warning: You have %d overdue loan(s)!\n\n", activeOverdueCount));
        }
        if (totalActiveCount > 5) {
            insights.append("🔄 Behavior: You lend or borrow frequently.\n\n");
        }
        if (activeGivenCount > activeTakenCount) {
            insights.append("📈 Behavior: You tend to act as a Lender.\n\n");
        } else if (activeTakenCount > activeGivenCount) {
            insights.append("📉 Behavior: You tend to act as a Borrower.\n\n");
        }
        if (totalActiveCount == 0) {
            insights.append("No active outstanding loans. Debt free!");
        }
        this.insightsArea.setText(insights.toString());
    }

    private void addLoan() {
        double emi;
        double interestRate;
        LocalDate dueDate;
        double paidAmount;
        double totalAmount;
        String name = this.personNameField.getText().trim();
        String type = this.typeBox.getSelectedIndex() == 0 ? "Given" : "Taken";
        String totalStr = this.totalAmountField.getText().trim();
        String paidStr = this.paidAmountField.getText().trim();
        String dueDateStr = this.dueDateField.getText().trim();
        String interestStr = this.interestRateField.getText().trim();
        String interestType = this.interestTypeBox.getSelectedIndex() == 0 ? "Simple" : "Compound";
        String emiStr = this.emiField.getText().trim();
        String note = this.noteField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Person Name cannot be empty.", "Validation Error", 0);
            return;
        }
        try {
            totalAmount = Double.parseDouble(totalStr);
            if (totalAmount <= 0.0) {
                JOptionPane.showMessageDialog(this, "Principal must be positive.", "Validation Error", 0);
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric principal.", "Validation Error", 0);
            return;
        }
        try {
            paidAmount = Double.parseDouble(paidStr);
            if (paidAmount < 0.0) {
                JOptionPane.showMessageDialog(this, "Paid Amount cannot be negative.", "Validation Error", 0);
                return;
            }
            if (paidAmount > totalAmount) {
                JOptionPane.showMessageDialog(this, "Paid Amount cannot exceed principal initially.", "Validation Error", 0);
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric paid amount.", "Validation Error", 0);
            return;
        }
        try {
            dueDate = LocalDate.parse(dueDateStr);
        }
        catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Due date must follow YYYY-MM-DD format.", "Validation Error", 0);
            return;
        }
        try {
            interestRate = Double.parseDouble(interestStr);
            if (interestRate < 0.0) {
                JOptionPane.showMessageDialog(this, "Interest rate cannot be negative.", "Validation Error", 0);
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid interest rate value.", "Validation Error", 0);
            return;
        }
        try {
            emi = Double.parseDouble(emiStr);
            if (emi < 0.0) {
                JOptionPane.showMessageDialog(this, "EMI cannot be negative.", "Validation Error", 0);
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid EMI amount.", "Validation Error", 0);
            return;
        }
        int maxId = 0;
        for (Loan l : this.allLoans) {
            if (l.getId() <= maxId) continue;
            maxId = l.getId();
        }
        Loan newLoan = new Loan(maxId + 1, name, type, totalAmount, paidAmount, LocalDate.now(), note, "Active", dueDate, interestRate, interestType, emi, new ArrayList<Payment>());
        if (paidAmount > 0.0) {
            newLoan.getPaymentHistory().add(new Payment(paidAmount, LocalDate.now()));
        }
        this.loanService.addLoan(this.username, newLoan);
        this.personNameField.setText("");
        this.totalAmountField.setText("");
        this.paidAmountField.setText("0.0");
        this.dueDateField.setText(LocalDate.now().plusMonths(1L).toString());
        this.interestRateField.setText("0.0");
        this.emiField.setText("0.0");
        this.noteField.setText("");
        this.previewLabel.setText("Enter Principal and Interest details for preview.");
        this.refreshData();
        JOptionPane.showMessageDialog(this, "Fintech Loan added successfully!", "Success", 1);
    }

    private void addPayment() {
        int selectedRow = this.table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan record from the table first.", "Selection Required", 2);
            return;
        }
        int loanId = (Integer)this.tableModel.getValueAt(selectedRow, 0);
        Loan loan = null;
        for (Loan l : this.allLoans) {
            if (l.getId() != loanId) continue;
            loan = l;
            break;
        }
        if (loan == null) {
            return;
        }
        if ("Closed".equalsIgnoreCase(loan.getStatus())) {
            JOptionPane.showMessageDialog(this, "This loan is already Closed.", "Info", 1);
            return;
        }
        double remaining = loan.getRemainingAmount();
        String promptMsg = "Given".equalsIgnoreCase(loan.getType()) ? String.format("Enter payment received from %s (Remaining: \u20b9%,.2f, inc. Interest):", loan.getPersonName(), remaining) : String.format("Enter payment paid to %s (Remaining: \u20b9%,.2f, inc. Interest):", loan.getPersonName(), remaining);
        String input = JOptionPane.showInputDialog(this, promptMsg, "Add Payment", 3);
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        try {
            double paymentVal = Double.parseDouble(input.trim());
            if (paymentVal <= 0.0) {
                JOptionPane.showMessageDialog(this, "Payment amount must be a positive number.", "Error", 0);
                return;
            }
            if (paymentVal > remaining) {
                int confirm = JOptionPane.showConfirmDialog(this, String.format("Payment (\u20b9%,.2f) exceeds remaining loan balance (\u20b9%,.2f).\nDo you want to clear the loan fully?", paymentVal, remaining), "Confirm Excess Payment", 0);
                if (confirm == 0) {
                    paymentVal = remaining;
                } else {
                    return;
                }
            }
            this.loanService.updateLoanPayment(this.username, loanId, paymentVal);
            this.refreshData();
            JOptionPane.showMessageDialog(this, "Payment recorded in history ledger successfully!", "Success", 1);
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value.", "Error", 0);
        }
    }

    private void markAsPaid() {
        int selectedRow = this.table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan record from the table first.", "Selection Required", 2);
            return;
        }
        int loanId = (Integer)this.tableModel.getValueAt(selectedRow, 0);
        Loan loan = null;
        for (Loan l : this.allLoans) {
            if (l.getId() != loanId) continue;
            loan = l;
            break;
        }
        if (loan == null) {
            return;
        }
        if ("Closed".equalsIgnoreCase(loan.getStatus())) {
            JOptionPane.showMessageDialog(this, "This loan is already Closed.", "Info", 1);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark the loan for " + loan.getPersonName() + " as fully paid/settled?", "Confirm Mark as Paid", 0);
        if (confirm == 0) {
            this.loanService.markAsPaid(this.username, loanId);
            this.refreshData();
            JOptionPane.showMessageDialog(this, "Loan marked as fully settled.", "Success", 1);
        }
    }

    private void viewHistory() {
        int selectedRow = this.table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan record from the table first.", "Selection Required", 2);
            return;
        }
        int loanId = (Integer)this.tableModel.getValueAt(selectedRow, 0);
        Loan loan = null;
        for (Loan l : this.allLoans) {
            if (l.getId() != loanId) continue;
            loan = l;
            break;
        }
        if (loan == null) {
            return;
        }
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Payment Ledger - " + loan.getPersonName(), true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        DefaultTableModel historyModel = new DefaultTableModel(new String[]{"ID", "Date", "Paid Amount"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        List<Payment> history = loan.getPaymentHistory();
        if (history == null || history.isEmpty()) {
            historyModel.addRow(new Object[]{"-", "No payments recorded yet", ""});
        } else {
            for (int i = 0; i < history.size(); ++i) {
                Payment p = history.get(i);
                historyModel.addRow(new Object[]{i + 1, p.getDate().toString(), String.format("\u20b9%,.2f", p.getAmount())});
            }
        }
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("SansSerif", 1, 12));
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialog.add((Component)scroll, "Center");
        JPanel closePanel = new JPanel(new FlowLayout(1));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        closePanel.add(closeBtn);
        dialog.add((Component)closePanel, "South");
        ThemeUtil.applyTheme(dialog.getContentPane());
        dialog.setVisible(true);
    }

    private void deleteLoan() {
        int selectedRow = this.table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan record from the table first.", "Selection Required", 2);
            return;
        }
        int loanId = (Integer)this.tableModel.getValueAt(selectedRow, 0);
        String person = this.tableModel.getValueAt(selectedRow, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Warning: Are you sure you want to delete the loan record for " + person + "?", "Confirm Delete", 0, 2);
        if (confirm == 0) {
            this.loanService.deleteLoan(this.username, loanId);
            this.refreshData();
            JOptionPane.showMessageDialog(this, "Loan record deleted successfully.", "Success", 1);
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Loans as CSV");
        fileChooser.setSelectedFile(new File(this.username + "_loans_export.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == 0) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave);){
                pw.println("ID,Person Name,Type,Principal (Total),Paid,Remaining (inc. Interest),Status,Due Date,Interest Rate (%),Interest Type,Monthly EMI,Payments Log");
                for (Loan l : this.allLoans) {
                    pw.println(String.format("%d,%s,%s,%.2f,%.2f,%.2f,%s,%s,%.2f,%s,%.2f,%s", l.getId(), l.getPersonName().replace(",", " "), l.getType(), l.getTotalAmount(), l.getPaidAmount(), l.getRemainingAmount(), l.getStatus(), l.getDueDate() != null ? l.getDueDate().toString() : "", l.getInterestRate(), l.getInterestType(), l.getMonthlyEMI(), l.getPaymentHistoryString()));
                }
                JOptionPane.showMessageDialog(this, "CSV exported successfully!", "Export Success", 1);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage(), "Export Error", 0);
            }
        }
    }

    private void exportTXT() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Loans as TXT Report");
        fileChooser.setSelectedFile(new File(this.username + "_loans_report.txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == 0) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave);){
                pw.println("==========================================================================================");
                pw.println("                           LOANS & BORROWS SUMMARY REPORT                                 ");
                pw.println("==========================================================================================");
                pw.println("User Account: " + this.username);
                pw.println("Date Generated: " + String.valueOf(LocalDate.now()));
                pw.println("Health Score: " + this.calculateLoanHealthScore(this.allLoans) + "/100");
                pw.println("------------------------------------------------------------------------------------------");
                double totalGiven = 0.0;
                double totalTaken = 0.0;
                for (Loan l : this.allLoans) {
                    if (!"Active".equalsIgnoreCase(l.getStatus()) && !"Overdue".equalsIgnoreCase(l.getStatus())) continue;
                    if ("Given".equalsIgnoreCase(l.getType())) {
                        totalGiven += l.getRemainingAmount();
                        continue;
                    }
                    totalTaken += l.getRemainingAmount();
                }
                pw.println(String.format("Total Given Outstanding (inc. Interest): \u20b9%,.2f", totalGiven));
                pw.println(String.format("Total Taken Outstanding (inc. Interest): \u20b9%,.2f", totalTaken));
                pw.println(String.format("Net Outstanding Balance: \u20b9%,.2f", totalGiven - totalTaken));
                pw.println("==========================================================================================");
                pw.println(String.format("%-4s | %-15s | %-6s | %-12s | %-12s | %-12s | %-8s | %-10s", "ID", "Person", "Type", "Principal", "Paid", "Remaining", "Status", "Due Date"));
                pw.println("------------------------------------------------------------------------------------------");
                for (Loan l : this.allLoans) {
                    pw.println(String.format("%-4d | %-15s | %-6s | \u20b9%-11.2f | \u20b9%-11.2f | \u20b9%-11.2f | %-8s | %-10s", l.getId(), l.getPersonName(), l.getType(), l.getTotalAmount(), l.getPaidAmount(), l.getRemainingAmount(), l.getStatus(), l.getDueDate() != null ? l.getDueDate().toString() : "-"));
                }
                pw.println("==========================================================================================");
                JOptionPane.showMessageDialog(this, "TXT Report exported successfully!", "Export Success", 1);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting TXT Report: " + e.getMessage(), "Export Error", 0);
            }
        }
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
