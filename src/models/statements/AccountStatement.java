package models.statements;

import java.time.LocalDate;

import models.Storable;

public class AccountStatement implements Storable {
    private int id;
    private String accountIBAN;
    private LocalDate transactionTime;
    private String transactorId;
    private String description;
    private String transactionType;
    private double amount;
    private double balanceAfterTransaction;
    private String receiverIBAN;

    public AccountStatement(int id, String accountIBAN, LocalDate transactionTime,
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

    public AccountStatement(String data){
        unmarshal(data);
    }

    public int getId() {
        return id;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public LocalDate getTransactionTime() {
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

    @Override
    public String marshal() {
        String temp = String.format(
                "id:%d,accountIBAN:%s,transactionTime:%s,transactorId:%s,description:%s,transactionType:%s,amount:%f,balanceAfterTransaction:%f",
                id, accountIBAN, transactionTime.toString(), transactorId, description, transactionType, amount,
                balanceAfterTransaction, receiverIBAN);
        if (receiverIBAN != null) {
            temp = temp + ",receiverIBAN:" + receiverIBAN;
        }
        return temp;
    }

    @Override
    public void unmarshal(String data) {
        String[] parts = data.split(",");

        this.id = Integer.parseInt(parts[0].split(":")[1]);
        this.accountIBAN = parts[1].split(":")[1];
        this.transactionTime = LocalDate.parse(parts[2].split(":")[1]);
        this.transactorId = parts[3].split(":")[1];
        this.description = parts[4].split(":")[1];
        this.transactionType = parts[5].split(":")[1];
        this.amount = Double.parseDouble(parts[6].split(":")[1]);
        this.balanceAfterTransaction = Double.parseDouble(parts[7].split(":")[1]);

        if(transactionType.startsWith("payment") || transactionType.startsWith("transfer")){
            this.receiverIBAN = parts[8].split(":")[1];
        }


    }
}