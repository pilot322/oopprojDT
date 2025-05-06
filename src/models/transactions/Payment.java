package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import models.bills.Bill;
import system.BankSystem;

public class Payment extends TwoWay {
    private final String RF;

    public Payment(String transactorId, String senderIBAN,
            String senderDescription,
            String RF, BankSystem systemRef) {

        super(transactorId, senderIBAN, senderDescription, -1,
                "",
                "", systemRef);
        this.RF = RF;

        // me vash to RF tha prepei na vreis to amount, to reiverIBAN kai to
        // receiverDescription
    }

    @Override
    public boolean execute() {
        if (executed) {
            return false; // Η συναλλαγή έχει ήδη εκτελεστεί
        }

        // try {
        // // 1. Εύρεση του λογαριασμού πληρωμής από το RF
        // Bill bill = systemRef.getBillManager().findActiveBillByRF(RF);
        // if (bill == null) {
        // return false; // Λογαριασμός πληρωμής δεν βρέθηκε
        // }

        // // 2. Έλεγχος αν το υπόλοιπο του αποστολέα είναι επαρκές
        // BankAccount senderAccount =
        // systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
        // if (senderAccount == null || senderAccount.getBalance() < bill.getAmount()) {
        // return false; // Ανεπαρκές υπόλοιπο ή λογαριασμός αποστολέα δεν βρέθηκε
        // }

        // // 3. Ενημέρωση υπολοίπου (Αφαίρεση από αποστολέα και προσθήκη στον
        // παραλήπτη)
        // senderAccount.removeFromBalance(bill.getAmount());
        // BankAccount receiverAccount =
        // systemRef.getAccountManager().findAccountByIBAN(bill.getBusinessAccount());
        // receiverAccount.addToBalance(bill.getAmount());

        // // 4. Ενημέρωση της πληρωμής
        // systemRef.getBillManager().markBillAsPaid(bill.getId());

        // // 5. Δημιουργία statement για τον αποστολέα και τον παραλήπτη
        // AccountStatementManager accStmtManager =
        // systemRef.getAccountStatementManager();
        // accStmtManager.addStatement(senderIBAN, transactorId, "PAYMENT_OUT",
        // -bill.getAmount(),
        // senderAccount.getBalance(), "PAYMENT", bill.getBusinessAccount());
        // accStmtManager.addStatement(bill.getBusinessAccount(), transactorId,
        // "PAYMENT_IN", bill.getAmount(),
        // receiverAccount.getBalance(), "PAYMENT", senderIBAN);

        // executed = true;
        // return true;
        // } catch (Exception e) {
        // return false;
        // }
        return true;
    }

    public String getRF() {
        return RF;
    }

}