package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import models.bills.Bill;
import system.BankSystem;

public class Payment extends TwoWay {
    private final String RF;

    public Payment(String transactorId,
            String senderIBAN,
            String senderDescription,
            String RF,
            BankSystem systemRef) {

        // Τα στοιχεία receiver θα τα συμπληρώσουμε όταν βρούμε το bill
        super(transactorId, senderIBAN, senderDescription,
                0, "", "", systemRef);
        this.RF = RF;
    }

    @Override
    public boolean execute() {
        if (executed) {
            return false; // Ήδη εκτελέστηκε
        }

        try {
            // Εύρεση του bill με βάση το RF
            Bill bill = systemRef.getBillManager().findActiveBillByRF(RF);
            if (bill == null) {
                return false; // Δεν βρέθηκε λογαριασμός
            }

            double billAmount = bill.getAmount();
            String receiverIBAN = bill.getBusinessId();

            // Εύρεση λογαριασμών
            BankAccount senderAccount = systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
            BankAccount receiverAccount = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);

            if (senderAccount == null || receiverAccount == null) {
                return false;
            }

            if (senderAccount.getBalance() < billAmount) {
                return false; // Ανεπαρκές υπόλοιπο
            }

            // Μεταφορά χρημάτων
            senderAccount.removeFromBalance(billAmount);
            receiverAccount.addToBalance(billAmount);

            // Ενημέρωση bill ως πληρωμένο
            systemRef.getBillManager().markBillAsPaid(bill.getId());

            // Καταγραφή κινήσεων
            AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();

            accStmtManager.addStatement(
                    senderIBAN, transactorId, "Payment to " + receiverIBAN + " (RF: " + RF + ")",
                    -billAmount, senderAccount.getBalance(), "PAYMENT_OUT", receiverIBAN);

            accStmtManager.addStatement(
                    receiverIBAN, transactorId, "Payment from " + senderIBAN + " (RF: " + RF + ")",
                    billAmount, receiverAccount.getBalance(), "PAYMENT_IN", senderIBAN);

            executed = true;
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public String getRF() {
        return RF;
    }
}
