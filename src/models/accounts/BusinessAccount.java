package models.accounts;

public class BusinessAccount extends BankAccount {
    private double maintenanceFee;

    public BusinessAccount(String IBAN, String ownerId, double interestRate, double maintenanceFee) {
        super(IBAN, ownerId, interestRate);
        this.maintenanceFee = maintenanceFee;
    }

    public double getMaintenanceFee() {
        return maintenanceFee;
    }

}
