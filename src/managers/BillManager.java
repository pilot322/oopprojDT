package managers;

import java.util.ArrayList;
import java.util.List;

import models.bills.Bill;
import system.BankSystem;

public class BillManager extends Manager {
    public BillManager(BankSystem system) {
        super(system);
    }

    private List<Bill> bills = new ArrayList<>();

    public void addBill(Bill bill) {
        bills.add(bill);
        ;
    }

    public Bill getBillById(int id) {
        for (Bill bill : bills) {
            if (bill.getId() == id) {
                return bill;
            }
        }
        return null;

    }

    public List<Bill> getBillsByRF(String RF) {
        List<Bill> result = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getRF().equals(RF)) {
                result.add(bill);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForCustomer(int customerId) {
        List<Bill> result = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getCustomerId() == customerId && bill.isActive()) {
                result.add(bill);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForBusiness(int businessId) {
        List<Bill> result = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getBusinessId() == businessId && bill.isActive()) {
                result.add(bill);
            }
        }
        return result;
    }

    public boolean payBill(int billId) {
        for (Bill bill : bills) {
            if (bill.getId() == billId && !bill.isPaid()) {
                bill.markAsPaid();
                return true;
            }
        }
        return false;
    }

    public void deactivateBillsWithRF(String RF) {
        for (Bill bill : bills) {
            if (bill.getRF().equals(RF)) {
                bill.setActive(false);
            }
        }
    }

}