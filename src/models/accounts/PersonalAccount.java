package models.accounts;

import java.util.ArrayList;

public class PersonalAccount extends BankAccount {
    private ArrayList<String> secondaryOwnerIds;

    public PersonalAccount(String IBAN, String ownerId, double interestRate,
            ArrayList<String> secondaryOwnerIds) {
        super(IBAN, ownerId, interestRate);
        this.secondaryOwnerIds = new ArrayList<>(secondaryOwnerIds);
    }

    public ArrayList<String> getSecondaryOwnerIds() {
        return new ArrayList<>(secondaryOwnerIds); // giati new?
        // epeidh an epistrepseis thn idia thn lista, aftos poy kalese thn synarthsh
        // mporei na thn allaksei
        // me afton ton tropo, ftiaxneis ena antigrafo
    }

    @Override
    public String marshal() {
        String temp = String.format(
                "type:PersonalAccount,IBAN:%s,primaryOwner:%s,dateCreated:%s,rate:%.2f,balance:%.2f,coOwner:%s",
                IBAN, ownerId, interestRate, balance);
        return temp;
    }

    @Override
    public void unmarshal(String data) {
        String[] parts = data.split(",");

        String IBAN = parts[1].split(":")[1];

        String ownerId = parts[2].split(":")[1];

        // String dateStr = parts[3].split(":")[1];

        double interestRate = Double.parseDouble(parts[4].split(":")[1]);

        double balance = Double.parseDouble(parts[5].split(":")[1]);

        // String coOwnerId = parts[6].split(":")[1];

        this.IBAN = IBAN;
        this.ownerId = ownerId;
        // date
        this.interestRate = interestRate;
        this.balance = balance;
        // coOwner
    }
}