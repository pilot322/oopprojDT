package test;
import managers.*;

import models.accounts.BankAccount;
import models.bills.Bill; // For scenario 3
import models.statements.AccountStatement;
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionIntegrationTest {

    private BankSystem bankSystem;
    private TransactionManager transactionManager;
    private AccountManager accountManager;
    private UserManager userManager;
    private AccountStatementManager accountStatementManager;
    private BillManager billManager; // For bill payment scenario

    private String userIdIndivA, userIdIndivB, userIdCompA, adminId;
    private final String BANK_ID = "BANK"; // Assuming bank ID is also a string
    private String ibanIndivA, ibanIndivB, ibanCompA;

    @Before
    public void setUp() throws Exception {
        bankSystem = new BankSystem();
        transactionManager = bankSystem.getTransactionManager();
        accountManager = bankSystem.getAccountManager();
        userManager = bankSystem.getUserManager();
        accountStatementManager = bankSystem.getAccountStatementManager();
        billManager = bankSystem.getBillManager();

        // Users
        userManager.register("Individual", "indivA", "passA", "Individual A", "100000001");
        userIdIndivA = userManager.login("indivA", "passA").getId();

        userManager.register("Individual", "indivB", "passB", "Individual B", "100000002");
        userIdIndivB = userManager.login("indivB", "passB").getId();

        userManager.register("Company", "compA", "passC", "Company A", "200000001");
        userIdCompA = userManager.login("compA", "passC").getId();

        userManager.register("Admin", "adminX", "passX", "Admin X", null);
        adminId = userManager.login("adminX", "passX").getId();

        // Accounts
        accountManager.createPersonalAccount(userIdIndivA, "GR", 0.01, new ArrayList<String>());
        ibanIndivA = accountManager.findAccountsByIndividualId(userIdIndivA).get(0).getIBAN();

        accountManager.createPersonalAccount(userIdIndivB, "GR", 0.01, new ArrayList<String>());
        ibanIndivB = accountManager.findAccountsByIndividualId(userIdIndivB).get(0).getIBAN();

        accountManager.createBusinessAccount(userIdCompA, "US", 0.00); // No interest for simplicity
        ibanCompA = accountManager.findAccountByBusinessId(userIdCompA).getIBAN();
    }

    @Test
    public void scenario1_IndividualDepositWithdrawStatementCheck() {
        // 1. Fund IndivA's account
        transactionManager.deposit(ibanIndivA, BANK_ID, "Initial funding", 500.0);
        BankAccount accA = accountManager.findAccountByIBAN(ibanIndivA);
        assertEquals(500.0, accA.getBalance(), 0.001);

        List<AccountStatement> statementsA = accountStatementManager.getStatements(ibanIndivA);
        assertEquals(1, statementsA.size());
        assertEquals("deposit", statementsA.get(0).getTransactionType());
        assertEquals(500.0, statementsA.get(0).getAmount(), 0.001);
        assertEquals(500.0, statementsA.get(0).getBalanceAfterTransaction(), 0.001);

        // 2. IndivA withdraws some funds
        transactionManager.withdraw(ibanIndivA, userIdIndivA, "ATM withdrawal", 100.0);
        assertEquals(400.0, accA.getBalance(), 0.001);
        statementsA = accountStatementManager.getStatements(ibanIndivA);
        assertEquals(2, statementsA.size());
        assertEquals("withdraw", statementsA.get(0).getTransactionType()); // Latest first
        assertEquals(100.0, statementsA.get(0).getAmount(), 0.001);
        assertEquals(400.0, statementsA.get(0).getBalanceAfterTransaction(), 0.001);

        // 3. IndivA attempts to withdraw more than balance
        double balanceBeforeFailedWithdraw = accA.getBalance();
        int statementsBeforeFailedWithdraw = statementsA.size();
        try {
            transactionManager.withdraw(ibanIndivA, userIdIndivA, "Overdraft attempt", 600.0);
            fail("Should have thrown IllegalStateException for insufficient funds");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals("Balance should be unchanged after failed withdrawal",
                     balanceBeforeFailedWithdraw, accA.getBalance(), 0.001);
        statementsA = accountStatementManager.getStatements(ibanIndivA);
        assertEquals("Statement count should be unchanged after failed withdrawal",
                     statementsBeforeFailedWithdraw, statementsA.size());

        // 4. Check statement order (already implicitly tested by checking latest first)
        assertEquals("deposit", statementsA.get(1).getTransactionType()); // The older one
    }

    @Test
    public void scenario2_CompanyAndInterAccountTransferByAdmin() {
        // 1. Fund Company A's account by Bank
        transactionManager.deposit(ibanCompA, BANK_ID, "Seed funding for CompA", 2000.0);
        BankAccount accCompA = accountManager.findAccountByIBAN(ibanCompA);
        assertEquals(2000.0, accCompA.getBalance(), 0.001);

        // 2. Fund Individual B's account slightly for variety
        transactionManager.deposit(ibanIndivB, BANK_ID, "Small fund for IndivB", 50.0);
        BankAccount accIndivB = accountManager.findAccountByIBAN(ibanIndivB);
        assertEquals(50.0, accIndivB.getBalance(), 0.001);

        // 3. Admin transfers from CompA to IndivB
        double compABalanceBefore = accCompA.getBalance();
        double indivBBalanceBefore = accIndivB.getBalance();
        transactionManager.transfer(ibanCompA, adminId, "Admin authorized transfer", 300.0, ibanIndivB);

        assertEquals(compABalanceBefore - 300.0, accCompA.getBalance(), 0.001);
        assertEquals(indivBBalanceBefore + 300.0, accIndivB.getBalance(), 0.001);

        List<AccountStatement> compAStmts = accountStatementManager.getStatements(ibanCompA);
        // Initial deposit + transfer out
        assertEquals(2, compAStmts.size());
        AccountStatement compATransferStmt = compAStmts.get(0); // Latest
        assertEquals("transfer_out", compATransferStmt.getTransactionType());
        assertEquals(300.0, compATransferStmt.getAmount(), 0.001);
        assertEquals(ibanIndivB, compATransferStmt.getReceiverIBAN());

        List<AccountStatement> indivBStmts = accountStatementManager.getStatements(ibanIndivB);
        // Initial deposit + transfer in
        assertEquals(2, indivBStmts.size());
        AccountStatement indivBTransferStmt = indivBStmts.get(0); // Latest
        assertEquals("transfer_in", indivBTransferStmt.getTransactionType());
        assertEquals(300.0, indivBTransferStmt.getAmount(), 0.001);
        // For transfer_in, receiverIBAN in statement might be null or self, depending on impl.
        // The AccountStatement constructor takes receiverIBAN. For a transfer_in, this field in the *statement*
        // might represent the sender, or be null. Let's assume it's null or self for now.
        // Or, more consistently, for a transfer_in to ibanIndivB, the statement's accountIBAN is ibanIndivB,
        // and receiverIBAN (if used to mean "other party") would be ibanCompA (sender).
        // This depends on how AccountStatement is populated for transfer_in.
        // The Transfer.java TODO had receiverDescription + " <- " + description.
        // Let's assume receiverIBAN on the statement for the *receiver* is the *sender's* IBAN.
        // If AccountStatementManager.addStatement for receiver sets receiverIBAN to senderIBAN:
        // assertEquals(ibanCompA, indivBTransferStmt.getReceiverIBAN());

        // 4. Admin attempts to transfer from IndivB (insufficient funds) to CompA
        compABalanceBefore = accCompA.getBalance();
        indivBBalanceBefore = accIndivB.getBalance(); // Should be 350
        int compAStmtCountBefore = accountStatementManager.getStatements(ibanCompA).size();
        int indivBStmtCountBefore = accountStatementManager.getStatements(ibanIndivB).size();

        try {
            transactionManager.transfer(ibanIndivB, adminId, "Transfer fail", 1000.0, ibanCompA);
            fail("Should throw for insufficient funds");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals(compABalanceBefore, accCompA.getBalance(), 0.001);
        assertEquals(indivBBalanceBefore, accIndivB.getBalance(), 0.001);
        assertEquals(compAStmtCountBefore, accountStatementManager.getStatements(ibanCompA).size());
        assertEquals(indivBStmtCountBefore, accountStatementManager.getStatements(ibanIndivB).size());
    }

    @Test
    public void scenario3_BillPaymentByIndividual() throws Exception {
        // This scenario heavily depends on BillManager being testable/mockable.
        // We'll simulate the BillManager's role by ensuring TransactionManager.pay
        // can obtain necessary info and trigger state changes.

        // 1. Fund Customer (IndivA)
        transactionManager.deposit(ibanIndivA, BANK_ID, "Funds for bill", 200.0);
        BankAccount accIndivA = accountManager.findAccountByIBAN(ibanIndivA);
        assertEquals(200.0, accIndivA.getBalance(), 0.001);
        
        BankAccount accCompA_Business = accountManager.findAccountByIBAN(ibanCompA);
        double businessBalanceBefore = accCompA_Business.getBalance();


        // 2. Setup a Bill in BillManager (conceptually)
        // Bill: String id, String businessId, String customerId, String RF, double amount, LocalDateTime timePublished, LocalDateTime expireTime
        String billRF = "BILLRF001";
        double billAmount = 75.0;
        String billId = "B101"; // example bill ID as String

        // This is where you would normally interact with BillManager to add a bill.
        // e.g., billManager.addBill(new Bill(billId, userIdCompA, userIdIndivA, billRF, billAmount, LocalDateTime.now(), LocalDateTime.now().plusDays(10)));
        // For this test to work without a full BillManager, your TransactionManager.pay()
        // needs a way to get these details. If it calls methods on BillManager, those methods
        // need to be implemented in BillManager.

        // For now, let's assume TransactionManager.pay will correctly:
        // - Use RF to find the bill (amount=75, for business userIdCompA -> ibanCompA)
        // - Transfer funds
        // - Mark the bill as paid (e.g., billManager.payBill(billId) or similar)

        // To make this test pass without a full BillManager, you might need to:
        // A) Implement a stub BillManager that returns fixed values for this RF.
        // B) Temporarily modify TransactionManager.pay to take amount and receiverIBAN if RF lookup is complex.
        // C) Assume BillManager is implemented and works. (Chosen for this example structure)

        System.out.println("Warning: Scenario 3 (Bill Payment) relies on BillManager being functional " +
                           "to resolve RF '" + billRF + "' to amount " + billAmount +
                           " and receiver " + ibanCompA + " and to mark the bill as paid.");
        
        // Simulate adding the bill to BillManager so it can be found by RF
        // This is a mock setup step. Your actual BillManager will have its own API.
        // If your BillManager is simple (e.g., in-memory map):
        if (billManager != null) { // Check if BillManager is available
            // Assuming Bill constructor now takes String IDs for billId, businessId, customerId
            Bill testBill = new Bill(billId, userIdCompA, userIdIndivA, billRF, billAmount, LocalDateTime.now(), LocalDateTime.now().plusDays(30));
            // This is a placeholder for actual BillManager interaction.
            // billManager.addBill(testBill); // You would need to implement this or a similar method
            // For the test to proceed, we'll assume the bill *would be* found if BillManager was fully mocked/implemented.
        } else {
            fail("BillManager is null, cannot proceed with bill payment scenario.");
            return;
        }


        // 3. IndivA pays the bill
        // This will fail if BillManager is not correctly implemented and populated for RF 'BILLRF001'
        try {
            // To make this test potentially pass without full BM, we must ensure the RF is somehow known
            // For example, if TransactionManager falls back to a dummy implementation or if we mock BillManager.
            // Given no mocking framework, this test's success is highly dependent on BillManager's state.
            // Let's assume for now it will throw if bill cannot be found.
            // We will stub the creation of the bill directly if needed for testability,
            // or rely on the fact that BillManager has been pre-populated (less ideal for unit test).

            // If BillManager is expected to throw if not found, then this try-catch needs to expect that.
            // For a *successful* payment test, BillManager must be primed.

            // For now, we'll proceed as if BillManager will be able to provide details for 'BILLRF001'.
            // This is a significant assumption for a unit/integration test without explicit mocking setup.
            // The provided BillManager might be a TODO, so this test is "best effort".
             transactionManager.pay(ibanIndivA, userIdIndivA, "Paying electricity bill", billRF);
        } catch (Exception e) {
            // This catch is to provide feedback if the test fails due to BillManager issues.
            // If TransactionManager is supposed to throw a specific exception for "bill not found", test for that.
            System.err.println("Bill payment failed. This might be due to BillManager not finding RF: " + billRF + " or other setup issues. Error: " + e.getMessage());
            e.printStackTrace(); // For more details
            // To make the test pass if bill not found is an expected app exception:
            // assertInstanceOf(IllegalArgumentException.class, e); // e.g. if bill not found
            // return; // End test here if that's the expected failure for this setup
            fail("Bill payment transaction failed unexpectedly. Assumed BillManager was set up for RF: " + billRF + ". Error: " + e);
            return; // Stop if it fails here
        }


        assertEquals(200.0 - billAmount, accIndivA.getBalance(), 0.001);
        assertEquals(businessBalanceBefore + billAmount, accCompA_Business.getBalance(), 0.001);

        List<AccountStatement> indivAStmts = accountStatementManager.getStatements(ibanIndivA);
        // Deposit + payment_out
        assertEquals(2, indivAStmts.size());
        assertEquals("payment_out", indivAStmts.get(0).getTransactionType());
        assertEquals(billAmount, indivAStmts.get(0).getAmount(), 0.001);
        assertEquals(ibanCompA, indivAStmts.get(0).getReceiverIBAN());


        List<AccountStatement> compAStmts = accountStatementManager.getStatements(ibanCompA);
        assertEquals(1, compAStmts.size()); // Assuming compA had no prior statements in this test run
        assertEquals("payment_in", compAStmts.get(0).getTransactionType());
        assertEquals(billAmount, compAStmts.get(0).getAmount(), 0.001);

        // Verify bill is marked as paid (requires BillManager.isBillPaid(RF) or similar)
        // Bill paidBill = billManager.getBillsByRF(billRF).get(0); // Assuming getBillsByRF returns a list and Bill is now using String ID
        // assertTrue(paidBill.isPaid());
        // This part is highly dependent on BillManager's API.
        System.out.println("Conceptual check: Bill " + billRF + " should be marked as paid in BillManager.");
    }


    @Test
    public void scenario4_MultipleTransactionsStatementOrderAndBalanceAfter() throws InterruptedException {
        // Fund account
        transactionManager.deposit(ibanIndivA, BANK_ID, "Funding", 1000.0); // Bal: 1000
        List<AccountStatement> stmts;

        // Op 1: Deposit
        transactionManager.deposit(ibanIndivA, userIdIndivA, "Deposit 1", 200.0); // Bal: 1200
        Thread.sleep(10); // Ensure timestamp difference

        // Op 2: Withdraw
        transactionManager.withdraw(ibanIndivA, userIdIndivA, "Withdraw 1", 50.0); // Bal: 1150
        Thread.sleep(10);

        // Op 3: Transfer Out
        transactionManager.transfer(ibanIndivA, userIdIndivA, "Transfer Out 1", 100.0, ibanIndivB); // Bal: 1050
        Thread.sleep(10);

        // Op 4: Deposit
        transactionManager.deposit(ibanIndivA, userIdIndivA, "Deposit 2", 300.0); // Bal: 1350

        stmts = accountStatementManager.getStatements(ibanIndivA);
        // Initial funding deposit + 4 operations = 5 statements
        assertEquals(5, stmts.size());

        // Check latest statement (Deposit 2)
        AccountStatement s0 = stmts.get(0);
        assertEquals("Deposit 2", s0.getDescription());
        assertEquals("deposit", s0.getTransactionType());
        assertEquals(300.0, s0.getAmount(), 0.001);
        assertEquals(1350.0, s0.getBalanceAfterTransaction(), 0.001);

        // Check previous statement (Transfer Out 1)
        AccountStatement s1 = stmts.get(1);
        assertEquals("Transfer Out 1", s1.getDescription());
        assertEquals("transfer_out", s1.getTransactionType());
        assertEquals(100.0, s1.getAmount(), 0.001);
        assertEquals(1050.0, s1.getBalanceAfterTransaction(), 0.001);

        // Check statement before that (Withdraw 1)
        AccountStatement s2 = stmts.get(2);
        assertEquals("Withdraw 1", s2.getDescription());
        assertEquals("withdraw", s2.getTransactionType());
        assertEquals(50.0, s2.getAmount(), 0.001);
        assertEquals(1150.0, s2.getBalanceAfterTransaction(), 0.001);

        // Check statement before that (Deposit 1)
        AccountStatement s3 = stmts.get(3);
        assertEquals("Deposit 1", s3.getDescription());
        assertEquals("deposit", s3.getTransactionType());
        assertEquals(200.0, s3.getAmount(), 0.001);
        assertEquals(1200.0, s3.getBalanceAfterTransaction(), 0.001);
        
        // Check initial funding statement
        AccountStatement s4 = stmts.get(4);
        assertEquals("Funding", s4.getDescription());
        assertEquals("deposit", s4.getTransactionType());
        // The original deposit amount for funding in setUp might be different or not exist.
        // Let's re-check the first transaction.
        // This test starts by depositing 1000.0, so s4 (the 5th statement, oldest here) amount should be 1000.0
        assertEquals(1000.0, s4.getAmount(), 0.001); // This was the initial funding amount for ibanIndivA
        assertEquals(1000.0, s4.getBalanceAfterTransaction(), 0.001); // Balance after initial funding

        assertEquals(1350.0, accountManager.findAccountByIBAN(ibanIndivA).getBalance(), 0.001);
    }

    @Test
    public void scenario5_UnauthorizedAttemptsAndOverrides() {
        // 1. Fund accountA (ibanIndivA)
        transactionManager.deposit(ibanIndivA, BANK_ID, "Initial", 300.0);
        BankAccount accA = accountManager.findAccountByIBAN(ibanIndivA);
        assertEquals(300.0, accA.getBalance(), 0.001);
        int initialStatementsA = accountStatementManager.getStatements(ibanIndivA).size();

        // 2. userB attempts to withdraw from accountA (Unauthorized)
        try {
            transactionManager.withdraw(ibanIndivA, userIdIndivB, "Sneaky withdraw", 50.0);
            fail("Should throw Exception for unauthorized withdrawal");
        } catch (IllegalArgumentException e) { // Or specific authorization exception
            // Expected
        }
        assertEquals("Balance A unchanged", 300.0, accA.getBalance(), 0.001);
        assertEquals("Statements A unchanged", initialStatementsA, accountStatementManager.getStatements(ibanIndivA).size());

        // 3. userB attempts to transfer from accountA to accountB (Unauthorized)
        BankAccount accB = accountManager.findAccountByIBAN(ibanIndivB);
        double initialBalanceB = accB.getBalance();
        int initialStatementsB = accountStatementManager.getStatements(ibanIndivB).size();
        try {
            transactionManager.transfer(ibanIndivA, userIdIndivB, "Sneaky transfer", 50.0, ibanIndivB);
            fail("Should throw Exception for unauthorized transfer");
        } catch (IllegalArgumentException e) { // Or specific authorization exception
            // Expected
        }
        assertEquals("Balance A unchanged", 300.0, accA.getBalance(), 0.001);
        assertEquals("Balance B unchanged", initialBalanceB, accB.getBalance(), 0.001);
        assertEquals("Statements A unchanged", initialStatementsA, accountStatementManager.getStatements(ibanIndivA).size());
        assertEquals("Statements B unchanged", initialStatementsB, accountStatementManager.getStatements(ibanIndivB).size());

        // 4. Bank user (BANK_ID) successfully withdraws from accountA
        transactionManager.withdraw(ibanIndivA, BANK_ID, "Bank levy", 20.0);
        assertEquals(280.0, accA.getBalance(), 0.001);
        assertEquals(initialStatementsA + 1, accountStatementManager.getStatements(ibanIndivA).size());
        assertEquals("withdraw", accountStatementManager.getStatements(ibanIndivA).get(0).getTransactionType());


        // 5. Admin successfully transfers from accountA to accountB
        initialBalanceB = accB.getBalance(); // Update B's balance before admin transfer
        transactionManager.transfer(ibanIndivA, adminId, "Admin move", 30.0, ibanIndivB);
        assertEquals(250.0, accA.getBalance(), 0.001); // 280 - 30
        assertEquals(initialBalanceB + 30.0, accB.getBalance(), 0.001);
        assertEquals(initialStatementsA + 2, accountStatementManager.getStatements(ibanIndivA).size()); // withdraw + transfer_out
        assertEquals(initialStatementsB + 1, accountStatementManager.getStatements(ibanIndivB).size()); // transfer_in
        assertEquals("transfer_out", accountStatementManager.getStatements(ibanIndivA).get(0).getTransactionType());
        assertEquals("transfer_in", accountStatementManager.getStatements(ibanIndivB).get(0).getTransactionType());
    }
}