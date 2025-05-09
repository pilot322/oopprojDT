package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.statements.AccountStatement;
import system.BankSystem;

public class AccountStatementManager extends Manager {
    private HashMap<String, List<AccountStatement>> statements;

    public AccountStatementManager(BankSystem system) {
        super(system);
        this.statements = new HashMap<>();
    }

    // Προσθήκη νέου statement
    public boolean addStatement(String accountIBAN, String transactorId,
            String description, double amount,
            double balanceAfter, String type,
            String receiverIBAN) {

        // AccountStatement newStatement = new AccountStatement(
        // transactorId,
        // description,
        // amount,
        // balanceAfter,
        // type,
        // receiverIBAN);

        // Αν δεν υπάρχει λίστα για αυτό το IBAN, δημιουργούμε νέα
        if (!statements.containsKey(accountIBAN)) {
            statements.put(accountIBAN, new ArrayList<>());
        }

        // Προσθέτουμε το statement στη λίστα
        // statements.get(accountIBAN).add(newStatement);

        return true;
    }

    // Λήψη όλων των statements για IBAN
    public List<AccountStatement> getStatements(String accountIBAN) {
        if (statements.containsKey(accountIBAN)) {
            return statements.get(accountIBAN);
        }
        return new ArrayList<>();
    }

}