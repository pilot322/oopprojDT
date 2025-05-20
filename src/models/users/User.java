package models.users;

import models.Storable;

public abstract class User implements Storable {
    protected String id;
    protected String userName;
    protected String password;
    private String type;
    protected String legalName;

    public User(String legalName, String id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
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