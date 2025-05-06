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
        if (executed == true)
            return false;

        // 1. ayksanw to balance toy bank account sto opoio antistoixei to iban kata to
        // // amount

        BankAccount b = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        
        // koitas ti soy epistrefei
        b.removeFromBalance(amount);

        executed = true;

        // 2. dhmioyrgeitai ena account statement
        // xreiazesai to account statement manager
        AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();

        accStmtManager.addStatement(accountIBAN, transactorId, description, amount, b.getBalance(), "withdraw", accountIBAN);

        return true;
    }
}