package managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Storable;
import models.statements.AccountStatement;
import system.BankSystem;

public class AccountStatementManager extends Manager implements StorageManager{
    private HashMap<String, List<AccountStatement>> statements;

    public AccountStatementManager(BankSystem system) {
        super(system);
        this.statements = new HashMap<>();

        loadData();
    }

    private int statementCounter = 0;

    public boolean addStatement(String accountIBAN, String transactorId,
            String description, double amount,
            double balanceAfter, String type,
            String receiverIBAN) {

        // Έλεγχος τύπου συναλλαγής
        List<String> types = List.of("deposit", "withdraw", "transfer_in", "transfer_out", "fee", "interest", "payment_in", "payment_out");
        if (!types.contains(type.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        if (systemRef.getAccountManager().findAccountByIBAN(accountIBAN) == null) {
            throw new IllegalArgumentException("Invalid account IBAN: " + accountIBAN);
        }

        // Έλεγχος αν υπάρχει ο transactor (user)
        if (systemRef.getUserManager().findUserById(transactorId) == null && !transactorId.equals("BANK")) {
            throw new IllegalArgumentException("Invalid transactor ID: " + transactorId);
        }

        // Έλεγχος ποσού
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Αν είναι deposit ή interest ή transfer και το ποσό είναι αρνητικό -> λάθος
        if (!type.equalsIgnoreCase("withdraw") && amount < 0) {
            throw new IllegalArgumentException("Negative amount is not allowed for non-withdraw transactions");
        }

        if (type.equalsIgnoreCase("transfer_out")) {
            if (receiverIBAN == null || receiverIBAN.isEmpty()) {
                throw new IllegalArgumentException("Receiver IBAN cannot be null for transfer_out transactions");
            }

            // Ελέγχει αν το receiverIBAN υπάρχει στο σύστημα
            if (systemRef.getAccountManager().findAccountByIBAN(receiverIBAN) == null) {
                throw new IllegalArgumentException("Receiver IBAN does not exist: " + receiverIBAN);
            }
        }

        AccountStatement newStatement = new AccountStatement(
                ++statementCounter,
                accountIBAN,
                BankSystem.getSystemRef().getTime(),
                transactorId,
                description,
                type,
                amount,
                balanceAfter,
                receiverIBAN);

        // Αν δεν υπάρχει λίστα για αυτό το IBAN, δημιουργούμε νέα
        if (!statements.containsKey(accountIBAN)) {
            statements.put(accountIBAN, new ArrayList<>());
        }

        // Προσθέτουμε το statement στη λίστα
        List<AccountStatement> accountStatements = statements.get(accountIBAN);

        // tha prepei to statement na mpainei sthn katallhlh thesh
        // pws tha vrw thn katallhlh thesh?
        for (int i = 0; i < accountStatements.size(); i++) {
            AccountStatement st = accountStatements.get(i);
            // thelw na valw to newAccountStatement sthn thesh PRIN apo to prwto
            // statement poy tha vrei kai einai "mikrotero", dld einai "prin" ap'to
            // kainoyrio
            if (newStatement.getTransactionTime().isAfter(st.getTransactionTime())) {
                accountStatements.add(i, newStatement);
                return true;
            }
        }

        accountStatements.add(newStatement);
        return true;
    }

    // Λήψη όλων των statements για IBAN
    public List<AccountStatement> getStatements(String accountIBAN) {
        // Επιστρέφουμε μια νέα λίστα αντιγράφοντας τα στοιχεία της υπάρχουσας λίστας
        if (statements.containsKey(accountIBAN)) {
            return new ArrayList<>(statements.get(accountIBAN));
        }
        return new ArrayList<>();
    }

    public void loadData(){
        File statementsDir = new File("data/statements");

        if (statementsDir.exists() && statementsDir.isDirectory()) {

            File[] files = statementsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        load(file.getAbsolutePath());
                    }
                }
            }
        } else {
            System.err.println("Directory 'data/statements' does not exist or is not a directory.");
        }
    }

    @Override
    public void load(String filePath) {
        // Extract the IBAN from the filename
        File file = new File(filePath);
        String fileName = file.getName(); // e.g., "IBANXXX...XX.csv"

        // Assuming the format is always "IBAN" followed by the actual IBAN and then ".csv"
        // if (fileName.startsWith("IBAN") && fileName.endsWith(".csv")) {
        String iban = fileName.substring(4, fileName.length() - 4); // Remove "IBAN" and ".csv"
        statements.put(iban, new ArrayList<>());
        // System.out.println("Extracted IBAN: " + iban);
        // } else {
        //     System.out.println("Filename does not match expected IBAN format: " + fileName);
        //     return; // Exit if the format is not as expected
        // }

        // Read all lines of the filepath and print them
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            System.out.println("Content of " + fileName + ":");
            for (String line : lines) {
                statements.get(iban).add(new AccountStatement(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
        }
    }

   @Override
    public void save(Storable s, String filePath, boolean append) {
        AccountStatement statement = (AccountStatement) s;
        // String iban = statement.getAccountIBAN(); // Get the IBAN from the statement
        String statementsDirectory = "data/statements";

        try {
            Files.createDirectories(Paths.get(statementsDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + statementsDirectory + ". Error: " + e.getMessage());
            return;
        }

        // Construct the file path for this specific IBAN's statements
        // The filePath argument passed to this method is currently not used to form the file name
        // because we're organizing files by IBAN. We'll use the statementsDirectory and IBAN.

        Path p = Paths.get(filePath);

        try {
            // StandardOpenOption.APPEND ensures data is added to the end.
            // StandardOpenOption.CREATE ensures the file is created if it doesn't exist.
            Files.write(p, (statement.marshal() + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error saving statement to " + filePath + ": " + e.getMessage());
        }
    }

    public void saveData(){
        File statementsDir = new File("data/statements");

        // Delete all existing files in the directory
        if (statementsDir.exists() && statementsDir.isDirectory()) {
            File[] files = statementsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            Files.delete(file.toPath());
                            // System.out.println("Deleted old statement file: " + file.getName()); // Uncomment for debugging
                        } catch (IOException e) {
                            System.err.println("Error deleting file " + file.getAbsolutePath() + ": " + e.getMessage());
                        }
                    }
                }
            }
        } else {
            // If the directory doesn't exist, create it to prevent issues during saving
            try {
                Files.createDirectories(statementsDir.toPath());
            } catch (IOException e) {
                System.err.println("Error creating directory " + statementsDir.getAbsolutePath() + ": " + e.getMessage());
                return; // Exit if directory cannot be created
            }
        }

        // Iterate through all stored statements and save them
        for (String IBAN : statements.keySet()) {
            List<AccountStatement> accountStatements = statements.get(IBAN);


            String filepath = "data/statements/IBAN" + IBAN + ".csv";
            try {
                    
                Files.write(Path.of(filepath), List.of());

            } catch(Exception e){
                e.printStackTrace();
            }
            for (AccountStatement statement : accountStatements) {
                save(statement, filepath, true);
               
                
            }
        }
    }


}