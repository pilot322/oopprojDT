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
        return new ArrayList<>(secondaryOwnerIds);  // giati new?
        // epeidh an epistrepseis thn idia thn lista, aftos poy kalese thn synarthsh mporei na thn allaksei
        // me afton ton tropo, ftiaxneis ena antigrafo
    }
}
