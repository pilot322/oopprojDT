package models.bills;

import java.time.LocalDateTime;

public class Bill {
    private final int id;
    private final int businessId;
    private final int customerId;
    private final String RF;
    private final double amount;
    private final LocalDateTime timePublished;
    private final LocalDateTime expireTime;
    private boolean active;
    private boolean isPaid;

    public Bill(int id, int businessId, int customerId, String RF,
            double amount, LocalDateTime timePublished,
            LocalDateTime expireTime) {
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

    public int getId() {
        return id;
    }

    public int getBusinessId() {
        return businessId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getRF() {
        return RF;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimePublished() {
        return timePublished;
    }

    public LocalDateTime getExpireTime() {
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
}