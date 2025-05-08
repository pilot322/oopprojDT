package managers;

import java.util.ArrayList;
import java.util.Random;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import system.BankSystem;

public class AccountManager extends Manager {

    private ArrayList<BankAccount> bankAccountList;

    public AccountManager(BankSystem systemref) {
        super(systemref);
        bankAccountList = new ArrayList<>();

    }

    private String generateIBAN(String countryCode, String accountTypeCode) {
        if (countryCode.length() != 2 || accountTypeCode.length() != 3) {
            throw new IllegalArgumentException("Country code is a 2 and account type code is a 3 digit number");
        }

        Random rand = new Random();
        String IBAN;

        do {

            StringBuilder random15Digits = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                random15Digits.append(rand.nextInt(10));
            }
            IBAN = countryCode + accountTypeCode + random15Digits.toString();
        } while (ibanExists(IBAN));

        return IBAN;
    }

    private boolean ibanExists(String IBAN) {
        for (BankAccount account : bankAccountList) {
            if (account.getIBAN().equals(IBAN)) {
                return true;
            }
        }
        return false;
    }

    public boolean createPersonalAccount(String ownerId, String countryCode, double interestRate,
            ArrayList<String> secondaryOwnerIds) {
        String IBAN = generateIBAN(countryCode, "100");
        PersonalAccount account = new PersonalAccount(IBAN, ownerId, interestRate, secondaryOwnerIds);
        bankAccountList.add(account);
        return true;

    }

    public boolean createBusinessAccount(String ownerId, String countryCode, double interestRate,
            double maintenanceFee) {
        if (findAccountByBusinessId(ownerId) != null) {
            throw new IllegalStateException("The company already has a business account");
        }
        String IBAN = generateIBAN(countryCode, "200");
        BusinessAccount account = new BusinessAccount(IBAN, ownerId, interestRate, maintenanceFee);
        bankAccountList.add(account);
        return true;
    }

    public BankAccount findAccountByIBAN(String IBAN) {
        for (BankAccount account : bankAccountList) {
            if (account.getIBAN().equals(IBAN)) {
                return account;
            }
        }
        return null;
    }

    public BusinessAccount findAccountByBusinessId(String businessId) {
        for (BankAccount account : bankAccountList) {
            if (account instanceof BusinessAccount && account.getOwnerId() == businessId) {
                return (BusinessAccount) account;
            }
        }
        return null;
    }

    public ArrayList<PersonalAccount> findAccountsByIndividualId(String individualId) {
        ArrayList<PersonalAccount> results = new ArrayList<PersonalAccount>();
        for (BankAccount account : bankAccountList) {
            // type check
            if (account instanceof PersonalAccount) {
                // casting to acc gia na mhn exoume prosvasi stis methodous
                PersonalAccount personalAccount = (PersonalAccount) account;
                if (personalAccount.getOwnerId() == individualId
                        || personalAccount.getSecondaryOwnerIds().contains(individualId)) {
                    results.add(personalAccount);
                }
            }
        }
        return results;
    }

    public boolean isOwnerOfBankAccount(String individualId){
        return false; 
    }
}
