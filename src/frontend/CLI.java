package frontend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import models.users.Individual;
import models.users.Company;
import models.users.Costumer;
import models.users.Admin;
import models.users.User;
import models.accounts.BankAccount;
import models.accounts.PersonalAccount;
import models.bills.Bill;
import models.statements.AccountStatement;
import models.accounts.BusinessAccount;
import managers.UserManager;
import managers.AccountManager;
import managers.BillManager;

import system.BankSystem;

public class CLI {

    public static final Scanner input = new Scanner(System.in);
    public static BankSystem system = new BankSystem();
    static User currentUser;
    static BankAccount currentBankAccount;

    public static void main(String[] args) {
        // test logic
        // User user1 = system.getUserManager().register("Individual", "test", "abcd", "Test1234", "123456789");
        // system.getAccountManager().createPersonalAccount(user1.getId(), "GR", 1, null);

        // User user2 = system.getUserManager().register("Company", "test2", "abcd", "Test1234", "987654321");
        // system.getAccountManager().createBusinessAccount(user2.getId(), "AL", 10);

        // User user3 = system.getUserManager().register("Admin", "admin", "abcd", "Test1234", null);

        // ontws to CLI ksekinaei edw
        while (true) {
            currentUser = null; // reset user

            promptToAuthenticate(); // login or register

            if (currentUser == null) {
                System.out.println("Exiting...");
                break;
            }

            if (currentUser instanceof Individual) {
                startIndividualMenu();
            } else if (currentUser instanceof Company) {
                companyOperationsMenu();
            } else if (currentUser instanceof Admin) {
                startAdminMenu();
            }

            // Όταν βγεις από ένα menu, επιστρέφεις εδώ και ξαναδείχνεις login
            System.out.println("\nLogged out. Returning to main menu...\n");
        }

        system.save();
    }

