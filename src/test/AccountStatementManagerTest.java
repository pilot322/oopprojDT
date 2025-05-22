package test;

import managers.*;
import models.statements.AccountStatement;
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AccountStatementManagerTest {

    private BankSystem bankSystem;
    private AccountStatementManager accountStatementManager;
    private AccountManager accountManager;
    private UserManager userManager;

    private String testIBAN1 = "GR100TESTIBAN001";
    private String testIBAN2 = "GR100TESTIBAN002"; // For receiver
    private String nonExistentIBAN = "GR000NONEXISTENT";
    private String testUserId1;
    private String nonExistentUserId = "999";
    private static int statementIdNumericCounter = 1; // Helper for predictable statement IDs in tests if they remain numeric for some reason
                                                 // If statement IDs also become strings, this needs adjustment or removal.
                                                 // For now, assuming statement ID itself might not change based on "user id, bill id etc."

    // Helper to create unique statement IDs for testing if your SUT doesn't assign them
    // or if you want to predict them.
    // If AccountStatementManager assigns IDs, this is not strictly needed for SUT interaction
    // but helps in crafting expected AccountStatement objects.
    // private String nextStatementId() { // If statement ID becomes string
    //     return "SID" + statementIdNumericCounter++;
    // }

    @Before
    public void setUp() throws Exception { // Added throws Exception for account creation
        bankSystem = new BankSystem();
        accountStatementManager = bankSystem.getAccountStatementManager();
        accountManager = bankSystem.getAccountManager();
        userManager = bankSystem.getUserManager();

        // Register a user
        userManager.register("Individual", "stmtUser", "pass", "Statement User", "123456789");
        testUserId1 = userManager.login("stmtUser", "pass").getId();

        // Create accounts for testing
        // AccountManager's create methods might throw Exception
        accountManager.createPersonalAccount(testUserId1, "GR", 0.01, new ArrayList<String>());
        // Find the created IBAN - this is a bit indirect.
        // For robust tests, AccountManager.createPersonalAccount could return the created account or IBAN.
        // Assuming the first account for testUserId1 is testIBAN1 for simplicity here if generateIBAN is predictable
        // Or, we find it:
        if (!accountManager.findAccountsByIndividualId(testUserId1).isEmpty()) {
            testIBAN1 = accountManager.findAccountsByIndividualId(testUserId1).get(0).getIBAN();
        } else {
            fail("Setup failed: Could not create or find account for testUserId1");
        }

        // Create a second user and account for receiverIBAN scenarios
        userManager.register("Individual", "recvUser", "pass", "Receiver User", "987654321");
        String recvUserId = userManager.login("recvUser", "pass").getId();
        accountManager.createPersonalAccount(recvUserId, "GR", 0.01, new ArrayList<String>());
        if (!accountManager.findAccountsByIndividualId(recvUserId).isEmpty()) {
            testIBAN2 = accountManager.findAccountsByIndividualId(recvUserId).get(0).getIBAN();
        } else {
            fail("Setup failed: Could not create or find account for recvUserId");
        }
        statementIdNumericCounter = 1; // Reset for each test
    }

    @Test
    public void testAddStatement_Success_AndGetStatements_Single() {
        // Parameters for addStatement: accountIBAN, transactorId, description, amount, balanceAfter, type, receiverIBAN
        // Note: AccountStatementManager is responsible for creating the AccountStatement object,
        // including its ID and timestamp.
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Test Deposit 1", 100.0, 100.0, "deposit", null);

        List<AccountStatement> statements = accountStatementManager.getStatements(testIBAN1);
        assertNotNull(statements);
        assertEquals(1, statements.size());

        AccountStatement retrieved = statements.get(0);
        assertEquals(testIBAN1, retrieved.getAccountIBAN());
        assertEquals(testUserId1, retrieved.getTransactorId()); // Assuming getTransactorId returns String
        assertEquals("Test Deposit 1", retrieved.getDescription());
        assertEquals(100.0, retrieved.getAmount(), 0.001);
        assertEquals(100.0, retrieved.getBalanceAfterTransaction(), 0.001);
        assertEquals("deposit", retrieved.getTransactionType());
        assertNull(retrieved.getReceiverIBAN()); // Or assertEquals("", retrieved.getReceiverIBAN()) if it defaults to empty string
        assertNotNull(retrieved.getTransactionTime());
        // assertEquals(1, retrieved.getId()); // If ID generation is predictable and numeric
        // If statement ID becomes string:
        // assertNotNull(retrieved.getId());
    }

    @Test
    public void testAddStatement_Multiple_CheckOrderDescendingTimestamp() throws InterruptedException {
        // Timestamps are crucial here. LocalDate.now() might be too fast.
        // For robust testing of order, you might need to inject a Clock or pass LocalDate to addStatement.
        // Assuming addStatement uses LocalDate.now() internally.
        // We'll add them with slight delays if possible, or rely on the internal list management for order.

        LocalDate time1 = LocalDate.now();
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Op 1", 50.0, 50.0, "deposit", null);
        Thread.sleep(10); // Introduce a small delay for distinct timestamps

        LocalDate time2 = LocalDate.now();
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Op 2", 20.0, 30.0, "withdraw", null);
        Thread.sleep(10);

        LocalDate time3 = LocalDate.now();
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Op 3", 100.0, 130.0, "deposit", null);

        List<AccountStatement> statements = accountStatementManager.getStatements(testIBAN1);
        
        for(AccountStatement st : statements){
            System.out.println(st.marshal());
        }
        
        assertEquals(3, statements.size());

        // Verify descending order by timestamp (latest first)
        assertTrue("Timestamp of statement 0 should be after or equal to statement 1",
                   !statements.get(0).getTransactionTime().isBefore(statements.get(1).getTransactionTime()));
        assertTrue("Timestamp of statement 1 should be after or equal to statement 2",
                   !statements.get(1).getTransactionTime().isBefore(statements.get(2).getTransactionTime()));

        // Check if the latest operation (Op 3) is first
        assertEquals("Op 3", statements.get(0).getDescription());
        assertEquals("Op 2", statements.get(1).getDescription());
        assertEquals("Op 1", statements.get(2).getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_InvalidAccountIBAN_ThrowsException() {
        // Assuming AccountStatementManager validates IBAN existence via AccountManager
        accountStatementManager.addStatement(nonExistentIBAN, testUserId1, "Test", 10.0, 10.0, "deposit", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_InvalidTransactorId_ThrowsException() {
        // Assuming AccountStatementManager validates transactorId existence via UserManager
        accountStatementManager.addStatement(testIBAN1, nonExistentUserId, "Test", 10.0, 10.0, "deposit", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_NegativeAmountForNonWithdraw_ThrowsException() {
        // While withdraw amount is positive, the effect is negative.
        // The 'amount' field in AccountStatement seems to store the transaction magnitude.
        // Let's assume 'amount' in addStatement should generally correspond to the change.
        // For a deposit, a negative amount is invalid.
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Invalid Deposit", -100.0, 0.0, "deposit", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_ZeroAmount_ThrowsException() {
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Zero Amount Op", 0.0, 50.0, "deposit", null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_InvalidType_ThrowsException() {
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Invalid Type", 10.0, 10.0, "unknown_type", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_NullReceiverIBAN_ForTransferOutType_ThrowsException() {
        // Assuming 'transfer_out' type requires a receiverIBAN
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Transfer Out", 50.0, 0.0, "transfer_out", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddStatement_NonExistentReceiverIBAN_ForTransferOutType_ThrowsException() {
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Transfer Out Bad Recv", 50.0, 0.0, "transfer_out", nonExistentIBAN);
    }

    @Test
    public void testAddStatement_ValidReceiverIBAN_ForTransferOutType_Success() {
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Transfer Out Valid", 50.0, 0.0, "transfer_out", testIBAN2);
        List<AccountStatement> statements = accountStatementManager.getStatements(testIBAN1);
        assertEquals(1, statements.size());
        assertEquals(testIBAN2, statements.get(0).getReceiverIBAN());
        assertEquals("transfer_out", statements.get(0).getTransactionType());
    }


    @Test
    public void testGetStatements_NoStatements_ReturnsEmptyList() {
        List<AccountStatement> statements = accountStatementManager.getStatements("IBANWITHOUTSTATEMENTS");
        assertNotNull(statements);
        assertTrue(statements.isEmpty());
    }

    @Test
    public void testGetStatements_ReturnsCopyNotOriginal() {
        accountStatementManager.addStatement(testIBAN1, testUserId1, "Copy Test", 10.0, 10.0, "deposit", null);
        List<AccountStatement> statements1 = accountStatementManager.getStatements(testIBAN1);
        assertFalse(statements1.isEmpty());

        // Try to modify the returned list
        try {
            statements1.add(null); // This should fail if it's an unmodifiable copy, or not affect the original
        } catch (UnsupportedOperationException e) {
            // This is good, means it's an unmodifiable list
        }

        List<AccountStatement> statements2 = accountStatementManager.getStatements(testIBAN1);
        assertEquals("Original list in manager should not be affected by modifications to the copy",
                     1, statements2.size()); // Check size remains 1
        
        // If add was successful on statements1 (meaning it's a modifiable copy, but a different instance)
        // then statements1.size() would be 2, statements2.size() would be 1.
        // If AccountStatementManager returns a new ArrayList(originalList), then statements1.add will work.
        // The key is that statements1 != statements2 (different instances) and
        // modification to statements1 doesn't change what statements2 gets later.
        if (statements1.size() == 1) { // If add failed or it's unmodifiable
             try {
                statements1.remove(0); // Try another modification
             } catch (UnsupportedOperationException e) {
                // Expected if unmodifiable
             }
        }
         List<AccountStatement> statements3 = accountStatementManager.getStatements(testIBAN1);
         assertEquals(1, statements3.size()); // Should still be 1
         assertNotSame("getStatements should return a new list instance (copy)", statements1, statements3); // This might fail if statements1 was unmodifiable and remove failed
    }
}