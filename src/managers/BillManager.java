package managers;

import java.util.List;

import models.bills.Bill;
import system.BankSystem;

public class BillManager extends Manager{
    public BillManager(BankSystem system) {
        super(system);
    }

    public void addBill(Bill bill) {
        throw new RuntimeException("TODO");
    }


    public Bill getBillById(int id) {
        throw new RuntimeException("TODO");
    }

    public List<Bill> getBillsByRF(String RF) {
        throw new RuntimeException("TODO");
    }

    public List<Bill> getActiveBillsForCustomer(int customerId) {
        throw new RuntimeException("TODO");
    }

    public List<Bill> getActiveBillsForBusiness(int businessId) {
        throw new RuntimeException("TODO");
    }

    public boolean payBill(int billId) {
        throw new RuntimeException("TODO");
    }

    public void deactivateBillsWithRF(String RF) {
        throw new RuntimeException("TODO");
    }


}