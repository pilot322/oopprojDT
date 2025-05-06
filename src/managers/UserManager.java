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
        for (User user : usersMap.values()) {
            if (user.getUserName().equals(username) && user.getPassword().equals(password)) {
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
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("The username '" + username + "already exists");
        }

        // 3. Create user with type
        User newUser;
        switch (type.toLowerCase()) {
            case "admin":
                newUser = new Admin(generateUserId(), legalName, username, password);
                break;
            case "individual":
                if (vat == null || vat.length() != 9 || !isDigit(vat)) { // VAT check
                    throw new IllegalArgumentException("VAT must have 9 digits");
                }
                newUser = new Individual(generateUserId(), legalName, username, password, vat);
                break;
            case "company":
                if (vat == null || vat.length() != 9 || !isDigit(vat)) {
                    throw new IllegalArgumentException("VAT must have 9 digits");
                }
                newUser = new Company(generateUserId(), legalName, username, password, vat);
                break;
            default:
                throw new IllegalArgumentException("Non valid user type: " + type);
        }

        // 4. Store the user
        usersMap.put(newUser.getId(), newUser);
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
        // 1.Search the user where we have store them(HashMap)
        User user = usersMap.get(userId);

        // 2. if user did not found throw exception
        if (user == null) {
            throw new IllegalArgumentException("User did not found by id " + userId);
        }
        return user;
    }

    public String getUserType(String userId) {
        // 1. find the user
        User user = usersMap.get(userId);

        // 2.if we cant return "Unknown"
        if (user == null) {
            return "Unknown";
        }

        // 3.return the class of the user
        return user.getClass().getSimpleName();
    }
}
