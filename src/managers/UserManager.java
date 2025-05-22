package managers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Storable;
import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import system.BankSystem;

public class UserManager extends Manager implements StorageManager {
    private final Map<String, User> usersMap;
    private String usersFilePath = "data/users/users.csv";
    private int nextId = 0; // ayto aplws krataei to posa users esxoyn dhmioyrghthei

    public UserManager(BankSystem systemref) {
        super(systemref);
        usersMap = new HashMap<>();

        load(usersFilePath);
    }

    // User login
    public User login(String username, String password) {
        // User user = usersByUsername.get(username.toLowerCase());
        // if (user == null || !user.getPassword().equals(password)) {
        // System.out.println(user);
        // return null;
        // }
        // return user;

        for (User user : usersMap.values()) {
            if (user.getUserName().toLowerCase().equals(username.toLowerCase())
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // check for already used username
    private boolean isUsernameTaken(String username) {
        for (User user : usersMap.values()) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;

    }

    private String generateUserId() {

        String userId = "1000000" + nextId;
        nextId++;

        return userId;
    }

    // C
    public User register(String type, String username, String password, String legalName, String vat)
            throws IllegalArgumentException {

        // 1.check for empty
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        // 2. check for taken username
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("The username '" + username + "' already exists");
        }

        // 3. Create user with type
        User newUser;
        if (type.equalsIgnoreCase("Admin")) {
            if (vat != null) {
                throw new IllegalArgumentException("Admin cannot have VAT");
            }
            newUser = new Admin(legalName, generateUserId(), username, password);
        } else if (type.equalsIgnoreCase("Individual")) {
            if (vat == null || vat.length() != 9 || !isDigit(vat)) { // VAT check
                throw new IllegalArgumentException("VAT must have 9 digits");
            }
            newUser = new Individual(legalName, generateUserId(), username, password, vat);
        } else if (type.equalsIgnoreCase("Company")) {
            if (vat == null || vat.length() != 9 || !isDigit(vat)) {
                throw new IllegalArgumentException("VAT must have 9 digits");
            }
            newUser = new Company(legalName, generateUserId(), username, password, vat);
        } else {
            throw new IllegalArgumentException("Invalid user type: " + type);
        }

        // 4. Store the user
        usersMap.put(newUser.getId(), newUser);
        System.out.printf("Register successful, %s\n", newUser.getUserName());
        // save(newUser, usersFilePath, true);

        return newUser;
    }

    private boolean isDigit(String vat) {
        if (vat == null || vat.isEmpty()) {
            return false;
        }
        for (int i = 0; i < vat.length(); i++) {
            if (!Character.isDigit(vat.charAt(i))) {
                return false; // if no digit is found return false
            }
        }
        return true;
    }

    public User findUserById(String userId) {
        return usersMap.get(userId);
    }

    public String getUserType(String userId) {
        User user = usersMap.get(userId);
        if (user == null)
            throw new NullPointerException();

        if (user instanceof Admin)
            return "Admin";
        if (user instanceof Individual)
            return "Individual";
        if (user instanceof Company)
            return "Company";
        throw new IllegalStateException("Unknown user type");
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersMap.values());
    }

    @Override
    public void load(String filePath) {
        Path path = Path.of(filePath);
        List<String> lines = List.of();

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

            User user = null;

            switch (type) {
                case "Admin":
                    user = new Admin(line);
                    break;
                case "Individual":
                    user = new Individual(line);
                    break;
                case "Company":
                    user = new Company(line);
                    break;
                default:
                    System.out.println("Unknown user type: " + type);
                    continue;
            }

            usersMap.put(user.getId(), user);

            // update nextId to avoid ID reuse
            try {
                int numericId = Integer.parseInt(user.getId().replace("1000000", ""));
                if (numericId >= nextId) {
                    nextId = numericId + 1;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format for user: " + user.getId());
            }
            System.out.println(user.marshal());
        }
    }

    @Override
    public void save(Storable s, String filePath, boolean append) {
        // kseroyme oti to append tha einai panta true
        // kseroyme oti to s einai User
        if (!(s instanceof User)) {
            return;
        }

        Path p = Path.of(filePath);

        // List<String> lines = null;
        // // 1. prwta prepei na diavaseis ola ta periexomena toy arxeioy (mia lista me
        // // oles tis grammes)
        // try {
        //     lines = Files.readAllLines(p);
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return;
        // }

        // 2. meta prepei na prostheseis mia grammh sto telos me to kainoyrio user
        // lines.add(s.marshal());
        String newline = s.marshal();
        // 3. kai na grapseis oles tis grammes sto arxeio (svhnwntas tis palies)
        try {
            Files.write(p, List.of(newline), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public void saveData(){
        // tha prepei na adeiazoyme to arxeio prin kanoyme append
        try{

            Path p = Path.of(usersFilePath);
            Files.write(p, List.of());

            // gia kathe antikeimeno sto usersmap tha kaloyme thn save
            for(User u : usersMap.values()){
                save(u,usersFilePath, true);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}