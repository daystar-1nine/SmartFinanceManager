package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loan Model Class (Upgraded)
 * ---------------------------
 * Represents a single loan or borrow account, with dynamic interest tracking,
 * overdue date enforcement, monthly EMI metrics, and an internal payment log ledger.
 */
public class Loan {

    private int id;
    private String personName;
    private String type;             // "Given" or "Taken"
    private double totalAmount;      // Principal amount
    private double paidAmount;
    private LocalDate date;          // Start date of the loan
    private String note;
    private String status;           // "Active", "Closed", or "Overdue"

    // New fintech fields
    private LocalDate dueDate;
    private double interestRate;     // Annual rate in % (e.g. 5.0 for 5%)
    private String interestType;     // "Simple" or "Compound"
    private double monthlyEMI;       // Monthly installment amount (optional, 0 if unset)
    private List<Payment> paymentHistory;

    /**
     * Complete Constructor
     */
    public Loan(int id, String personName, String type, double totalAmount, double paidAmount,
                LocalDate date, String note, String status, LocalDate dueDate,
                double interestRate, String interestType, double monthlyEMI, List<Payment> paymentHistory) {
        this.id = id;
        this.personName = personName;
        this.type = type;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.date = date;
        this.note = note;
        this.status = status;
        this.dueDate = dueDate;
        this.interestRate = interestRate;
        this.interestType = interestType == null ? "Simple" : interestType;
        this.monthlyEMI = monthlyEMI;
        this.paymentHistory = paymentHistory == null ? new ArrayList<>() : paymentHistory;
        updateStatus(); // Dynamically align status based on date/amounts
    }

