package managers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.Storable;
import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import system.BankSystem;

public class AccountManager extends Manager implements StorageManager {

    private ArrayList<BankAccount> bankAccountList;
    private String accountsFilePath = "data/accounts/accounts.csv";

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

        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }

        // Έλεγχος owner
        User owner = systemRef.getUserManager().findUserById(ownerId);
        if (!(owner instanceof Individual)) {
            throw new IllegalArgumentException("Invalid owner type");
        }

        // Χειρισμός secondary owners (null-safe)
        ArrayList<String> secondOwners = new ArrayList<>();
        if (secondaryOwnerIds != null) {
            for (String secondaryId : secondaryOwnerIds) {
                User secondaryUser = systemRef.getUserManager().findUserById(secondaryId);
                if (!(secondaryUser instanceof Individual)) {
                    throw new IllegalArgumentException("Secondary owner must be Individual");
                }
                if (secondaryId.equals(ownerId)) {
                    throw new IllegalArgumentException("Owner cannot be secondary");
                }
                secondOwners.add(secondaryId);
            }
        }
        String IBAN = generateIBAN(countryCode, "100");
        PersonalAccount account = new PersonalAccount(IBAN, ownerId, interestRate, secondOwners);
        bankAccountList.add(account);
        return true;

    }

    public boolean createBusinessAccount(String ownerId, String countryCode, double interestRate) {
        User user = systemRef.getUserManager().findUserById(ownerId);
        if (user == null) {
            throw new IllegalArgumentException("Owner with ID " + ownerId + " does not exist.");
        }

        // Έλεγχος αν ο χρήστης είναι τύπου Company
        if (!(user instanceof Company)) {
            throw new IllegalArgumentException("Only Company users can own business accounts.");
        }

        // Έλεγχος αν ήδη υπάρχει λογαριασμός για αυτή την εταιρεία
        if (findAccountByBusinessId(ownerId) != null) {
            throw new IllegalStateException("The company already has a business account.");
        }
        String IBAN = generateIBAN(countryCode, "200");
        BusinessAccount account = new BusinessAccount(IBAN, ownerId, interestRate, 5.0);
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
        User user = systemRef.getUserManager().findUserById(businessId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + businessId + " does not exist.");
        }

        // Έλεγχος ότι είναι Company
        if (!(user instanceof Company)) {
            throw new IllegalArgumentException("Only Company users can have a business account.");
        }

        for (BankAccount account : bankAccountList) {
            if (account instanceof BusinessAccount && account.getOwnerId() == businessId) {
                return (BusinessAccount) account;
            }
        }
        return null;
    }

    public ArrayList<PersonalAccount> findAccountsByIndividualId(String individualId) {
        User user = systemRef.getUserManager().findUserById(individualId);
        if (!(user instanceof Individual)) {
            throw new IllegalArgumentException("User with id " + individualId + " is not an Individual.");
        }

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

    public boolean isOwnerOfBankAccount(BankAccount ba, String individualId) {
        if (ba == null || individualId == null) {
            return false;
        }

        // Έλεγχος πρωτεύοντος ιδιοκτήτη
        if (individualId.equals(ba.getOwnerId())) {
            return true;
        }

        // Έλεγχος αν είναι στους δευτερεύοντες ιδιοκτήτες
        if (ba instanceof PersonalAccount) {
            return ((PersonalAccount) ba).getSecondaryOwnerIds().contains(individualId);
        }

        return false;

    }

    @Override
    public void load(Storable s, String filePath) {
        Path path = Path.of(filePath);
        List<String> lines;

        try {
            lines = Files.readAllLines(path);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            String type = line.split(",")[0].split(":")[1];

            BankAccount account = null;

            switch (type) {
                case "PersonalAccount":
                    // account = new PersonalAccount("", "", "", "", "", "");
                    break;
                case "BusinessAccount":
                    // account = new BusinessAccount("", "", "", "", "");
                    break;
                default:
                    System.out.println("Unknown account type: " + type);
                    continue;
            }

            account.unmarshal(line);
            bankAccountList.add(account);
        }
    }

    public ArrayList<BankAccount> getAllAccounts() {
        return new ArrayList<>(bankAccountList);
    }

    @Override
    public void save(Storable s, String filePath, boolean append) {
        if (!(s instanceof BankAccount)) {
            return;
        }

        Path p = Path.of(filePath);

        List<String> lines = null;

        try {
            lines = Files.readAllLines(p);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        lines.add(s.marshal());

        try {
            Files.write(p, lines);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

}