package models.users;

public abstract class Costumer extends User {

    String VAT;

    public Costumer(String legalName, String id, String userName, String password, String VAT) {
        super(legalName, id, userName, password);
        this.VAT = VAT;
    }

    public Costumer(String data){
        super(data);
    }

    public String getVAT() {
        return VAT;
    }
}