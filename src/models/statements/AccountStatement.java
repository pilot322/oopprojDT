package models.statements;

import java.time.LocalDateTime;

public class AccountStatement {
    private final int id;
    private final String accountIBAN;
    private final LocalDateTime transactionTime;
    private final String transactorId;
    private final String description;
    private final String transactionType;
    private final double amount;
    private final double balanceAfterTransaction;
    private final String receiverIBAN;

    public AccountStatement(int id, String accountIBAN, LocalDateTime transactionTime,
            String transactorId, String description, String transactionType,
            double amount, double balanceAfterTransaction,
            String receiverIBAN) {
        this.id = id;
        this.accountIBAN = accountIBAN;
        this.transactionTime = transactionTime;
        this.transactorId = transactorId;
        this.description = description;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.receiverIBAN = receiverIBAN;
    }

    public int getId() {
        return id;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public String getTransactorId() {
        return transactorId;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    @Override
    public String toString() {
        return String.format(
                "AccountStatement[id=%d, account=%s, time=%s, type=%s, amount=%.2f, balance=%.2f]",
                id, accountIBAN, transactionTime, transactionType, amount, balanceAfterTransaction);
    }
}