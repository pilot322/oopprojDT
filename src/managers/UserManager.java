package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import system.BankSystem;

public class UserManager extends Manager {
    private final Map<String, User> usersMap;

    private int nextId = 0; // ayto aplws krataei to posa users esxoyn dhmioyrghthei

    public UserManager(BankSystem systemref) {
        super(systemref);
        usersMap = new HashMap<>();
    }

    // User login
    public User login(String username, String password) {
        // User user = usersByUsername.get(username.toLowerCase());
        // if (user == null || !user.getPassword().equals(password)) {
        //     System.out.println(user);
        //     return null;
        // }
        // return user;

        for (User user : usersMap.values()) {
            if (user.getUserName().toLowerCase().equals(username.toLowerCase()) && user.getPassword().equals(password)) {
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

    public User register(String type, String username, String password, String legalName, String vat)
            throws IllegalArgumentException {

        // 1.check for empty
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        // 2. check for taken username
        // TODO
        
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
}
