package models.users;

public class Costumer extends User {

    String VAT;

    public Costumer(String legalName, String type, String id, String userName, String password, String VAT) {
        super(legalName, id, userName, password, type);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }

}
