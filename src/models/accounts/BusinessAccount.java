package models.accounts;

import java.time.LocalDate;

public class BusinessAccount extends BankAccount {
    private double maintenanceFee;

    public BusinessAccount(String IBAN, String ownerId, double interestRate, double maintenanceFee) {
        super(IBAN, ownerId, interestRate);
        this.maintenanceFee = maintenanceFee;
    }

    public double getMaintenanceFee() {
        return maintenanceFee;
    }

    @Override
    public String marshal() {
        String temp = String.format(
                "type:BusinessAccount,IBAN:%s,primaryOwner:%s,dateCreated:%s,rate:%.2f,balance:%.2f",
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

        this.IBAN = IBAN;
        this.ownerId = ownerId;
        // date
        this.interestRate = interestRate;
        this.balance = balance;
    }

}