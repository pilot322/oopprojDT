package models.users;

public abstract class User {
    private String id;
    private String userName;
    private String password;
    private String type;
    private String legalName;

   
    public User(String legalName ,String id,String type, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.type = type;
        this.legalName = legalName;

    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }


    public String getUserName() {
        return userName;
    }


    public String getPassword() {
        return password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLegalName() {
        return legalName;
    }


}
