package models.transactions;

import system.BankSystem;

public abstract class TwoWay extends Transaction {

    protected final String receiverIBAN;
    protected final String receiverDescription;

    public TwoWay(String transactorId, String senderIBAN, String senderDescription, double amount, String receiverIBAN,
            String receiverDescription, BankSystem system) {
        super(transactorId, senderIBAN, senderDescription, amount, system);
        this.receiverIBAN = receiverIBAN;
        this.receiverDescription = receiverDescription;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public String getReceiverDescription() {
        return receiverDescription;
    }

}