package test;

import managers.*;

import models.accounts.BankAccount;
import models.accounts.PersonalAccount; // For casting if needed
import models.bills.Bill; // For mocking pay
import models.statements.AccountStatement;
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionManagerTest {

    private BankSystem bankSystem;
    private TransactionManager transactionManager;
    private AccountManager accountManager;
    private UserManager userManager;
    private AccountStatementManager accountStatementManager;
    // BillManager might be needed for 'pay' if we don't mock deeply
    private BillManager billManager;


    private String individualId1, individualId2, adminId;
    private final String BANK_USER_ID = "-1"; // Assuming bank ID is also string
    private String iban1, iban2, iban3_business; // iban3 for business
    private String companyId;


    @Before
    public void setUp() throws Exception {
        bankSystem = new BankSystem();
        transactionManager = bankSystem.getTransactionManager();
        accountManager = bankSystem.getAccountManager();
        userManager = bankSystem.getUserManager();
        accountStatementManager = bankSystem.getAccountStatementManager();
        billManager = bankSystem.getBillManager(); // Initialize BillManager

        // Setup users
        userManager.register("Individual", "userOne", "pass", "User One", "111111111");
        individualId1 = userManager.login("userOne", "pass").getId();

        userManager.register("Individual", "userTwo", "pass", "User Two", "222222222");
        individualId2 = userManager.login("userTwo", "pass").getId();

        userManager.register("Admin", "admin", "adminPass", "Admin User", null);
        adminId = userManager.login("admin", "adminPass").getId();
        
        userManager.register("Company", "compOne", "compPass", "Company One", "333333333");
        companyId = userManager.login("compOne", "compPass").getId();

        // Setup accounts
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, new ArrayList<String>());
        iban1 = accountManager.findAccountsByIndividualId(individualId1).get(0).getIBAN();

        accountManager.createPersonalAccount(individualId2, "GR", 0.01, new ArrayList<String>());
        iban2 = accountManager.findAccountsByIndividualId(individualId2).get(0).getIBAN();
        
        accountManager.createBusinessAccount(companyId, "GR", 0.01);
        iban3_business = accountManager.findAccountByBusinessId(companyId).getIBAN();


        // Pre-fund iban1 for withdrawal/transfer tests
        // Directly manipulating balance or using a "bank deposit"
        BankAccount acc1 = accountManager.findAccountByIBAN(iban1);
        acc1.addToBalance(1000.0); // Start with 1000
    }

    // --- Deposit Tests ---
    @Test
    public void testDeposit_Success() {
        double initialBalance = accountManager.findAccountByIBAN(iban2).getBalance();
        transactionManager.deposit(iban2, individualId2, "Initial deposit", 200.0);
        assertEquals(initialBalance + 200.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
        List<AccountStatement> statements = accountStatementManager.getStatements(iban2);
        assertFalse(statements.isEmpty());
        assertEquals("deposit", statements.get(0).getTransactionType()); // Assuming latest is first
        assertEquals(200.0, statements.get(0).getAmount(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_InvalidAccountIBAN_ThrowsException() {
        transactionManager.deposit("INVALID_IBAN", individualId1, "Deposit fail", 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_NonExistentTransactorId_ThrowsException() {
        transactionManager.deposit(iban1, "999", "Deposit fail", 100.0);
    }
    
    @Test
    public void testDeposit_UnrelatedTransactor_Allowed() {
        // individualId2 trying to deposit into iban1 (owned by individualId1)
        // Deposits are generally allowed by anyone into any account.
        // The transactorId is for logging who initiated it.
        double initialBalanceIban1 = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.deposit(iban1, individualId2, "Deposit by unrelated", 100.0);
        assertEquals(initialBalanceIban1 + 100.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        // Verify statement if necessary, ensuring transactorId is logged as individualId2
        List<AccountStatement> statements = accountStatementManager.getStatements(iban1);
        // Find the specific statement if multiple deposits exist
        AccountStatement lastStatement = statements.get(0); // Assuming latest first
        assertEquals(individualId2, lastStatement.getTransactorId());
        assertEquals("Deposit by unrelated", lastStatement.getDescription());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_NegativeAmount_ThrowsException() {
        transactionManager.deposit(iban1, individualId1, "Negative deposit", -50.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_ZeroAmount_ThrowsException() {
        transactionManager.deposit(iban1, individualId1, "Zero deposit", 0.0);
    }

    @Test
    public void testDeposit_Success_ByBankUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.deposit(iban1, BANK_USER_ID, "Bank deposit", 500.0);
        assertEquals(initialBalance + 500.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    @Test
    public void testDeposit_Success_ByAdminUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.deposit(iban1, adminId, "Admin deposit", 300.0);
        assertEquals(initialBalance + 300.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    // --- Withdraw Tests ---
    @Test
    public void testWithdraw_Success() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // Should be 1000
        transactionManager.withdraw(iban1, individualId1, "Cash withdrawal", 100.0);
        assertEquals(initialBalance - 100.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        List<AccountStatement> statements = accountStatementManager.getStatements(iban1);
        // Statements for initial funding + this withdrawal
        boolean foundWithdraw = statements.stream().anyMatch(s -> "withdraw".equals(s.getTransactionType()) && s.getAmount() == 100.0);
        assertTrue("Withdraw statement not found or incorrect.", foundWithdraw);
    }

    @Test
    public void testWithdraw_InsufficientFunds_ThrowsException_NoStatement_BalanceUnchanged() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        int initialStatementCount = accountStatementManager.getStatements(iban1).size();
        try {
            transactionManager.withdraw(iban1, individualId1, "Withdraw too much", initialBalance + 2000.0);
            fail("Should throw IllegalStateException for insufficient funds.");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals(initialBalance, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(initialStatementCount, accountStatementManager.getStatements(iban1).size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWithdraw_UnauthorizedUser_ThrowsException() {
        // individualId2 (not owner of iban1) tries to withdraw from iban1
        transactionManager.withdraw(iban1, individualId2, "Unauthorized withdraw", 50.0);
    }

    @Test
    public void testWithdraw_Success_ByBankUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.withdraw(iban1, BANK_USER_ID, "Bank withdrawal", 50.0);
        assertEquals(initialBalance - 50.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    @Test
    public void testWithdraw_Success_ByAdminUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.withdraw(iban1, adminId, "Admin withdrawal", 50.0);
        assertEquals(initialBalance - 50.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }


    // --- Transfer Tests ---
    @Test
    public void testTransfer_Success() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();

        transactionManager.transfer(iban1, individualId1, "Payment for services", 150.0, iban2);
        
        assertEquals(senderInitial - 150.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial + 150.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);

        List<AccountStatement> senderStatements = accountStatementManager.getStatements(iban1);
        AccountStatement senderStmt = senderStatements.stream()
            .filter(s -> "transfer_out".equals(s.getTransactionType()) && s.getAmount() == 150.0 && iban2.equals(s.getReceiverIBAN()))
            .findFirst().orElse(null);
        assertNotNull("Sender transfer_out statement not found or incorrect.", senderStmt);


        List<AccountStatement> receiverStatements = accountStatementManager.getStatements(iban2);
        AccountStatement receiverStmt = receiverStatements.stream()
            .filter(s -> "transfer_in".equals(s.getTransactionType()) && s.getAmount() == 150.0)
            .findFirst().orElse(null);
        assertNotNull("Receiver transfer_in statement not found or incorrect.", receiverStmt);
    }

    @Test
    public void testTransfer_SenderInsufficientFunds_ThrowsException_NoStatements_BalancesUnchanged() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();
        int senderInitialStmtCount = accountStatementManager.getStatements(iban1).size();
        int receiverInitialStmtCount = accountStatementManager.getStatements(iban2).size();

        try {
            transactionManager.transfer(iban1, individualId1, "Large transfer", senderInitial + 500.0, iban2);
            fail("Should throw IllegalStateException for insufficient funds.");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals(senderInitial, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
        assertEquals(senderInitialStmtCount, accountStatementManager.getStatements(iban1).size());
        assertEquals(receiverInitialStmtCount, accountStatementManager.getStatements(iban2).size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_SameSenderAndReceiverIBAN_ThrowsException() {
        transactionManager.transfer(iban1, individualId1, "Self transfer", 50.0, iban1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_UnauthorizedUser_ThrowsException() {
        // individualId2 (not owner of iban1) tries to transfer from iban1
        transactionManager.transfer(iban1, individualId2, "Unauthorized transfer", 50.0, iban2);
    }
    
    @Test
    public void testTransfer_Success_ByAdmin() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();

        transactionManager.transfer(iban1, adminId, "Admin transfer", 150.0, iban2);
        
        assertEquals(senderInitial - 150.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial + 150.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
    }

    // --- Pay Tests ---
    // These require BillManager to be functional or mocked.
    // For now, let's assume a simplified BillManager interaction for unit testing TransactionManager.
    // We will need a way to tell BillManager "this RF exists, has this amount, and maps to this business (receiver IBAN)".

    @Test
    public void testPay_Success() throws Exception {
        // Simplified setup: Assume BillManager can provide Bill details.
        // This is where mocking BillManager would be ideal for a unit test.
        // Since we are not using a mocking framework, we'll have to rely on
        // BillManager's actual (potentially TODO) implementation or make assumptions.

        // Let's assume we can add a bill to BillManager for testing.
        // Bill constructor: String id, String businessId, String customerId, String RF, double amount, LocalDateTime timePublished, LocalDateTime expireTime
        String testRF = "RF12345";
        double billAmount = 75.0;
        String billId = "BILL001"; // Bill ID as String
        // The bill's businessId is companyId, which owns iban3_business
        // Assuming Bill constructor now takes String IDs
        Bill testBill = new Bill(billId, companyId, individualId1, testRF, billAmount, LocalDateTime.now(), LocalDateTime.now().plusDays(30));
        
        // This is a placeholder for how you'd make the bill known to BillManager.
        // if (billManager != null) {
        //     // billManager.addBill(testBill); // Example: Method to add/register a bill for lookup
        // } else {
        //     fail("BillManager is null. Cannot setup test for pay.");
        // }
        // For this test, it's critical that TransactionManager can resolve 'testRF' via BillManager.
        // This requires BillManager to be implemented and populated with this bill.
        // Without a working BillManager or a mock, this test is speculative.

        System.out.println("Warning: testPay_Success depends on BillManager returning valid bill details for RF: " + testRF +
                           " (amount " + billAmount + ", receiver " + iban3_business + ", billId " + billId + ")" +
                           " and marking it as paid.");

        double payerInitialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double businessInitialBalance = accountManager.findAccountByIBAN(iban3_business).getBalance();

        try {
            // To make this test runnable, BillManager *must* be able to resolve RF12345.
            // If TransactionManager calls billManager.getBillByRF(testRF) and it returns null (or throws),
            // then the transaction will fail.
            // For a "success" test, we have to assume BillManager is correctly pre-populated.
            // This is an integration point that is hard to unit test for TransactionManager in isolation
            // without mocks or stubs for BillManager.

            // To make it more likely to pass in a simple system, let's assume TransactionManager.pay
            // *might* have a fallback or that BillManager is very basic and populated in its constructor.
            // For now, this will likely fail if BillManager is not fully implemented.
            transactionManager.pay(iban1, individualId1, "Paying bill " + testRF, testRF);

            assertEquals(payerInitialBalance - billAmount, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
            assertEquals(businessInitialBalance + billAmount, accountManager.findAccountByIBAN(iban3_business).getBalance(), 0.001);

            List<AccountStatement> payerStmts = accountStatementManager.getStatements(iban1);
            assertTrue("Payer statement for payment_out not found.",
                        payerStmts.stream().anyMatch(s -> "payment_out".equals(s.getTransactionType()) && s.getAmount() == billAmount));

            List<AccountStatement> receiverStmts = accountStatementManager.getStatements(iban3_business);
            assertTrue("Receiver statement for payment_in not found.",
                        receiverStmts.stream().anyMatch(s -> "payment_in".equals(s.getTransactionType()) && s.getAmount() == billAmount));
            
            // Check if bill is marked as paid (this implies BillManager has such a method)
            // Bill foundBill = billManager.getBillByRF(testRF); // or getBillById(billId)
            // assertNotNull("Bill should still exist", foundBill);
            // assertTrue("Bill should be marked as paid", foundBill.isPaid()); // Assuming isPaid() method exists

        } catch (Exception e) {
             System.err.println("testPay_Success failed, likely due to BillManager dependency or bill RF '" + testRF + "' not found/setup: " + e.getMessage());
             e.printStackTrace();
             // If RF not found should throw IllegalArgumentException from TransactionManager (or BillManager):
             // assertInstanceOf(IllegalArgumentException.class, e);
             // For this test to pass, the above setup for BillManager must be effective.
             fail("testPay_Success failed: " + e.getMessage());
        }
    }
}