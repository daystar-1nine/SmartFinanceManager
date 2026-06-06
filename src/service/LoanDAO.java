package service;

import model.Loan;
import java.util.List;

/**
 * LoanDAO Interface
 * -----------------
 * Abstraction layer for user loan data persistence.
 */
public interface LoanDAO {

    /**
     * Appends a new loan to the user's data store.
     */
    void addLoan(String username, Loan loan);

    /**
     * Loads all loans for a given username.
     */
    List<Loan> loadLoans(String username);

    /**
     * Overwrites the data store with the provided list of loans.
     */
    void saveAllLoans(String username, List<Loan> loans);
}
