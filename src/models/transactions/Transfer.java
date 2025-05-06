package models.transactions;

import system.BankSystem;

public class Transfer extends TwoWay {
        public Transfer(String transactorId, String senderIBAN,
                        String senderDescription, double amount,
                        String receiverIBAN, String receiverDescription, BankSystem system) {
                super(transactorId, senderIBAN, senderDescription, amount,
                                receiverIBAN, receiverDescription, system);
        }

        @Override
        public boolean execute() {
                // AccountStatement senderStatement = new AccountStatement(
                // accountIBAN,
                // timestamp,
                // transactorId,
                // "TRANSFER_OUT",
                // description + " -> " + receiverDescription,
                // -amount,
                // -amount);

                // AccountStatement receiverStatement = new AccountStatement(
                // receiverIBAN,
                // timestamp,
                // transactorId,
                // "TRANSFER_IN",
                // receiverDescription + " <- " + description,
                // amount,
                // amount);

                // statementManager.addStatement(senderStatement);
                // statementManager.addStatement(receiverStatement);
                executed = true;
                return true;
        }
}