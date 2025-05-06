package frontend;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import models.users.Individual;
import models.users.Company;
import models.users.Admin;
import models.users.User;
import models.accounts.BankAccount;
import models.accounts.PersonalAccount;
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

        promptToAuthenticate(); // stoxos: user != null gia na paw sto state 4
        if (currentUser == null) {
            System.exit(0);
        }
        if (currentUser instanceof Individual) {
            // startIndividualMenu();
        } else if (currentUser instanceof Company) {

        } else {

        }

    }

    private static void startIndividualMenu() {
        System.out.println("Available accounts: (choose one)");

        // TODO: ektypwse me 1, 2, 3 .. ta available bank accounts gia ayton ton xrhsth
        // choose kai anathesh

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
        System.out.println("1. Register");
        System.out.println("2. Log in");
        System.out.println("3. Exit");
        System.out.print("Choice: ");

        try {
            int choice = input.nextInt();
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
                    System.exit(0);
                    // TODO: exit h retry
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: Please enter a number (1, 2, 3 or  4).");
            input.next(); // Clear the wrong input
        }
    }
    // Building on UserManager

    private static void handleLogin() {

        System.out.print("Username/Company name: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        User currentUser = system.getUserManager().login(username, password);

        if (currentUser == null) {
            System.out.println("Wrong username ή password");

            // TODO: exit h retry
        } else {
            System.out.println("loged in successfully " + currentUser.getLegalName());
        }
    }

    private static void handleRegistration() {
        System.out.println("\nUser type:");
        System.out.println("1. Individual");
        System.out.println("2. Company");
        System.out.println("3. Admin"); // TODO: handle admin
        System.out.print("Choice: ");

        int typeChoice = input.nextInt();
        input.nextLine(); // Clear buffer

        String type = (typeChoice == 1) ? "Individual" : "Company";

        System.out.print("Name and surname/ Company name: ");
        String legalName = input.nextLine();

        System.out.print("Username: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        System.out.print("Vat(9 digits): ");
        String vat = input.nextLine();

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

            System.out.print("Enter monthly maintenance fee: ");
            double fee = input.nextDouble();
            input.nextLine(); // Clear buffer

            system.getAccountManager().createBusinessAccount(company.getId(), countryCode, interestRate, fee);

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
