package managers;

import system.BankSystem;

public class TransactionManager extends Manager{

    public TransactionManager(BankSystem system) {
        super(system);
    }

    /**
     * Εκτέλεση ανάληψης
     * 
     * @return true αν η ανάληψη ολοκληρώθηκε επιτυχώς
     */
    public boolean withdraw(String accountIBAN, String transactorId,
            String description, double amount) {
        throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση κατάθεσης
     * 
     * @return true αν η κατάθεση ολοκληρώθηκε επιτυχώς
     */
    public boolean deposit(String accountIBAN, int transactorId,
            String description, double amount) {
        throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση μεταφοράς
     * 
     * @return true αν η μεταφορά ολοκληρώθηκε επιτυχώς
     */
    public boolean transfer(String senderIBAN, int transactorId,
            String description, double amount,
            String receiverIBAN) {
        throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση πληρωμής λογαριασμού
     * 
     * @return true αν η πληρωμή ολοκληρώθηκε επιτυχώς
     */
    public boolean pay(String accountIBAN, int transactorId,
            String description, String RF) {
        throw new RuntimeException("TODO");
    }
}