    // ================= GETTERS & SETTERS =================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        updateStatus();
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
        updateStatus();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        // Enforce a dynamic status check whenever requested
        updateStatus();
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        updateStatus();
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
        updateStatus();
    }

    public String getInterestType() {
        return interestType;
    }

    public void setInterestType(String interestType) {
        this.interestType = interestType;
        updateStatus();
    }

    public double getMonthlyEMI() {
        return monthlyEMI;
    }

    public void setMonthlyEMI(double monthlyEMI) {
        this.monthlyEMI = monthlyEMI;
    }

    public List<Payment> getPaymentHistory() {
        return paymentHistory;
    }

    public void setPaymentHistory(List<Payment> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }

    // ================= CALCULATED PROPERTIES =================

    /**
     * Calculates the interest earned/accrued dynamically.
     * Uses daily precision based on days elapsed between loan start date and now.
     */
    public final double getInterestEarned() {
        if (interestRate <= 0.0) {
            return 0.0;
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        double years = Math.max(0.0, daysBetween / 365.0);

        if ("Simple".equalsIgnoreCase(interestType)) {
            return totalAmount * (interestRate / 100.0) * years;
        } else { // Compound compounded annually
            return totalAmount * Math.pow(1.0 + (interestRate / 100.0), years) - totalAmount;
        }
    }

    /**
     * Calculates the total amount payable (Principal + Dynamic Interest).
     */
    public final double getTotalPayable() {
        return totalAmount + getInterestEarned();
    }

    /**
     * Calculates remaining amount (Principal + Dynamic Interest - Paid Amount).
     */
    public final double getRemainingAmount() {
        return Math.max(0.0, getTotalPayable() - paidAmount);
    }

    /**
     * Calculates the estimated date of the next installment payment.
     */
    public LocalDate getNextPaymentDate() {
        return date.plusMonths(paymentHistory.size() + 1);
    }

    /**
     * Auto sets status based on payment progress and deadlines:
     * - Closed: if remaining amount = 0
     * - Overdue: if deadline passed and remaining amount > 0
     * - Active: otherwise
     */
    public final void updateStatus() {
        if (getRemainingAmount() <= 0.0) {
            this.status = "Closed";
        } else if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            this.status = "Overdue";
        } else {
            this.status = "Active";
        }
    }

    // ================= FILE CONVERSION & SERIALIZATION =================

    /**
     * Serializes payment history into a delimited string: amount|date;amount|date;...
     */
    public String getPaymentHistoryString() {
        if (paymentHistory == null || paymentHistory.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paymentHistory.size(); i++) {
            Payment p = paymentHistory.get(i);
            sb.append(p.getAmount()).append("|").append(p.getDate());
            if (i < paymentHistory.size() - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Deserializes payment history from a delimited string.
     */
    public static List<Payment> parsePaymentHistory(String str) {
        List<Payment> list = new ArrayList<>();
        if (str == null || str.trim().isEmpty()) {
            return list;
        }
        try {
            String[] tokens = str.split(";");
            for (String token : tokens) {
                String[] parts = token.split("\\|");
                if (parts.length == 2) {
                    double amt = Double.parseDouble(parts[0].trim());
                    LocalDate dt = LocalDate.parse(parts[1].trim());
                    list.add(new Payment(amt, dt));
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing payment history: " + str);
        }
        return list;
    }

    /**
     * Converts Loan object to CSV file string.
     */
    public String toFileString() {
        String safeName = personName == null ? "" : personName.replace(",", " ");
        String safeNote = note == null ? "" : note.replace(",", " ");
        String dueDateStr = dueDate == null ? "" : dueDate.toString();
        String paymentHistStr = getPaymentHistoryString();

        return id + "," +
                safeName + "," +
                type + "," +
                totalAmount + "," +
                paidAmount + "," +
                date + "," +
                safeNote + "," +
                status + "," +
                dueDateStr + "," +
                interestRate + "," +
                interestType + "," +
                monthlyEMI + "," +
                paymentHistStr;
    }

    /**
     * Restores a Loan object from a CSV file string, maintaining backwards compatibility.
     */
    public static Loan fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            String[] data = line.split(",", -1);
            if (data.length < 8) {
                System.out.println("Invalid loan format: " + line);
                return null;
            }

            // Standard legacy fields
            int id = Integer.parseInt(data[0].trim());
            String personName = data[1].trim();
            String type = data[2].trim();
            double totalAmount = Double.parseDouble(data[3].trim());
            double paidAmount = Double.parseDouble(data[4].trim());
            LocalDate date = LocalDate.parse(data[5].trim());
            String note = data[6].trim();
            String status = data[7].trim();

            // Default fallback settings for upgraded fields (backwards compatibility)
            LocalDate dueDate = date.plusMonths(1);
            double interestRate = 0.0;
            String interestType = "Simple";
            double monthlyEMI = 0.0;
            List<Payment> paymentHistory = new ArrayList<>();

            // Extract upgraded fields if available in file line
            if (data.length >= 9 && !data[8].trim().isEmpty()) {
                dueDate = LocalDate.parse(data[8].trim());
            }
            if (data.length >= 10 && !data[9].trim().isEmpty()) {
                interestRate = Double.parseDouble(data[9].trim());
            }
            if (data.length >= 11 && !data[10].trim().isEmpty()) {
                interestType = data[10].trim();
            }
            if (data.length >= 12 && !data[11].trim().isEmpty()) {
                monthlyEMI = Double.parseDouble(data[11].trim());
            }
            if (data.length >= 13 && !data[12].trim().isEmpty()) {
                paymentHistory = parsePaymentHistory(data[12].trim());
            }

            return new Loan(id, personName, type, totalAmount, paidAmount, date, note, status,
                    dueDate, interestRate, interestType, monthlyEMI, paymentHistory);

        } catch (Exception e) {
            System.out.println("Error parsing loan: " + line);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", personName='" + personName + '\'' +
                ", type='" + type + '\'' +
                ", totalAmount=" + totalAmount +
                ", paidAmount=" + paidAmount +
                ", date=" + date +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                ", interestRate=" + interestRate +
                ", interestType='" + interestType + '\'' +
                ", monthlyEMI=" + monthlyEMI +
                ", paymentHistorySize=" + paymentHistory.size() +
                '}';
    }
}
