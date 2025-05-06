package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import system.BankSystem;

public class Deposit extends Transaction {
    public Deposit(String transactorId, String accountIBAN, String description, double amount, BankSystem systemRef) {
        super(transactorId, accountIBAN, description, amount, systemRef);
    }

    @Override
    public boolean execute() {
        if (executed) {
            return false; // Η συναλλαγή έχει ήδη εκτελεστεί
        }

        try {
            // 1. Έλεγχος αν ο λογαριασμός υπάρχει
            BankAccount b = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
            if (b == null) {
                return false; // Λογαριασμός δεν βρέθηκε
            }

            // 2. Αύξηση του υπολοίπου του λογαριασμού
            b.addToBalance(amount);

            // 3. Δημιουργία statement
            AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();
            accStmtManager.addStatement(accountIBAN, transactorId, description, amount, b.getBalance(), "DEPOSIT",
                    accountIBAN);

            executed = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}