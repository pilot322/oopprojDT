package models.users;

public class Admin extends User {

    public Admin(String legalName, String id, String userName, String password) {
        super(legalName, id,  userName, password);
    }

    @Override
    public String marshal() {
        String temp = String.format("type:Admin,id:%s,legalName:%s,userName:%s,password:%s", id, legalName, userName, password);
        return temp;
    }

    // dedomena: exw ena antikeimeno typoy Admin
    // to opoio einai praktika keno: den exei tipota mesa poy na me endiaferei

    @Override
    public void unmarshal(String data) {
        // estw data: "type:Admin,legalName:Bank Administrator,userName:admin,password:123456"

        // type:Admin legalName:Bank Administrator userName:admin password:123456
        String parts[] = data.split(",");
        // parts = ["type:Admin", "legalName:Bank Administrator", ..]

        // 2 epiloges gia to pws tha diavaseis to kathe pedio:
        // 1h (pio aplh): me vash thn seira
        // ksereis oti to parts[0] einai to type

        // String type = parts[0].split(":")[1];
        
        String id = parts[1].split(":")[1];

        String legalName = parts[2].split(":")[1];

        String userName = parts[3].split(":")[1];

        String password = parts[4].split(":")[1];


        // double amount = Double.parseDouble("0.01");
        this.id = id;
        this.legalName = legalName;
        this.userName = userName;
        this.password = password;
    }
}