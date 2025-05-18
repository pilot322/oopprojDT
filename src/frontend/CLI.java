package frontend;

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
import models.statements.AccountStatement;
import models.accounts.BusinessAccount;
import managers.UserManager;
import managers.AccountManager;

import system.BankSystem;

public class CLI {

    public static final Scanner input = new Scanner(System.in);
    public static BankSystem system = new BankSystem();
    static User currentUser;
    static BankAccount currentBankAccount;

    public static void main(String[] args) {
        // test logic
        User admin1 = system.getUserManager().register("Admin", "test", "abcd", "Test1234", null);
       
        // User user1 = system.getUserManager().register("Individual", "test", "abcd", "Test1234", "123456789");
        // system.getAccountManager().createPersonalAccount(user1.getId(), "GR", 1, null);
        
        // User user2 = system.getUserManager().register("Company", "test2", "abcd", "Test1234", "987654321");
        // system.getAccountManager().createBusinessAccount(user2.getId(), "AL", 10);
        // // ontws to CLI ksekinaei edw
        promptToAuthenticate(); // stoxos: user != null gia na paw sto state 4
        if (currentUser == null) {
            System.exit(0);
        }
        if (currentUser instanceof Individual) {
            startIndividualMenu();
        } else if (currentUser instanceof Company) {
            companyOperationsMenu();
        } else if (currentUser instanceof Admin) {
            startAdminMenu();
        }

    }

    private static void startAdminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Show All Customers");
            System.out.println("2. Show Customer Details");
            System.out.println("3. Show All Bank Accounts");
            System.out.println("4. Show Bank Account Info");
            System.out.println("5. Show Bank Account Statements");
            ;
            System.out.println("0. Exit");
            System.out.print("Choose action: ");

            try {
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
                        System.out.println("Returning to main menu...");
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
            System.out.println("4. Account Details");
            System.out.println("5. Back to Accounts");
            System.out.print("Choose action: ");

            try {
                int action = input.nextInt();
                input.nextLine(); // Clear buffer

                switch (action) {
                    case 1:
                        System.out.printf("Current Balance: %.2f€\n", account.getBalance());
                        break;

                    case 2:
                        // deposit
                        break;

                    case 3:
                        // withdraw
                        break;

                    case 4:
                        System.out.println("IBAN: " + account.getIBAN());
                        System.out.println("Owner: " + account.getOwnerId());
                        System.out.printf("Balance: %.2f€\n", account.getBalance());
                        System.out.println("Interest Rate: " + account.getInterestRate() + "%");
                        System.out.println("Secondary Owners: " + account.getSecondaryOwnerIds());
                        break;

                    case 5:
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

    public static void companyOperationsMenu() {
        Company company = (Company) currentUser;
        currentBankAccount = system.getAccountManager().findAccountByBusinessId(company.getId());

        // an den yparxei?

        while (true) {
            System.out.println("\n=== Company Account ===");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. View Transactions");
            System.out.println("4. Account Details");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose action: ");

            int choice;

            do {
                choice = input.nextInt();

                switch (choice) {
                    case 1:
                        // deposit
                        break;

                    case 2:
                        // withraw
                        break;

                    case 3:
                        System.out.println("=== Transaction History ===");

                        break;

                    case 4:
                        System.out.println("=== Account Details ===");
                        System.out.println("IBAN: " + currentBankAccount.getIBAN());
                        System.out.println("Company ID: " + currentBankAccount.getOwnerId());
                        System.out.printf("Balance: %.2f€\n", currentBankAccount.getBalance());
                        System.out.println("Interest Rate: " + currentBankAccount.getInterestRate() + "%");
                        System.out.println(
                                "Monthly Fee: " + ((BusinessAccount) currentBankAccount).getMaintenanceFee() + "€");
                        break;

                    case 5:
                        return; // return to menu

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while ( !(choice == 1 || choice == 2 || choice == 3 || choice == 4 || choice == 5));
        }
    }

    // private static int getMainMenuChoice(User user) {
    // while (true) {

    // try {
    // System.out.println("\n----Main Menu----");
    // System.out.println("1. Show Balance");
    // System.out.println("2. Deposit");
    // System.out.println("3. Withrawl");
    // System.out.println("4 Find user by id");
    // System.out.println("5. Desconnect");

    // System.out.print("Choice: ");

    // return input.nextInt();
    // } catch (InputMismatchException e) {
    // System.out.println("Error: Please enter a number (1-4).");
    // input.nextLine();

    // }
    // }
    // }

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
                    System.exit(0);
                    break;
                default:
                    System.out.println("Non valid choice");
                    // System.exit(0);

            }
        } while (!(choice == 1 || choice == 2 || choice == 3));
    }

    // Building on UserManager

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
                    System.exit(0);
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

    private static void findUserById() {
        System.out.print("Enter user ID to search: ");
        String userId = input.nextLine();

        try {
            User foundUser = system.getUserManager().findUserById(userId);
            System.out.println("\nUser found:");
            System.out.println("Name: " + foundUser.getLegalName());
            System.out.println("Type: " + system.getUserManager().getUserType(userId));
        } catch (IllegalArgumentException e) {// if there is no matching id
            System.out.println("Error: " + e.getMessage());
        }
    }
    // Building on AccountManager

    private static void handleAccountCreation(User user) {
        System.out.println("\n--- Create New Account ---");

        if (user instanceof Individual) {
            createPersonalAccount((Individual) user);
        } else if (user instanceof Company) {
            createBusinessAccount((Company) user);
        } else {
            System.out.println("Unknown user type");
        }
    }

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

    private static void findAccountByIBAN() {
        System.out.print("Enter IBAN to search: ");
        String iban = input.nextLine();

        BankAccount account = system.getAccountManager().findAccountByIBAN(iban);
        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        System.out.println("\nAccount found:");
        System.out.println("IBAN: " + account.getIBAN());
        System.out.println("Owner ID: " + account.getOwnerId());
        System.out.println("Balance: " + account.getBalance());

        if (account instanceof PersonalAccount) {
            System.out.println("Type: Personal");
            System.out.println("Interest Rate: " + ((PersonalAccount) account).getInterestRate() + "%");
        } else {
            System.out.println("Type: Business");
            System.out.println("Maintenance Fee: " + ((BusinessAccount) account).getMaintenanceFee());
        }

    }
}
