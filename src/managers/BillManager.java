package managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Added for null-safe equals if needed, though .equals should handle null correctly

import models.bills.Bill;
import system.BankSystem;

public class BillManager extends Manager {
    private List<Bill> bills = new ArrayList<>();

    public BillManager(BankSystem system) {
        super(system);
    }

    // Assuming Bill class's getBusinessId() and getCustomerId() now return String
    // Assuming Bill class's constructor and other relevant methods are also updated for String IDs

    public void issueBill(String businessId, String customerId, double amount, LocalDateTime expireTime, String oldRF) throws Exception{
        // TODO: Implement actual logic.
        // Validations (should be more robust and use UserManager/AccountManager):
        // - Check if businessId and customerId are valid and of correct types (Company/any User).
        // - Check amount > 0.
        // - Check expireTime is in the future.
        // - Handle oldRF logic (deactivate old, sum amounts, etc.).
        // - Generate new RF if oldRF is null or not found.
        // - Create and add the new Bill object to the 'bills' list.
        // Bill newBill = new Bill(generatedBillId, businessId, customerId, newRf, newAmount, LocalDateTime.now(), expireTime);
        // this.bills.add(newBill);
        throw new RuntimeException("TODO: Implement issueBill with String IDs!");
    }

    public List<Bill> getBillsByRF(String RF) {
        List<Bill> result = new ArrayList<>();
        if (RF == null) return result; // Handle null RF if necessary
        for (Bill b : bills) {
            if (RF.equals(b.getRF())) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForCustomer(String customerId) {
        List<Bill> result = new ArrayList<>();
        if (customerId == null) return result; // Or throw IllegalArgumentException
        // TODO: Add validation that customerId exists via UserManager

        for (Bill b : bills) {
            // Assuming Bill.getCustomerId() now returns String
            if (customerId.equals(b.getCustomerId()) && b.isActive() && !b.isPaid() && b.getExpireTime().isAfter(LocalDateTime.now())) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForBusiness(String businessId) {
        List<Bill> result = new ArrayList<>();
        if (businessId == null) return result; // Or throw IllegalArgumentException
        // TODO: Add validation that businessId exists and is a Company via UserManager

        for (Bill b : bills) {
            // Assuming Bill.getBusinessId() now returns String
            if (businessId.equals(b.getBusinessId()) && b.isActive() && !b.isPaid() && b.getExpireTime().isAfter(LocalDateTime.now())) {
                result.add(b);
            }
        }
        return result;
    }

    public void markBillAsPaid(String RF) {
        // TODO: Implement actual logic.
        // - Find the active, unpaid bill by RF.
        // - If not found, or not active, or already paid, throw appropriate exception.
        // - Mark the bill as paid and inactive.
        // Bill billToPay = getActiveBillByRf(RF);
        // if (billToPay == null) { throw new IllegalArgumentException("Bill with RF " + RF + " not found or not active/unpaid."); }
        // billToPay.setPaid(true);
        // billToPay.setActive(false);
        throw new RuntimeException("TODO: Implement markBillAsPaid with String IDs!");
    }

    public ArrayList<Bill> getBillsForBusinessCustomerPair(String customerId, String businessId){
        // TODO: Implement actual logic.
        // - Validate customerId and businessId.
        // ArrayList<Bill> result = new ArrayList<>();
        // if (customerId == null || businessId == null) return result; // Or throw
        // for (Bill b : bills) {
        //     if (customerId.equals(b.getCustomerId()) && businessId.equals(b.getBusinessId())) {
        //         result.add(b);
        //     }
        // }
        // return result;
        throw new RuntimeException("TODO: Implement getBillsForBusinessCustomerPair with String IDs!");
    }

    public void deactivateBillsWithRF(String RF) {
        if (RF == null) return;
        for (Bill b : bills) {
            if (RF.equals(b.getRF())) {
                b.setActive(false);
            }
        }
    }

    public Bill getActiveBillByRf(String RF) {
        if (RF == null) return null;
        for (Bill b : bills) {
            if (RF.equals(b.getRF()) && b.isActive() && !b.isPaid() && b.getExpireTime().isAfter(LocalDateTime.now())) {
                return b;
            }
        }
        return null;
    }

    public Bill findActiveBillByRF(String rF) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findActiveBillByRF'");
    }

}
