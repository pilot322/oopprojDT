package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import system.BankSystem;

public class Transfer extends TwoWay {
    public Transfer(String transactorId, String senderIBAN,
            String senderDescription, double amount,
            String receiverIBAN, String receiverDescription, BankSystem system) {
        super(transactorId, senderIBAN, senderDescription, amount,
                receiverIBAN, receiverDescription, system);
    }

    @Override
    public boolean execute() {
        if (executed) {
            return false; // Η συναλλαγή έχει ήδη εκτελεστεί
        }

        try {
            // 1. Έλεγχος αν οι λογαριασμοί αποστολέα και παραλήπτη υπάρχουν
            // BankAccount senderAccount =
            // systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
            // BankAccount receiverAccount =
            // systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);

            // if (senderAccount == null || receiverAccount == null) {
            // return false; // Λογαριασμός δεν βρέθηκε
            // }

            // // 2. Έλεγχος αν το υπόλοιπο του αποστολέα είναι επαρκές
            // if (senderAccount.getBalance() < amount) {
            // return false; // Ανεπαρκές υπόλοιπο
            // }

            // // 3. Εκτέλεση της μεταφοράς (Αφαίρεση από αποστολέα και προσθήκη στον
            // παραλήπτη)
            // senderAccount.removeFromBalance(amount);
            // receiverAccount.addToBalance(amount);

            // // 4. Δημιουργία statement για τον αποστολέα και τον παραλήπτη
            // AccountStatementManager accStmtManager =
            // systemRef.getAccountStatementManager();
            // accStmtManager.addStatement(senderIBAN, transactorId, senderDescription,
            // -amount, senderAccount.getBalance(), "TRANSFER_OUT", receiverIBAN);
            // accStmtManager.addStatement(receiverIBAN, transactorId, receiverDescription,
            // amount, receiverAccount.getBalance(), "TRANSFER_IN", senderIBAN);

            // executed = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}