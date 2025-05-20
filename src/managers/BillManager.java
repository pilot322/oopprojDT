package managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.bills.Bill;
import models.users.Company;
import models.users.User;
import system.BankSystem;

public class BillManager extends Manager {
    private List<Bill> bills = new ArrayList<>();

    public BillManager(BankSystem system) {
        super(system);
    }

    private String generateRF() {
        return "RF" + System.currentTimeMillis();
    }

    public void issueBill(String businessId, String customerId, double amount, LocalDateTime expireTime, String oldRF)
            throws Exception {

        User business = systemRef.getUserManager().findUserById(businessId);
        if (business == null || !(business instanceof Company)) {
            throw new IllegalArgumentException("Business ID is invalid or not a Company.");
        }

        User customer = systemRef.getUserManager().findUserById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer ID is invalid.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Bill amount must be greater than zero.");
        }

        LocalDateTime now = systemRef.getTime();
        if (!expireTime.isAfter(now)) {
            throw new IllegalArgumentException("Expire time must be in the future.");
        }

        // --- HANDLE OLD RF LOGIC ---
        String finalRF;
        double totalAmount = amount;

        if (oldRF != null && !oldRF.isBlank()) {
            Bill oldBill = this.getActiveBillByRf(oldRF);
            if (oldBill != null) {
                this.deactivateBillsWithRF(oldRF);
                totalAmount += oldBill.getAmount();
                finalRF = oldRF;
            } else {
                finalRF = oldRF; // <== ΕΔΩ η κρίσιμη προσθήκη
            }
        } else {
            finalRF = generateRF();
        }

        String billId = UUID.randomUUID().toString();
        Bill newBill = new Bill(billId, businessId, customerId, finalRF, totalAmount, now, expireTime);

        this.bills.add(newBill);
    }

    public List<Bill> getBillsByRF(String RF) {
        List<Bill> result = new ArrayList<>();
        if (RF == null)
            return result; // Handle null RF if necessary
        for (Bill b : bills) {
            if (RF.equals(b.getRF())) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForCustomer(String customerId) {
        List<Bill> result = new ArrayList<>();
        if (customerId == null)
            throw new IllegalArgumentException("Invalid costumer id");

        if (systemRef.getUserManager().findUserById(customerId) == null) {
            throw new IllegalArgumentException("Customer does not exist.");
        }
        for (Bill b : bills) {
            if (customerId.equals(b.getCustomerId()) && b.isActive() && !b.isPaid()
                    && b.getExpireTime().isAfter(systemRef.getTime())) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForBusiness(String businessId) {
        List<Bill> result = new ArrayList<>();
        if (businessId == null)
            throw new IllegalArgumentException("Business ID is null ");

        // check to see if user with the same id exists
        User user = systemRef.getUserManager().findUserById(businessId);
        if (user == null) {
            throw new IllegalArgumentException("No user found with ID: " + businessId);
        }

        // check that the user is company
        if (!(user instanceof Company)) {
            throw new IllegalArgumentException("User with ID " + businessId + " is not a Company.");
        }

        for (Bill b : bills) {
            // Assuming Bill.getBusinessId() now returns String
            if (businessId.equals(b.getBusinessId()) && b.isActive() && !b.isPaid()
                    && b.getExpireTime().isAfter(systemRef.getTime())) {
                result.add(b);
            }
        }
        return result;
    }

    public void markBillAsPaid(String RF) {
        if (RF == null || RF.isBlank()) {
            throw new IllegalArgumentException("RF cannot be null or empty.");
        }

        // Βρες ΠΡΩΤΑ οποιοδήποτε bill με αυτό το RF
        Bill billToPay = null;
        for (Bill b : bills) {
            if (RF.equals(b.getRF())) {
                billToPay = b;
                break;
            }
        }

        if (billToPay == null) {
            throw new IllegalArgumentException("No bill found with RF: " + RF);
        }

        if (billToPay.isPaid()) {
            throw new IllegalStateException("Bill is already paid.");
        }

        if (!billToPay.isActive()) {
            throw new IllegalStateException("Bill is not active.");
        }

        billToPay.markAsPaid();
    }

    public ArrayList<Bill> getBillsForBusinessCustomerPair(String customerId, String businessId)
            throws IllegalArgumentException {
        ArrayList<Bill> result = new ArrayList<>();

        User customer = systemRef.getUserManager().findUserById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Invalid customer ID.");
        }

        User business = systemRef.getUserManager().findUserById(businessId);
        if (business == null || !(business instanceof Company)) {
            throw new IllegalArgumentException("Invalid business ID or not a company.");
        }

        for (Bill b : bills) {
            if (customerId.equals(b.getCustomerId()) && businessId.equals(b.getBusinessId())) {
                result.add(b);
            }
        }

        return result;
    }

    public void deactivateBillsWithRF(String RF) {
        if (RF == null)
            return;
        for (Bill b : bills) {
            if (RF.equals(b.getRF())) {
                b.setActive(false);
            }
        }
    }

    public Bill getActiveBillByRf(String RF) {
        if (RF == null)
            return null;
        for (Bill b : bills) {
            if (RF.equals(b.getRF()) && b.isActive() && !b.isPaid() && b.getExpireTime().isAfter(systemRef.getTime())) {
                return b;
            }
        }
        return null;
    }

    public Bill findActiveBillByRF(String rF) {
        if (rF == null || rF.isEmpty()) {
            throw new IllegalArgumentException("RF cannot be null or empty");
        }

        for (Bill bill : bills) {
            if (rF.equals(bill.getRF()) &&
                    bill.isActive() &&
                    !bill.isPaid() &&
                    bill.getExpireTime().isAfter(systemRef.getTime())) {
                return bill;
            }
        }

        return null; // Δεν βρέθηκε ενεργός λογαριασμός με αυτό το RF
    }

}
