package managers;

import models.accounts.BankAccount;
import models.bills.Bill;
import models.users.Admin;

import system.BankSystem;

public class TransactionManager extends Manager {

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

        BankAccount account = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);

        if (account == null) {
            throw new IllegalArgumentException("The account does not exist: " + accountIBAN);
        }
        if (systemRef.getUserManager().findUserById(transactorId) == null
                && !(systemRef.getUserManager().findUserById(transactorId) instanceof Admin)
                && !(transactorId.equals("BANK"))) {
            throw new IllegalArgumentException("User does not exist: " + transactorId);
        }
        if (amount <= 0 || account.getBalance() < amount) {
            throw new IllegalStateException("Invalid amount or insufficient funds.");
        }
        if (!account.getOwnerId().equals(transactorId)) {
            throw new IllegalArgumentException("User is not authorized to withdraw from this account.");
        }

        // Αφαίρεση από το υπόλοιπο του λογαριασμού
        account.removeFromBalance(amount);

        // Καταγραφή συναλλαγής
        systemRef.getAccountStatementManager().addStatement(
                accountIBAN, transactorId, description, amount,
                account.getBalance(), "withdraw", null);

        return true;
    }

    /**
     * Εκτέλεση κατάθεσης
     * 
     * @return true αν η κατάθεση ολοκληρώθηκε επιτυχώς
     */
    public void deposit(String accountIBAN, String transactorId, String description, double amount) throws Exception {
        // Έλεγχος εγκυρότητας λογαριασμού
        BankAccount account = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist: " + accountIBAN);
        }

        // Έλεγχος ταυτότητας χρήστη
        if (systemRef.getUserManager().findUserById(transactorId) == null
                && !(systemRef.getUserManager().findUserById(transactorId) instanceof Admin)
                && !(transactorId.equals("BANK"))) {
            throw new IllegalArgumentException("User does not exist: " + transactorId);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // // Ενημέρωση υπολοίπου
        account.addToBalance(amount);

        // Καταγραφή συναλλαγής
        systemRef.getAccountStatementManager().addStatement(
                accountIBAN,
                transactorId,
                description,
                amount,
                account.getBalance(),
                "deposit",
                null);

    }

    /**
     * Εκτέλεση μεταφοράς
     * 
     * @return true αν η μεταφορά ολοκληρώθηκε επιτυχώς
     */
    public boolean transfer(String senderIBAN, String transactorId, String description, double amount,
            String receiverIBAN) {
        // Έλεγχος εγκυρότητας λογαριασμών
        BankAccount senderAccount = systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
        BankAccount receiverAccount = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);
        if (senderAccount == null) {
            throw new IllegalArgumentException("Sender account does not exist: " + senderIBAN);
        }
        if (receiverAccount == null) {
            throw new IllegalArgumentException("Receiver account does not exist: " + receiverIBAN);
        }

        if (receiverAccount.equals(senderAccount)) {
            throw new IllegalArgumentException("Sender and receiver can't be the same.");
        }
        if (!systemRef.getAccountManager().isOwnerOfBankAccount(senderAccount, transactorId)
                && !(systemRef.getUserManager().findUserById(transactorId) instanceof Admin)
                && !(transactorId.equals("BANK"))) {
            throw new IllegalArgumentException("Transactor should be owner of the account, admin or the bank.");
        }

        // Έλεγχος ταυτότητας χρήστη
        if (systemRef.getUserManager().findUserById(transactorId) == null) {
            throw new IllegalArgumentException("User does not exist: " + transactorId);
        }

        // Έλεγχος υπολοίπου αποστολέα
        if (senderAccount.getBalance() < amount) {
            throw new IllegalStateException("Insufficient funds in sender's account.");
        }

        // Εκτέλεση μεταφοράς
        senderAccount.removeFromBalance(amount);
        receiverAccount.addToBalance(amount);

        // Καταγραφή συναλλαγών
        systemRef.getAccountStatementManager().addStatement(
                senderIBAN, transactorId, description + " to " + receiverIBAN, amount, senderAccount.getBalance(),
                "transfer_out", receiverIBAN);
        systemRef.getAccountStatementManager().addStatement(
                receiverIBAN, transactorId, description + " from " + senderIBAN, amount,
                receiverAccount.getBalance(), "transfer_in", senderIBAN);

        return true;
    }

    /**
     * Εκτέλεση πληρωμής λογαριασμού
     * 
     * @return true αν η πληρωμή ολοκληρώθηκε επιτυχώς
     */
    public boolean pay(String accountIBAN, String transactorId, String description, String RF) {
        // Έλεγχος εγκυρότητας λογαριασμού
        BankAccount account = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        if (account == null) {
            throw new IllegalArgumentException("Account does not exist: " + accountIBAN);
        }

        // Έλεγχος ταυτότητας χρήστη
        if (systemRef.getUserManager().findUserById(transactorId) == null
                && !(systemRef.getUserManager().findUserById(transactorId) instanceof Admin)
                && !(transactorId.equals("BANK"))) {
            throw new IllegalArgumentException("User does not exist: " + transactorId);
        }

        // Εύρεση λογαριασμού πληρωμής
        Bill bill = systemRef.getBillManager().findActiveBillByRF(RF);
        if (bill == null) {
            throw new IllegalArgumentException("Payment bill not found for RF: " + RF);
        }

        // Έλεγχος υπολοίπου
        if (account.getBalance() < bill.getAmount()) {
            throw new IllegalArgumentException("Insufficient funds to pay the bill.");
        }

        // Εκτέλεση πληρωμής
        account.removeFromBalance(bill.getAmount());
        systemRef.getAccountManager().findAccountByIBAN(bill.getBusinessId()).addToBalance(bill.getAmount());

        // Ενημέρωση λογαριασμού πληρωμής
        systemRef.getBillManager().markBillAsPaid(bill.getId());

        // Καταγραφή συναλλαγών
        systemRef.getAccountStatementManager().addStatement(
                accountIBAN, transactorId, description + " (RF: " + RF + ")",
                -bill.getAmount(), account.getBalance(), "payment_out",
                bill.getBusinessId());
        systemRef.getAccountStatementManager().addStatement(
                bill.getBusinessId(), transactorId, "Payment from " + accountIBAN + "(RF: " + RF + ")",
                bill.getAmount(),
                systemRef.getAccountManager().findAccountByIBAN(bill.getBusinessId()).getBalance(),
                "payment_in", accountIBAN);

        return true;
    }

}
