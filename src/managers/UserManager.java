package managers;

import java.util.HashMap;
import java.util.Map;

import models.users.User;
import system.BankSystem;

public class UserManager  extends Manager{
    public UserManager(BankSystem systemRef) {
        super(systemRef);
    }

    private final Map<String, User> usersMap = new HashMap<>();

    private int nextId = 0; // ayto aplws krataei to posa users esxoyn dhmioyrghthei

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
        // to user id tha einai ena string poy tha einai mono pshfia kai tha exei mhkos
        // 8 xarakthres

        String temp = "";

        int idInt = 10000000;
        idInt += nextId;
        nextId++;

        temp += idInt;

        return temp;
    }

    // TODO
    public User register(String type, String username, String password, String legalName, String vat)
            throws IllegalArgumentException {
        throw new RuntimeException("TODO");
    }

    private boolean isDigit(String vat) {
        try {
            int valid = Integer.parseInt(vat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // TODO
    public User findUserById(String userId) {
        throw new RuntimeException("TODO");
    }

    // TODO
    public String getUserType(String userId) {
        throw new RuntimeException("TODO");
    }
}
