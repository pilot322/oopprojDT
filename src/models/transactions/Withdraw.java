package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import system.BankSystem;

public class Withdraw extends Transaction {
    public Withdraw(String transactorId, String accountIBAN, String description, double amount, BankSystem system) {
        super(transactorId, accountIBAN, description, amount, system);
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

            // 2. Έλεγχος αν το υπόλοιπο είναι επαρκές
            if (b.getBalance() < amount) {
                return false; // Ανεπαρκές υπόλοιπο
            }

            // 3. Μείωση του υπολοίπου του λογαριασμού
            b.removeFromBalance(amount);

            // 4. Δημιουργία statement
            AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();
            accStmtManager.addStatement(accountIBAN, transactorId, description, -amount, b.getBalance(), "WITHDRAWAL",
                    accountIBAN);

            executed = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}