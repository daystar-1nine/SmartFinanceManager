package model;

import java.time.LocalDate;

/**
 * Payment Model Class
 * -------------------
 * Represents a single payment recorded toward a loan.
 */
public class Payment {

    private double amount;
    private LocalDate date;

    /**
     * Constructor for a payment event
     */
    public Payment(double amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "amount=" + amount +
                ", date=" + date +
                '}';
    }
}
