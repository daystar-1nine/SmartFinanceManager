package service;

import model.Transaction;

import java.io.*;
import java.util.List;

/**
 * ReportService
 * -------------
 * Handles exporting transaction data to files.
 *
 * Supports:
 * - TXT export
 * - CSV export
 */
public class ReportService {

    /**
     * Export transactions as TXT file
     */
    public void exportToTXT(File file, List<Transaction> transactions) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write("SMART FINANCE MANAGER REPORT");
            writer.newLine();
            writer.write("====================================");
            writer.newLine();

            for (Transaction t : transactions) {

                writer.write(
                        "ID: " + t.getId() +
                                " | Type: " + t.getType() +
                                " | Amount: ₹" + t.getAmount() +
                                " | Category: " + t.getCategory() +
                                " | Note: " + t.getNote() +
                                " | Date: " + t.getDate()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error exporting TXT: " + e.getMessage());
        }
    }

    /**
     * Export transactions as CSV file
     */
    public void exportToCSV(File file, List<Transaction> transactions) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // Header
            writer.write("ID,Type,Amount,Category,Note,Date");
            writer.newLine();

            // Data
            for (Transaction t : transactions) {
                String note = t.getNote();
                String safeNote = note == null ? "" : note.replace(",", " ");

                writer.write(
                        t.getId() + "," +
                                t.getType() + "," +
                                t.getAmount() + "," +
                                t.getCategory() + "," +
                                safeNote + "," +
                                t.getDate()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error exporting CSV: " + e.getMessage());
        }
    }
}