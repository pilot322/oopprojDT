package models.bills;

import java.time.LocalDate;

import models.Storable;

public class Bill implements Storable{
    private String id;
    private String businessId;
    private String customerId;
    private String RF;
    private double amount;
    private LocalDate timePublished;
    private LocalDate expireTime;
    private boolean active;
    private boolean isPaid;

    public Bill(String id, String businessId,String customerId, String RF,
            double amount, LocalDate timePublished,
            LocalDate expireTime) {
        this.id = id;
        this.businessId = businessId;
        this.customerId = customerId;
        this.RF = RF;
        this.amount = amount;
        this.timePublished = timePublished;
        this.expireTime = expireTime;
        this.active = true;
        this.isPaid = false;
    }

    public String getId() {
        return id;
    }

    public String getBusinessId() {
        return businessId;
    }


    public String getCustomerId() {
        return customerId;
    }

    public String getRF() {
        return RF;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getTimePublished() {
        return timePublished;
    }

    public LocalDate getExpireTime() {
        return expireTime;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void markAsPaid() {
        this.isPaid = true;
        this.active = false;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", businessId=" + businessId +
                ", customerId=" + customerId +
                ", RF='" + RF + '\'' +
                ", amount=" + amount +
                ", timePublished=" + timePublished +
                ", expireTime=" + expireTime +
                ", active=" + active +
                ", isPaid=" + isPaid +
                '}';
    }

    public String marshal() {
        return String.format(
                "type:Bill,paymentCode:%s,billNumber:%s,issuer:%s,customer:%s,amount:%.2f,issueDate:%s,dueDate:%s,active:%b,paid:%b",
                 RF, id, businessId, customerId, amount,
                timePublished.toString(), expireTime.toString(), active, isPaid);
    }

    public void unmarshal(String data) {
        String[] parts = data.split(",");

        
        String RF = parts[1].split(":")[1];
        String id = parts[2].split(":")[1];


        String businessId = parts[3].split(":")[1];

        String customerId = parts[4].split(":")[1];

        double amount = Double.parseDouble(parts[5].split(":")[1]);

        LocalDate timePublished = LocalDate.parse(parts[6].split(":")[1]);

        LocalDate expireTime = LocalDate.parse(parts[7].split(":")[1]);

        boolean active = Boolean.parseBoolean(parts[8].split(":")[1]);

        boolean isPaid = Boolean.parseBoolean(parts[9].split(":")[1]);

        this.id = id;
        this.RF = RF;
        this.businessId = businessId;
        this.customerId = customerId;
        this.amount = amount;
        this.timePublished = timePublished;
        this.expireTime = expireTime;
        this.active = active;
        this.isPaid = isPaid;

    }

}