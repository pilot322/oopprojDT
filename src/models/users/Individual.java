package models.users;

public class Individual extends Costumer {

    public Individual(String legalName, String id, String userName, String password, String vatNumber) {
        super(legalName, id, userName, password, vatNumber);
    }

    @Override
    public String marshal() {
        return String.format("type:Individual,id:%s,legalName:%s,userName:%s,password:%s,vatNumber:%s",
                id, legalName, userName, password, VAT);
    }

    @Override
    public void unmarshal(String data) {
        String[] parts = data.split(",");

        String id = parts[1].split(":")[1];

        String legalName = parts[2].split(":")[1];

        String userName = parts[3].split(":")[1];

        String password = parts[4].split(":")[1];

        String vatNumber = parts[5].split(":")[1];

        this.id = id;
        this.legalName = legalName;
        this.userName = userName;
        this.password = password;
        this.VAT = vatNumber;
    }

}