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
            BankAccount senderAccount = systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
            BankAccount receiverAccount = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);

            if (senderAccount == null || receiverAccount == null) {
                throw new IllegalArgumentException("Sender or receiver account does not exist.");
            }

            // 2. Έλεγχος εγκυρότητας ποσού
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }

            // 3. Έλεγχος αν το υπόλοιπο του αποστολέα είναι επαρκές
            if (senderAccount.getBalance() < amount) {
                throw new IllegalArgumentException("Insufficient balance in sender account.");
            }

            // 4. Εκτέλεση μεταφοράς
            senderAccount.removeFromBalance(amount);
            receiverAccount.addToBalance(amount);

            // 5. Καταγραφή συναλλαγών
            AccountStatementManager statementManager = systemRef.getAccountStatementManager();
            statementManager.addStatement(
                    senderIBAN,
                    transactorId,
                    senderDescription,
                    -amount,
                    senderAccount.getBalance(),
                    "transfer_out",
                    receiverIBAN);

            statementManager.addStatement(
                    receiverIBAN,
                    transactorId,
                    receiverDescription,
                    amount,
                    receiverAccount.getBalance(),
                    "transfer_in",
                    senderIBAN);

            executed = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}