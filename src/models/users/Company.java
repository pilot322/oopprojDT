package models.users;

public class Company extends Costumer {

    public Company(String id, String legalName, String userName, String password, String vatNumber) {
        super(legalName, id, userName, password, vatNumber);
    }
}
