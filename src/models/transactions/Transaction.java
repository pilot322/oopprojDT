package models.transactions;

import java.time.LocalDateTime;

import system.BankSystem;


public abstract class Transaction {
    protected final String transactorId;
    protected final String accountIBAN;
    protected final String description;
    protected final double amount;
    protected final LocalDateTime timestamp;
    protected boolean executed;
    protected BankSystem systemRef;

    public Transaction(String transactorId, String accountIBAN, String description, double amount, BankSystem system) {
        this.transactorId = transactorId;
        this.accountIBAN = accountIBAN;
        this.description = description;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.executed = false;
        this.systemRef = system;
    }

    public abstract boolean execute();

    public String getTransactorId() {
        return transactorId;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isExecuted() {
        return executed;
    }
}