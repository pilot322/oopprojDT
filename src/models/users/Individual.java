package models.users;

public class Individual extends Costumer {

    public Individual(String id, String legalName, String userName, String password, String vatNumber) {
        super("Individual", legalName, userName, password);
    }
}
