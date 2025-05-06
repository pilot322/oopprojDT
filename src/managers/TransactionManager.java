package managers;

import models.accounts.BankAccount;
import models.bills.Bill;
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
        try {
            BankAccount account = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
            if (account == null) {
                throw new Exception("The account does not exist: " + accountIBAN);
            }
            if (systemRef.getUserManager().findUserById(transactorId) == null) {
                throw new Exception("User does not exist: " + transactorId);
            }
            if (amount <= 0 || account.getBalance() < amount) {
                throw new Exception("Invalid amount or insufficient funds.");
            }

            // Αφαίρεση από το υπόλοιπο του λογαριασμού
            account.removeFromBalance(amount);

            // Καταγραφή συναλλαγής
            systemRef.getAccountStatementManager().addStatement(
                    accountIBAN, transactorId, description, amount,
                    account.getBalance(), "WITHDRAW", null);

            return true;
        } catch (Exception e) {
            System.err.println("Withdrawal Error!");
            return false;
        }
    }

    /**
     * Εκτέλεση κατάθεσης
     * 
     * @return true αν η κατάθεση ολοκληρώθηκε επιτυχώς
     */
    public boolean deposit(String accountIBAN, String transactorId, String description, double amount) {
        try {
            // Έλεγχος εγκυρότητας λογαριασμού
            BankAccount account = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
            if (account == null) {
                throw new Exception("Account does not exist: " + accountIBAN);
            }

            // Έλεγχος ταυτότητας χρήστη
            if (systemRef.getUserManager().findUserById(transactorId) == null) {
                throw new Exception("User does not exist: " + transactorId);
            }

            // Ενημέρωση υπολοίπου
            account.addToBalance(amount);

            // Καταγραφή συναλλαγής
            systemRef.getAccountStatementManager().addStatement(
                    accountIBAN,
                    transactorId,
                    description,
                    amount,
                    account.getBalance(),
                    "DEPOSIT",
                    null);

            return true;
        } catch (Exception e) {
            System.err.println("Deposit Error!");
            return false;
        }
    }

    /**
     * Εκτέλεση μεταφοράς
     * 
     * @return true αν η μεταφορά ολοκληρώθηκε επιτυχώς
     */
    public boolean transfer(String senderIBAN, String transactorId, String description, double amount,
            String receiverIBAN) {
        try {
            // Έλεγχος εγκυρότητας λογαριασμών
            BankAccount senderAccount = systemRef.getAccountManager().findAccountByIBAN(senderIBAN);
            BankAccount receiverAccount = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);
            if (senderAccount == null) {
                throw new Exception("Sender account does not exist: " + senderIBAN);
            }
            if (receiverAccount == null) {
                throw new Exception("Receiver account does not exist: " + receiverIBAN);
            }

            // Έλεγχος ταυτότητας χρήστη
            if (systemRef.getUserManager().findUserById(transactorId) == null) {
                throw new Exception("User does not exist: " + transactorId);
            }

            // Έλεγχος υπολοίπου αποστολέα
            if (senderAccount.getBalance() < amount) {
                throw new Exception("Insufficient funds in sender's account.");
            }

            // Εκτέλεση μεταφοράς
            senderAccount.removeFromBalance(amount);
            receiverAccount.addToBalance(amount);

            // Καταγραφή συναλλαγών
            systemRef.getAccountStatementManager().addStatement(
                    senderIBAN, transactorId, description + " to " + receiverIBAN, -amount, senderAccount.getBalance(),
                    "TRANSFER_OUT", receiverIBAN);
            systemRef.getAccountStatementManager().addStatement(
                    receiverIBAN, transactorId, description + " from " + senderIBAN, amount,
                    receiverAccount.getBalance(), "TRANSFER_IN", senderIBAN);

            return true;
        } catch (Exception e) {
            System.err.println("Transfer Error!");
            return false;
        }
    }

    /**
     * Εκτέλεση πληρωμής λογαριασμού
     * 
     * @return true αν η πληρωμή ολοκληρώθηκε επιτυχώς
     */
    public boolean pay(String accountIBAN, String transactorId, String description, String RF) {
        try {
            // // Έλεγχος εγκυρότητας λογαριασμού
            // BankAccount account =
            // systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
            // if (account == null) {
            // throw new Exception("Account does not exist: " + accountIBAN);
            // }

            // // Έλεγχος ταυτότητας χρήστη
            // if (systemRef.getUserManager().findUserById(transactorId) == null) {
            // throw new Exception("User does not exist: " + transactorId);
            // }

            // // Εύρεση λογαριασμού πληρωμής
            // Bill bill = systemRef.getBillManager().findActiveBillByRF(RF);
            // if (bill == null) {
            // throw new Exception("Payment bill not found for RF: " + RF);
            // }

            // // Έλεγχος υπολοίπου
            // if (account.getBalance() < bill.getAmount()) {
            // throw new Exception("Insufficient funds to pay the bill.");
            // }

            // // Εκτέλεση πληρωμής
            // account.removeFromBalance(bill.getAmount());
            // systemRef.getAccountManager().findAccountByIBAN(bill.getBusinessAccount()).addToBalance(bill.getAmount());

            // // Ενημέρωση λογαριασμού πληρωμής
            // systemRef.getBillManager().markBillAsPaid(bill.getId());

            // // Καταγραφή συναλλαγών
            // systemRef.getAccountStatementManager().addStatement(
            // accountIBAN, transactorId, description + " (RF: " + RF + ")",
            // -bill.getAmount(), account.getBalance(), "PAYMENT_OUT",
            // bill.getBusinessAccount());
            // systemRef.getAccountStatementManager().addStatement(
            // bill.getBusinessAccount(), transactorId, "Payment from " + accountIBAN + "
            // (RF: " + RF + ")",
            // bill.getAmount(),
            // systemRef.getAccountManager().findAccountByIBAN(bill.getBusinessAccount()).getBalance(),
            // "PAYMENT_IN", accountIBAN);

            return true;
        } catch (Exception e) {
            System.err.println("Payment Error: " + e.getMessage());
            return false;
        }
    }

}
