package system;

import managers.AccountManager;
import managers.AccountStatementManager;
import managers.BillManager;
import managers.TransactionManager;
import managers.UserManager;
import models.users.User;

public class BankSystem {
    private AccountManager accountManager;
    private AccountStatementManager accountStatementManager;
    private BillManager billManager;
    private TransactionManager transactionManager;
    private UserManager userManager;

    public BankSystem(){
        accountManager = new AccountManager(this);
        accountStatementManager = new AccountStatementManager(this);
        billManager = new BillManager(this);
        transactionManager = new TransactionManager(this);
        userManager = new UserManager(this);
    }


    public AccountManager getAccountManager() {
        return accountManager;
    }
    public AccountStatementManager getAccountStatementManager() {
        return accountStatementManager;
    }
    public BillManager getBillManager() {
        return billManager;
    }
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
    public UserManager getUserManager() {
        return userManager;
    }


    public User register(String type, String username, String password, String legalName, String vat){
        return userManager.register(type, username, password, legalName, vat);
    }


}
