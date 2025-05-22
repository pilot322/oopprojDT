package models.accounts;

import java.time.LocalDate;

import models.Storable;
import system.BankSystem;

public abstract class BankAccount implements Storable {
    protected String IBAN;
    protected String ownerId;
    protected double balance;
    protected double interestRate;
    protected LocalDate dateCreated;

    public BankAccount(String IBAN, String ownerId, double interestRate) {
        this.IBAN = IBAN;
        this.ownerId = ownerId;
        this.interestRate = interestRate;
        this.balance = 0.0;
        this.dateCreated = BankSystem.getSystemRef().getTime();
    }

    public BankAccount(String data){
        unmarshal(data);
    }

    public String getIBAN() {
        return IBAN;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public double getBalance() {
        return balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public boolean addToBalance(double amount) {
        if (amount < 0)
            return false;

        this.balance += amount;
        return true;
    }

    public boolean removeFromBalance(double amount) {
        if (amount < 0 || this.balance - amount < 0)
            return false;
        this.balance -= amount;
        return true;
    }

}