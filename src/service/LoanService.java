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
 * <h2>LoanService</h2>
 * <p>
 * This class coordinates the core business logic, validation rules, and payment histories
 * for loans (debts given to or taken from others).
 * </p>
 * 
 * <h3>Architecture Role:</h3>
 * <p>
 * Located in the <b>Service Layer</b>. It relies on the {@link LoanDAO} abstraction interface
 * (manual Dependency Injection) to save and load data from disk, keeping the business logic
 * separated from the storage mechanisms.
 * </p>
 * 
 * <h3>Design & Concurrency:</h3>
 * <ul>
 *   <li><b>In-Memory Caching:</b> Stores user-specific loan lists in a private {@link Map} to avoid constant CSV parsing on disk.</li>
 *   <li><b>Thread Safety:</b> Methods are declared as `synchronized` to ensure sequential, thread-safe access
 *       when invoked from UI listeners and concurrent SwingWorker background tasks.</li>
 * </ul>
 * 
 * <h3>Key Business Rules:</h3>
 * <ul>
 *   <li><b>Payment Capping:</b> Overpayment check to ensure paid amount never exceeds total payable sum (principal + interest).</li>
 *   <li><b>History Tracking:</b> Every payment generates a nested {@link Payment} record tracking date and exact amount.</li>
 * </ul>
 * 
 * @see Loan
 * @see LoanDAO
 * @see Payment
 */
public class LoanService {

    /**
     * Logger instance for consolidating warnings and data operation diagnostics.
     */
    private static final Logger LOGGER = Logger.getLogger(LoanService.class.getName());
    
    /**
     * The persistence layer injected via constructor.
     */
    private final LoanDAO loanDAO;
    
    /**
     * Cache storage to cache loaded loans by username to optimize retrieval performance.
     */
    private final Map<String, List<Loan>> loanCache = new HashMap<>();

    /**
     * Constructs a LoanService injecting a specific {@link LoanDAO} implementation.
     * 
     * @param loanDAO The Data Access Object responsible for files/storage.
     */
    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    /**
     * Appends a new loan record to the repository and updates the cache.
     * 
     * @param username The owner of the loan profile.
     * @param loan The Loan entity containing principal, rate, interest types, and status.
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
     * Retrieves loans associated with a user, using cache if populated, otherwise reads from disk.
     * 
     * @param username The user identifier.
     * @return List copy of user loans (never null).
     */
    public synchronized List<Loan> loadLoans(String username) {
        if (username == null) return new ArrayList<>();
        
        // Cache Hit check
        if (loanCache.containsKey(username)) {
            return new ArrayList<>(loanCache.get(username));
        }
        
        // Cache Miss check: Fetch from DAO layer
        List<Loan> loans = loanDAO.loadLoans(username);
        loanCache.put(username, loans);
        return new ArrayList<>(loans);
    }

    /**
     * Overwrites all loan records for the specified user and resets the local cache.
     * 
     * @param username The owner of the loans.
     * @param loans The updated list of all user loans.
     */
    public synchronized void saveAllLoans(String username, List<Loan> loans) {
        loanDAO.saveAllLoans(username, loans);
        loanCache.put(username, new ArrayList<>(loans));
    }

    /**
     * Adds a partial payment to a loan, ensuring the balance doesn't exceed the total payable amount.
     * 
     * @param username The owner of the loan.
     * @param loanId The unique identifier of the target loan.
     * @param paymentAmount The amount paid.
     */
    public synchronized void updateLoanPayment(String username, int loanId, double paymentAmount) {
        List<Loan> loans = loadLoans(username);
        boolean updated = false;
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                // TODO: Consider extracting calculation logic to Loan domain class
                double maxPayable = loan.getTotalPayable();
                double newPaid = loan.getPaidAmount() + paymentAmount;
                
                // Edge Case: Prevent overpayment by capping new paid value to max payable
                if (newPaid > maxPayable) {
                    paymentAmount = Math.max(0.0, maxPayable - loan.getPaidAmount());
                    newPaid = maxPayable;
                }
                
                loan.setPaidAmount(newPaid);
                
                // Record the payment event in history if payment amount is positive
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
     * Marks a specific loan as fully paid by setting paid amount equal to total payable.
     * Records a payment history log with the remaining balance.
     * 
     * @param username The owner of the loan.
     * @param loanId The unique identifier of the target loan.
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
     * Removes a loan by ID, writing updates back to disk.
     * 
     * @param username The owner of the loan.
     * @param loanId The unique identifier of the target loan.
     */
    public synchronized void deleteLoan(String username, int loanId) {
        List<Loan> loans = loadLoans(username);
        boolean removed = loans.removeIf(loan -> loan.getId() == loanId);
        if (removed) {
            saveAllLoans(username, loans);
        }
    }

    /**
     * Invalidates the in-memory cache for the specified user.
     * Used mainly during logout sequences.
     * 
     * @param username The user whose cache should be invalidated.
     */
    public synchronized void clearCache(String username) {
        loanCache.remove(username);
    }
}