    private static void promptToAuthenticate() {
        System.out.println("Bank Of Tuc");
        System.out.println("1. log in");
        System.out.println("2. Registration");
        System.out.println("3. Exit");
        System.out.print("Choice: ");
        int choice;
        do {
            choice = input.nextInt();
            input.nextLine();
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegistration();
                    handleLogin();
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Non valid choice");
                    // System.exit(0);

            }
        } while (!(choice == 1 || choice == 2 || choice == 3));
    }

    private static void handleLogin() {
        // apeires epanalupseis mexri na ginei to login h exit?
        while (true) {
            System.out.print("Username / Company name / Admin name: ");
            String username = input.nextLine();

            System.out.print("\nPassword: ");
            String password = input.nextLine();

            currentUser = system.getUserManager().login(username, password);

            if (currentUser == null) {
                System.out.println("Wrong username or password.");

                System.out.print("Try Again (yes/no): ");
                String choice = input.nextLine().trim().toLowerCase();

                if (!choice.equals("yes")) {
                    System.out.println("Exit");
                    return;
                }

            } else {
                System.out.println("Logged in successfully " + currentUser.getLegalName());
                break;
            }
        }
    }

    private static void handleRegistration() {
        System.out.println("\nUser type:");
        System.out.println("1. Individual");
        System.out.println("2. Company");
        System.out.println("3. Admin");
        System.out.print("Choice: ");

        int typeChoice = input.nextInt();
        input.nextLine(); // Clear buffer

        String type;
        switch (typeChoice) {
            case 1:
                type = "Individual";
                break;
            case 2:
                type = "Company";
                break;
            case 3:
                type = "Admin";
                break;
            default:
                System.out.println("Invalid choice. Defaulting to Individual.");
                type = "Individual";
        }

        System.out.print("Name and surname/Company name: ");
        String legalName = input.nextLine();

        System.out.print("Username: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        // VAT only required for Individual/Company
        String vat = null;
        if (!type.equals("Admin")) {
            System.out.print("VAT (9 digits): ");
            vat = input.nextLine();
        }

        try {
            currentUser = system.getUserManager().register(type, username, password, legalName, vat);
            System.out.println("Registration completed! User id: " + currentUser.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void startIndividualMenu() {

        Individual individual = (Individual) currentUser;
        ArrayList<PersonalAccount> accounts = system.getAccountManager().findAccountsByIndividualId(individual.getId());

        while (true) {
            System.out.println("\n=== Your Accounts ===");
            for (int i = 0; i < accounts.size(); i++) {
                System.out.printf("%d. %s (Balance: %.2f€)\n", i + 1, accounts.get(i).getIBAN(),
                        accounts.get(i).getBalance());
            }
            System.out.println("-1. Exit");
            System.out.println("0. Create new personal account.");

            System.out.print("Select account: ");

            try {
                int choice = input.nextInt();
                input.nextLine(); // Clear buffer

                switch (choice) {
                    case 0:
                        createPersonalAccount(individual);
                        // bazw ton kainourio logariamo sthn lista
                        accounts = system.getAccountManager().findAccountsByIndividualId(individual.getId());

                        break;
                    case -1:
                        return;

                    default:
                        if (choice > 0 && choice <= accounts.size()) {
                            PersonalAccount selectedAccount = accounts.get(choice - 1);
                            accountOperationsMenu(selectedAccount);
                        } else {
                            System.out.println("Invalid choice. Please try again.");
                        }
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                input.nextLine(); // Clear invalid input
            }
        }
    }

    private static void accountOperationsMenu(PersonalAccount account) {
        while (true) {
            System.out.println("\n=== Account: " + account.getIBAN() + " ===");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. Bill Payment");
            System.out.println("6. Account Details");
            System.out.println("7. Show Account Statemets");
            System.out.println("8. View Active Bills You Owe");
            System.out.println("9. Back to Accounts");
            System.out.print("Choose action: ");

            try {
                int action = input.nextInt();
                input.nextLine(); // Clear buffer

                switch (action) {
                    case 1:
                        System.out.printf("Current Balance: %.2f€\n", account.getBalance());
                        break;

                    case 2:
                        handleDeposit(account);
                        break;

                    case 3:
                        handleWithdraw(account);
                        break;

                    case 4:
                        handleTransfer(account);
                        break;

                    case 5:
                        handlePayment(account);
                        break;

                    case 6:
                        System.out.println("IBAN: " + account.getIBAN());
                        System.out.println("Owner: " + account.getOwnerId());
                        System.out.printf("Balance: %.2f€\n", account.getBalance());
                        System.out.println("Interest Rate: " + account.getInterestRate() + "%");
                        System.out.println("Secondary Owners: " + account.getSecondaryOwnerIds());
                        break;

                    case 7:
                        showAccountStatements(account);
                        break;

                    case 8:
                        viewActiveBillsForCurrentUser();
                        break;

                    case 9:
                        return; // Return to accounts list

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                input.nextLine();
            }
        }
    }

    // Dhmiourgia kainourio personal apo startIndividualMenu
    private static void createPersonalAccount(Individual user) {
        try {
            System.out.print("Enter country code (2 letters): ");
            String countryCode = input.nextLine().toUpperCase();

            System.out.print("Enter interest rate : ");
            double interestRate = input.nextDouble();
            input.nextLine(); // Clear buffer

            ArrayList<String> secondaryOwners = new ArrayList<>();
            System.out.print("Add secondary owners? y/n: ");
            if (input.nextLine().equalsIgnoreCase("y")) {
                System.out.println("Enter secondary owner IDs (comma separated):");
                String[] ids = input.nextLine().split(",");
                for (String id : ids) {
                    secondaryOwners.add(id.trim());
                }
            }

            system.getAccountManager().createPersonalAccount(user.getId(), countryCode, interestRate, secondaryOwners);

            System.out.println("Personal account created successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    private static void showAccountStatements(BankAccount account) {
        List<AccountStatement> statements = system.getAccountStatementManager().getStatements(account.getIBAN());

        if (statements.isEmpty()) {
            System.out.println("No transaction history available.");
            return;
        }

        System.out.println("\n=== Transaction History for " + account.getIBAN() + " ===");

        for (AccountStatement st : statements) {
            System.out.println(st);
        }
    }

    public static void companyOperationsMenu() {
        Company company = (Company) currentUser;
        currentBankAccount = system.getAccountManager().findAccountByBusinessId(company.getId());

        if (currentBankAccount == null) {
            System.out.println(" No business account found for this company.");
            System.out.println("Creating one now...");

            createBusinessAccount(company);

            currentBankAccount = system.getAccountManager().findAccountByBusinessId(company.getId());

            // in case createBusinessAccount fails
            if (currentBankAccount == null) {
                System.out.println("Failed to create business account. Returning to main menu.");
                return;
            }
        }

        while (true) {

            int choice;

            System.out.println("\n=== Company Account ===");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. View Transactions");
            System.out.println("4. Transfer Money");
            System.out.println("5. Bill Payment");
            System.out.println("6. Account Details");
            System.out.println("7. View Active Bills You Owe");
            System.out.println("8. View all Active Bills You have Issued");
            System.out.println("9. Handle Issue Bill");
            System.out.println("10. Back to Main Menu");
            System.out.print("Choose action: ");

            choice = input.nextInt();

            switch (choice) {
                case 1:
                    handleDeposit(currentBankAccount);
                    break;

                case 2:
                    handleWithdraw(currentBankAccount);
                    break;

                case 3:
                    System.out.println("=== Transaction History ===");
                    break;

                case 4:
                    handleTransfer(currentBankAccount);
                    break;

                case 5:
                    handlePayment(currentBankAccount);
                    break;

                case 6:
                    System.out.println("=== Account Details ===");
                    System.out.println("IBAN: " + currentBankAccount.getIBAN());
                    System.out.println("Company ID: " + currentBankAccount.getOwnerId());
                    System.out.printf("Balance: %.2f€\n", currentBankAccount.getBalance());
                    System.out.println("Interest Rate: " + currentBankAccount.getInterestRate() + "%");
                    System.out.println(
                            "Monthly Fee: " + ((BusinessAccount) currentBankAccount).getMaintenanceFee() + "€");
                    break;

                case 7:
                    viewActiveBillsForCurrentUser();
                    break;

                case 8:
                    viewIssuedBillsByCompany();
                    break;

                case 9:
                    handleIssueBill(company);
                    break;

                case 10:
                    return; // return to menu

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        }
    }

    private static void createBusinessAccount(Company company) {
        try {
            System.out.print("Enter country code (2 letters): ");
            String countryCode = input.nextLine().toUpperCase();

            System.out.print("Enter interest rate (e.g. 1.5): ");
            double interestRate = input.nextDouble();

            system.getAccountManager().createBusinessAccount(company.getId(), countryCode, interestRate);

            System.out.println("Business account created successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // HANDLE TRANSACTIONS

    private static void handleDeposit(BankAccount account) {
        try {
            System.out.print("Amount to deposit: ");
            double depositAmount = input.nextDouble();
            input.nextLine(); // καθαρισμός buffer

            if (depositAmount <= 0) {
                System.out.println(" Deposit amount must be positive.");
                return;
            }

            System.out.print("Description: ");
            String description = input.nextLine();
            try {
                system.getTransactionManager().deposit(account.getIBAN(), currentUser.getId(), description,
                        depositAmount);

                System.out.printf(" Deposit successful. New balance: %.2f€\n", account.getBalance());
            }

            catch (Exception e) {
                System.out.println(" Deposit failed. Reason: " + e.getMessage());
            }

        } catch (

        Exception e) {
            System.out.println(" Error: " + e.getMessage());
            input.nextLine(); // Clear buffer
        }
    }

    private static void handleWithdraw(BankAccount account) {
        try {
            System.out.print("Amount to withdraw: ");
            double withdrawAmount = input.nextDouble();
            input.nextLine(); // clear buffer

            if (withdrawAmount <= 0) {
                System.out.println(" Withdrawal amount must be positive.");
                return;
            }

            if (withdrawAmount > account.getBalance()) {
                System.out.println(" Insufficient balance.");
                return;
            }

            System.out.print("Description: ");
            String description = input.nextLine();

            try {
                system.getTransactionManager().withdraw(
                        account.getIBAN(),
                        currentUser.getId(),
                        description,
                        withdrawAmount);

                System.out.printf(" Withdrawal successful. New balance: %.2f€\n", account.getBalance());
            } catch (Exception e) {
                System.out.println(" Withdrawal failed. Reason: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
            input.nextLine(); // clear input buffer
        }
    }    

    private static void handleTransfer(BankAccount senderAccount) {
        try {
            System.out.print("Receiver IBAN: ");
            String receiverIBAN = input.nextLine().trim();

            if (receiverIBAN.equalsIgnoreCase(senderAccount.getIBAN())) {
                System.out.println(" Cannot transfer to the same account.");
                return;
            }

            BankAccount receiverAccount = system.getAccountManager().findAccountByIBAN(receiverIBAN);
            if (receiverAccount == null) {
                System.out.println(" No account found with that IBAN.");
                return;
            }

            System.out.print("Amount to transfer: ");
            double amount = input.nextDouble();
            input.nextLine(); // Clear buffer

            if (amount <= 0) {
                System.out.println(" Amount must be greater than zero.");
                return;
            }

            if (amount > senderAccount.getBalance()) {
                System.out.println(" Insufficient balance.");
                return;
            }

            System.out.print("Description: ");
            String description = input.nextLine();

            try {
                system.getTransactionManager().transfer(
                        senderAccount.getIBAN(),
                        currentUser.getId(),
                        description,
                        amount,
                        receiverIBAN);

                System.out.printf(" Transfer completed. New balance: %.2f€\n", senderAccount.getBalance());
            } catch (Exception e) {
                System.out.println(" Transfer failed. Reason: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(" Error during transfer: " + e.getMessage());
            input.nextLine(); // Clear input buffer
        }
    }    


    private static void handlePayment(BankAccount senderAccount) {
        try {
            System.out.print("Enter RF code: ");
            String rf = input.nextLine().trim();

            if (rf.isEmpty()) {
                System.out.println(" RF code cannot be empty.");
                return;
            }

            if (system.getBillManager().findActiveBillByRF(rf) == null) {
                System.out.println(" No active bill found with this RF code.");
                return;
            }

            System.out.print("Description: ");
            String description = input.nextLine();

            try {
                system.getTransactionManager().pay(
                        senderAccount.getIBAN(),
                        currentUser.getId(),
                        description,
                        rf);

                System.out.printf(" Payment completed. New balance: %.2f€\n", senderAccount.getBalance());
            } catch (Exception e) {
                System.out.println(" Payment failed. Reason: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(" Error during payment: " + e.getMessage());
            input.nextLine(); // Clear input buffer
        }
    }    

    // HANDLE BILLS

    private static void viewActiveBillsForCurrentUser() {
        String userId = currentUser.getId();

        try {
            List<Bill> activeBills = system.getBillManager().getActiveBillsForCustomer(userId);
            if (activeBills.isEmpty()) {
                System.out.println("You have no active unpaid bills.");
                return;
            }

            System.out.println("\n=== Active Bills You Owe ===");
            for (Bill bill : activeBills) {
                System.out.println("Bill ID: " + bill.getId());
                System.out.println("From Business ID: " + bill.getBusinessId());
                System.out.printf("Amount: %.2f€\n", bill.getAmount());
                System.out.println("RF: " + bill.getRF());
                System.out.println("Due Date: " + bill.getExpireTime());
                System.out.println("------");
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    private static void viewIssuedBillsByCompany() {
        String businessId = currentUser.getId();

        try {
            List<Bill> allBills = system.getBillManager().getActiveBillsForBusiness(businessId);

            if (allBills.isEmpty()) {
                System.out.println("You haven't issued any bills yet.");
                return;
            }

            System.out.println("\n=== All Bills You've Issued ===");
            for (Bill bill : allBills) {
                System.out.println("Bill ID: " + bill.getId());
                System.out.println("Customer ID: " + bill.getCustomerId());
                System.out.printf("Amount: %.2f€\n", bill.getAmount());
                System.out.println("RF: " + bill.getRF());
                System.out.println("Issued: " + bill.getTimePublished());
                System.out.println("Expires: " + bill.getExpireTime());
                System.out.println("Active: " + bill.isActive());
                System.out.println("Paid: " + bill.isPaid());
                System.out.println("------");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving bills: " + e.getMessage());
        }
    }

    private static void handleIssueBill(Company company) {
        try {
            System.out.print("Enter customer ID: ");
            input.nextLine();
            String customerId = input.nextLine().strip();

            System.out.print("Enter amount: ");
            double amount = input.nextDouble();
            input.nextLine();

            System.out.print("Enter days until expiration: ");
            int days = input.nextInt();
            input.nextLine();

            LocalDate expireTime = system.getTime().plusDays(days);
            System.out.println(customerId);
            system.getBillManager().issueBill(company.getId(), customerId, amount, expireTime, null);
            System.out.println("Bill issued successfully.");
        } catch (Exception e) {
            System.out.println("Error issuing bill: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startAdminMenu() {
        while (true) {
            
            try {
                System.out.println("\n=== Admin Menu ===");
                System.out.println("1. Show All Customers");
                System.out.println("2. Show Customer Details");
                System.out.println("3. Show All Bank Accounts");
                System.out.println("4. Show Bank Account Info");
                System.out.println("5. Show Bank Account Statements");
                System.out.println("6. Simulate Time Passing");
    
                System.out.println("0. Exit");
                System.out.print("Choose action: ");
                int choice = input.nextInt();
                input.nextLine(); // clear buffer

                switch (choice) {
                    case 1:
                        showAllCustomers();
                        break;
                    case 2:
                        showCostumerDetails();
                        break;
                    case 3:
                        showAllBankAccounts();
                        break;
                    case 4:
                        showBankAccountInfo();
                        break;
                    case 5:
                        showBankAccountStatements();
                        break;
                    case 6:
                        promptToSimulateTimePassing();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                input.nextLine(); // Clear invalid input
            }
        }
    }

    // Δείχνει λίστα όλων των πελατών με βασικά στοιχεία.
    private static void showAllCustomers() {
        System.out.println("\n=== All Customers ===");

        for (User user : system.getUserManager().getAllUsers()) {
            if (user instanceof Individual || user instanceof Company) {
                System.out.println("ID: " + user.getId()
                        + ", Name: " + user.getLegalName()
                        + ", Username: " + user.getUserName()
                        + ", Type: " + (user instanceof Individual ? "Individual" : "Company"));
            }
        }
    }

    // Δείχνει αναλυτικά στοιχεία για συγκεκριμένο πελάτη που επιλέγει ο admin
    private static void showCostumerDetails() {
        System.out.print("\nEnter Customer ID: ");
        String id = input.nextLine();

        User user = system.getUserManager().findUserById(id);

        if (user == null) {
            System.out.println("No customer found with that ID.");
            return;
        }

        if (!(user instanceof Individual || user instanceof Company)) {
            System.out.println("This user is not a customer.");
            return;
        }

        System.out.println("\n=== Customer Details ===");
        System.out.println("ID: " + user.getId());
        System.out.println("Name: " + user.getLegalName());
        System.out.println("Username: " + user.getUserName());
        System.out.println("Type: " + (user instanceof Individual ? "Individual" : "Company"));

        // casting για να καλεσω getVat() που ειναι ορισμενη στο customer
        Costumer customer = (Costumer) user;
        System.out.println("VAT: " + customer.getVAT());
    }

    private static void showAllBankAccounts() {
        System.out.println("\n=== All Bank Accounts ===");

        ArrayList<BankAccount> accounts = system.getAccountManager().getAllAccounts();

        for (BankAccount acc : accounts) {
            System.out.println("IBAN: " + acc.getIBAN()
                    + ", Owner ID: " + acc.getOwnerId()
                    + ", Balance: " + String.format("%.2f€", acc.getBalance()));
        }
    }

    // Ο admin δίνει ένα IBAN και βλέπει όλες τις πληροφορίες του λογαριασμού
    private static void showBankAccountInfo() {
        System.out.print("\nEnter IBAN: ");
        String iban = input.nextLine();

        BankAccount acc = system.getAccountManager().findAccountByIBAN(iban);

        if (acc == null) {
            System.out.println("No account found with that IBAN.");
            return;
        }

        System.out.println("\n=== Account Info ===");
        System.out.println("IBAN: " + acc.getIBAN());
        System.out.println("Owner ID: " + acc.getOwnerId());
        System.out.printf("Balance: %.2f€\n", acc.getBalance());
        System.out.println("Interest Rate: " + acc.getInterestRate() + "%");

        if (acc instanceof PersonalAccount) {
            System.out.println("Type: Personal");
            System.out.println("Secondary Owners: " + ((PersonalAccount) acc).getSecondaryOwnerIds());
        } else if (acc instanceof BusinessAccount) {
            System.out.println("Type: Business");
            System.out.println("Monthly Fee: " + ((BusinessAccount) acc).getMaintenanceFee() + "€");
        }
    }

    private static void showBankAccountStatements() {
        System.out.print("\nEnter IBAN: ");
        String iban = input.nextLine();

        BankAccount acc = system.getAccountManager().findAccountByIBAN(iban);

        if (acc == null) {
            System.out.println("No account found with that IBAN.");
            return;
        }

        List<AccountStatement> statements = system.getAccountStatementManager().getStatements(iban);

        if (statements.isEmpty()) {
            System.out.println("No statements found for this account.");
            return;
        }

        System.out.println("\n=== Account Statements ===");
        for (AccountStatement st : statements) {
            System.out.println(st); // Αν η toString() στην AccountStatement είναι σωστά ορισμένη
        }
    }

    static void promptToSimulateTimePassing() {

        // rwtaei:
        // years
        System.out.println();
        int years = input.nextInt();

        // months
        int months = input.nextInt();

        // days
        int days = input.nextInt();
        // kai kalei thn antistoixei synarthsh toy system
        LocalDate dateBefore = system.getTime();
        try {
            system.increaseTime(LocalDate.of(years, months, days));
            System.out.println("Date before: " + dateBefore);
            System.out.println("Date now: " + system.getTime());
        } catch(Exception e){
            System.out.println("Fail: " + e.getMessage());
        }
        
    }
}
