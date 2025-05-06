package models.transactions;

import system.BankSystem;

public class Payment extends TwoWay {
    private final String RF;

    public Payment(String transactorId, String senderIBAN,
            String senderDescription,
            String RF, BankSystem systemRef) {

        super(transactorId, senderIBAN, senderDescription, -1,
                "",
                "", systemRef);
        this.RF = RF;

        // me vash to RF tha prepei na vreis to amount, to reiverIBAN kai to
        // receiverDescription
    }

    @Override
    public boolean execute() {
        throw new RuntimeException("TODO");
    }

    public String getRF() {
        return RF;
    }
}