package system;

import java.time.LocalDateTime;
import java.util.ArrayList;

import managers.AccountManager;
import managers.AccountStatementManager;
import managers.BillManager;
import managers.TransactionManager;
import managers.UserManager;
import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.users.User;

public class BankSystem {
    private AccountManager accountManager;
    private AccountStatementManager accountStatementManager;
    private BillManager billManager;
    private TransactionManager transactionManager;
    private UserManager userManager;

    // xronos prosomoiwshs
    LocalDateTime time = LocalDateTime.of(2020, 1, 1, 0, 0);

    public BankSystem() {
        accountManager = new AccountManager(this);
        accountStatementManager = new AccountStatementManager(this);
        billManager = new BillManager(this);
        transactionManager = new TransactionManager(this);
        userManager = new UserManager(this);
        System.out.printf("BankSystem: %s\n", time);
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

    public User register(String type, String username, String password, String legalName, String vat) {
        return userManager.register(type, username, password, legalName, vat);
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void increaseTime(LocalDateTime targetDate) throws Exception {
        // tha prepei:
        // gia kathe mera mexri na teleiwsei o xronos,
        // na ayksaneis kata 1 thn mera toy systhmatos
        // kai na:
        // 1. apodideis epitokia kathe 15 Maioy
        // 2. eisprateis synthrhsh apo epixeirhmatikoys logariasmoys

        if (targetDate.isBefore(time)) {
            throw new IllegalArgumentException("Target date can't be before the current date!");
        }

        while (time.isBefore(targetDate)) {
            // 1.
            time.plusDays(1);
            tryToGiveInterest();
            // 2.
            tryToTaxBusinesses();

        }

    }

    void tryToGiveInterest() {
        if (time.getMonthValue() != 5 || time.getDayOfMonth() != 15) {
            return;
        }
        ArrayList<BankAccount> bankAccounts = getAccountManager().getAllAccounts();

        for (BankAccount b : bankAccounts) {
            // vhma 1: ypologise to epitokio
            double interest = b.getBalance() * b.getInterestRate();

            // vhma 2: dwse toy to epitokio
            b.addToBalance(interest);
        }
    }

    void tryToTaxBusinesses() {
        if (time.getDayOfMonth() != 1) {
            return;
        }

        ArrayList<BankAccount> bankAccounts = getAccountManager().getAllAccounts();

        for (BankAccount b : bankAccounts) {
            if (!(b instanceof BusinessAccount)) {
                continue;
            }

            b.removeFromBalance(((BusinessAccount) b).getMaintenanceFee());
        }
    }
}
