package models.transactions;

import system.BankSystem;

public abstract class TwoWay extends Transaction {

    protected final String senderIBAN;
    protected final String senderDescription;
    protected final String receiverIBAN;
    protected final String receiverDescription;

    public TwoWay(String transactorId,
            String senderIBAN,
            String senderDescription,
            double amount,
            String receiverIBAN,
            String receiverDescription,
            BankSystem system) {

        super(transactorId, null, null, amount, system);

        this.senderIBAN = senderIBAN;
        this.senderDescription = senderDescription;
        this.receiverIBAN = receiverIBAN;
        this.receiverDescription = receiverDescription;
    }

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public String getSenderDescription() {
        return senderDescription;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public String getReceiverDescription() {
        return receiverDescription;
    }
}
