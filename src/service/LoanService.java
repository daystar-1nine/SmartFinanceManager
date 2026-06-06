package service;

import model.Loan;
import model.Payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * LoanService Class
 * -----------------
 * Coordinates business rules for loans and payments.
 * Integrates constructor dependency injection for LoanDAO and in-memory cache.
 */
public class LoanService {

    private static final Logger LOGGER = Logger.getLogger(LoanService.class.getName());
    private final LoanDAO loanDAO;
    private final Map<String, List<Loan>> loanCache = new HashMap<>();

    /**
     * Dependency Injected Constructor
     */
    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    /**
     * Appends a new loan and updates local cache.
     */
    public synchronized void addLoan(String username, Loan loan) {
        if (loan == null) {
            LOGGER.warning("Null loan passed. Skipping add.");
            return;
        }
        loanDAO.addLoan(username, loan);
        if (loanCache.containsKey(username)) {
            loanCache.get(username).add(loan);
        }
    }

    /**
     * Loads loans for a given username using in-memory caching.
     */
    public synchronized List<Loan> loadLoans(String username) {
        if (username == null) return new ArrayList<>();
        if (loanCache.containsKey(username)) {
            return new ArrayList<>(loanCache.get(username));
        }
        List<Loan> loans = loanDAO.loadLoans(username);
        loanCache.put(username, loans);
        return new ArrayList<>(loans);
    }

    /**
     * Saves updated list of loans and refreshes cache.
     */
    public synchronized void saveAllLoans(String username, List<Loan> loans) {
        loanDAO.saveAllLoans(username, loans);
        loanCache.put(username, new ArrayList<>(loans));
    }

    /**
     * Updates the paid amount of a specific loan, logging the payment.
     */
    public synchronized void updateLoanPayment(String username, int loanId, double paymentAmount) {
        List<Loan> loans = loadLoans(username);
        boolean updated = false;
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                double maxPayable = loan.getTotalPayable();
                double newPaid = loan.getPaidAmount() + paymentAmount;
                
                // Cap the paid amount to avoid overpayment
                if (newPaid > maxPayable) {
                    paymentAmount = Math.max(0.0, maxPayable - loan.getPaidAmount());
                    newPaid = maxPayable;
                }
                
                loan.setPaidAmount(newPaid);
                
                // Record the payment in history if positive
                if (paymentAmount > 0.0) {
                    loan.getPaymentHistory().add(new Payment(paymentAmount, LocalDate.now()));
                }
                updated = true;
                break;
            }
        }
        if (updated) {
            saveAllLoans(username, loans);
        }
    }

    /**
     * Marks a specific loan as fully paid.
     */
    public synchronized void markAsPaid(String username, int loanId) {
        List<Loan> loans = loadLoans(username);
        boolean updated = false;
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                double remaining = loan.getRemainingAmount();
                if (remaining > 0.0) {
                    loan.setPaidAmount(loan.getTotalPayable());
                    loan.getPaymentHistory().add(new Payment(remaining, LocalDate.now()));
                    updated = true;
                }
                break;
            }
        }
        if (updated) {
            saveAllLoans(username, loans);
        }
    }

    /**
     * Deletes a loan by ID.
     */
    public synchronized void deleteLoan(String username, int loanId) {
        List<Loan> loans = loadLoans(username);
        boolean removed = loans.removeIf(loan -> loan.getId() == loanId);
        if (removed) {
            saveAllLoans(username, loans);
        }
    }

    /**
     * Clears local in-memory cache for a user.
     */
    public synchronized void clearCache(String username) {
        loanCache.remove(username);
    }
}
