package models.accounts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class PersonalAccount extends BankAccount {
    private ArrayList<String> secondaryOwnerIds;

    public PersonalAccount(String IBAN, String ownerId, double interestRate,
            ArrayList<String> secondaryOwnerIds) {
        super(IBAN, ownerId, interestRate);
        this.secondaryOwnerIds = new ArrayList<>(secondaryOwnerIds);
    }

    public PersonalAccount(String data){
        super(data);
    }
    public ArrayList<String> getSecondaryOwnerIds() {
        return new ArrayList<>(secondaryOwnerIds); // giati new?
        // epeidh an epistrepseis thn idia thn lista, aftos poy kalese thn synarthsh
        // mporei na thn allaksei
        // me afton ton tropo, ftiaxneis ena antigrafo
    }

    @Override
    public String marshal() {
        String temp = String.format(Locale.US,
                "type:PersonalAccount,IBAN:%s,primaryOwner:%s,dateCreated:%s,rate:%.2f,balance:%.2f",
                IBAN, ownerId, dateCreated.toString(), interestRate, balance);

        for (String coownerId : secondaryOwnerIds) {
            temp += ",coOwner:" + coownerId;
        }
        return temp;
    }

    @Override
    public void unmarshal(String data) {
        String[] parts = data.split(",");

        String IBAN = parts[1].split(":")[1];

        String ownerId = parts[2].split(":")[1];

        LocalDate dateCreated = LocalDate.parse(parts[3].split(":")[1]);

        double interestRate = Double.parseDouble(parts[4].split(":")[1]);

        double balance = Double.parseDouble(parts[5].split(":")[1]);

        // Αρχικοποίηση λίστας co-owners πριν τη χρήση
        ArrayList<String> secondaryOwnerIds = new ArrayList<>();

        // apo to parts[6] kai meta, einai coowner
        for (int i = 6; i < parts.length; i++) {
            if (parts[i].startsWith("coOwner:")) {
                String coOwnerId = parts[i].split(":")[1];
                secondaryOwnerIds.add(coOwnerId);
            }
        }

        this.IBAN = IBAN;
        this.ownerId = ownerId;
        this.dateCreated = dateCreated;
        this.interestRate = interestRate;
        this.balance = balance;
        this.secondaryOwnerIds = secondaryOwnerIds;
    }
}