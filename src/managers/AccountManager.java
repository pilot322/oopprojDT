package managers;

import java.util.ArrayList;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import system.BankSystem;

public class AccountManager extends Manager{

    public AccountManager(BankSystem systemRef) {
        super(systemRef);
    }

    private ArrayList<BankAccount> bankAccountList = new ArrayList<>();



    public boolean createPersonalAccount(int ownerId, String countryCode, double interestRate,
            ArrayList<Integer> secondaryOwnerIds) {
        throw new RuntimeException("TODO");

    }

    // ftiakse synarthsh gia thn dhmioyrgia business bank account
    public BankAccount findAccountByIBAN(String IBAN) {
        for (BankAccount account : bankAccountList) {
            if (account.getIBAN().equals(IBAN)) {
                return account;
            }
        }
        return null;
    }

    public BusinessAccount findAccountByBusinessId(String businessId) {
        throw new RuntimeException("TODO");
    }

    public ArrayList<PersonalAccount> findAccountsByIndividualId(String individualId) {
        throw new RuntimeException("TODO");
    }
}
