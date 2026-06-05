package model;

import java.time.LocalDate;

/**
 * Transaction Model Class
 * -----------------------
 * Represents a single financial transaction.
 *
 * This class:
 * - Stores transaction details
 * - Converts object → file (CSV format)
 * - Converts file → object safely
 */
public class Transaction {

    // ================= FIELDS =================

    private int id;
    private String type;      // "Income" or "Expense"
    private double amount;
    private String category;
    private String note;
    private LocalDate date;

    // ================= CONSTRUCTOR =================

    /**
     * Creates a new Transaction object
     */
    public Transaction(int id,
                       String type,
                       double amount,
                       String category,
                       String note,
                       LocalDate date) {

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }

    // ================= GETTERS =================

    public int getId() { return id; }

    public String getType() { return type; }

    public double getAmount() { return amount; }

    public String getCategory() { return category; }

    public String getNote() { return note; }

    public LocalDate getDate() { return date; }

    // ================= FILE CONVERSION =================

    /**
     * Converts Transaction → CSV string
     *
     * Important:
     * - Removes commas from note to prevent CSV breaking
     */
    public String toFileString() {

        String safeNote = note == null ? "" : note.replace(",", " ");

        return id + "," +
                type + "," +
                amount + "," +
                category + "," +
                safeNote + "," +
                date;
    }

    /**
     * Converts CSV string → Transaction object
     *
     * Safe parsing:
     * - Handles invalid lines
     * - Prevents app crash
     */
    public static Transaction fromFileString(String line) {

        // Ignore empty lines
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            String[] data = line.split(",");

            // Validate format
            if (data.length != 6) {
                System.out.println("Invalid transaction format: " + line);
                return null;
            }

            int id = Integer.parseInt(data[0].trim());
            String type = data[1].trim();
            double amount = Double.parseDouble(data[2].trim());
            String category = data[3].trim();
            String note = data[4].trim();
            LocalDate date = LocalDate.parse(data[5].trim());

            return new Transaction(id, type, amount, category, note, date);

        } catch (Exception e) {
            System.out.println("Error parsing transaction: " + line);
            return null;
        }
    }

    // ================= DEBUG SUPPORT =================

    /**
     * Useful for debugging/logging
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", date=" + date +
                '}';
    }
}