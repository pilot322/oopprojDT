package models.users;

public class Costumer extends User {

    String VAT;

    public Costumer(String legalName, String id, String userName, String password, String VAT) {
        super(legalName, id, userName, password);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }

    @Override
    public String marshal() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'marshal'");
    }

    @Override
    public void unmarshal(String data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unmarshal'");
    }

}
