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

    private int statementCounter = 0;

    public boolean addStatement(String accountIBAN, String transactorId,
            String description, double amount,
            double balanceAfter, String type,
            String receiverIBAN) {

        // Έλεγχος τύπου συναλλαγής
        List<String> types = List.of("deposit", "withdraw", "transfer_in", "transfer_out", "fee", "interest");
        if (!types.contains(type.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        if (systemRef.getAccountManager().findAccountByIBAN(accountIBAN) == null) {
            throw new IllegalArgumentException("Invalid account IBAN: " + accountIBAN);
        }

        // Έλεγχος αν υπάρχει ο transactor (user)
        if (systemRef.getUserManager().findUserById(transactorId) == null && !transactorId.equals("BANK")) {
            throw new IllegalArgumentException("Invalid transactor ID: " + transactorId);
        }

        // Έλεγχος ποσού
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Αν είναι deposit ή interest ή transfer και το ποσό είναι αρνητικό -> λάθος
        if (!type.equalsIgnoreCase("withdraw") && amount < 0) {
            throw new IllegalArgumentException("Negative amount is not allowed for non-withdraw transactions");
        }

        if (type.equalsIgnoreCase("transfer_out")) {
            if (receiverIBAN == null || receiverIBAN.isEmpty()) {
                throw new IllegalArgumentException("Receiver IBAN cannot be null for transfer_out transactions");
            }

            // Ελέγχει αν το receiverIBAN υπάρχει στο σύστημα
            if (systemRef.getAccountManager().findAccountByIBAN(receiverIBAN) == null) {
                throw new IllegalArgumentException("Receiver IBAN does not exist: " + receiverIBAN);
            }
        }

        AccountStatement newStatement = new AccountStatement(
                ++statementCounter,
                accountIBAN,
                java.time.LocalDateTime.now(),
                transactorId,
                description,
                type,
                amount,
                balanceAfter,
                receiverIBAN);

        // Αν δεν υπάρχει λίστα για αυτό το IBAN, δημιουργούμε νέα
        if (!statements.containsKey(accountIBAN)) {
            statements.put(accountIBAN, new ArrayList<>());
        }

        // Προσθέτουμε το statement στη λίστα
        List<AccountStatement> accountStatements = statements.get(accountIBAN);

        // tha prepei to statement na mpainei sthn katallhlh thesh
        // pws tha vrw thn katallhlh thesh?
        for (int i = 0; i < accountStatements.size(); i++) {
            AccountStatement st = accountStatements.get(i);
            // thelw na valw to newAccountStatement sthn thesh PRIN apo to prwto
            // statement poy tha vrei kai einai "mikrotero", dld einai "prin" ap'to
            // kainoyrio
            if (newStatement.getTransactionTime().isAfter(st.getTransactionTime())) {
                accountStatements.add(i, newStatement);
                return true;
            }
        }

        accountStatements.add(newStatement);
        return true;
    }

    // Λήψη όλων των statements για IBAN
    public List<AccountStatement> getStatements(String accountIBAN) {
        // Επιστρέφουμε μια νέα λίστα αντιγράφοντας τα στοιχεία της υπάρχουσας λίστας
        if (statements.containsKey(accountIBAN)) {
            return new ArrayList<>(statements.get(accountIBAN));
        }
        return new ArrayList<>();
    }

}