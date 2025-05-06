package models.users;

public class Admin extends User {

    public Admin(String id, String legalName, String userName, String password) {
        super("Admin", legalName, userName);
    }
}