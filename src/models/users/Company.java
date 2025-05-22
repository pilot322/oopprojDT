package models.users;

public class Company extends Costumer {

    public Company(String legalName, String id, String userName, String password, String vatNumber) {
        super(legalName, id, userName, password, vatNumber);
    }

    public Company(String userMarshallData) {
        super(userMarshallData);
    }

    @Override
    public String marshal() {
        String temp = String.format("type:Company,id:%s,legalName:%s,userName:%s,password:%s,vatNumber:%s", id,
                legalName, userName, password, VAT);
        return temp;
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