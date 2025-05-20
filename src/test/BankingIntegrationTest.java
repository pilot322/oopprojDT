package test;
import managers.*;

import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import models.users.User;
// JUnit 4 imports
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.util.ArrayList;
import java.util.Arrays;

// JUnit 4 static asserts
import static org.junit.Assert.*;


public class BankingIntegrationTest {

    private BankSystem bankSystem;
    private UserManager userManager;
    private AccountManager accountManager;

    private String indivIdMain, indivIdSec1, indivIdSec2;
    private String companyIdMain;
    private String nonExistentUserId = "999";


    @Before // JUnit 4
    public void setUp() { // Must be public
        bankSystem = new BankSystem();
        userManager = bankSystem.getUserManager();
        accountManager = bankSystem.getAccountManager();
    }

    @Test
    public void testIndividualScenario_AccountManagement() throws Exception { // Added throws Exception
        // 1. Register users
        userManager.register("Individual", "mainIndiv", "pass1", "Main Individual", "100000001");
        indivIdMain = userManager.login("mainIndiv", "pass1").getId();

        userManager.register("Individual", "secIndiv1", "pass2", "Secondary One", "100000002");
        indivIdSec1 = userManager.login("secIndiv1", "pass2").getId();

        userManager.register("Individual", "secIndiv2", "pass3", "Secondary Two", "100000003");
        indivIdSec2 = userManager.login("secIndiv2", "pass3").getId();

        userManager.register("Company", "otherComp", "passC", "Other Company", "200000001");
        String otherCompId = userManager.login("otherComp", "passC").getId();

        // 2. Login "mainIndiv"
        User loggedInUser = userManager.login("mainIndiv", "pass1");
        assertNotNull(loggedInUser);
        assertEquals(indivIdMain, loggedInUser.getId());

        // 3. mainIndiv creates Personal Account 1
        accountManager.createPersonalAccount(indivIdMain, "GR", 0.01, new ArrayList<String>());
        ArrayList<PersonalAccount> mainIndivAccounts = accountManager.findAccountsByIndividualId(indivIdMain);
        assertEquals("Main Individual should have 1 account.", 1, mainIndivAccounts.size());
        PersonalAccount pa1 = mainIndivAccounts.get(0);
        assertEquals(indivIdMain, pa1.getOwnerId());
        assertEquals(0.01, pa1.getInterestRate(), 0.001);
        assertEquals(0.0, pa1.getBalance(), 0.001);
        assertTrue(pa1.getIBAN().startsWith("GR100"));

        // 4. mainIndiv fails to create Business Account
        try {
            accountManager.createBusinessAccount(indivIdMain, "GR", 0.02);
            fail("Individual user should not be able to create a business account. Expected IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        // 5. mainIndiv creates Personal Account 2 with indivIdSec1 and indivIdSec2 as secondary owners
        ArrayList<String> secondaryOwners = new ArrayList<String>(Arrays.asList(indivIdSec1, indivIdSec2));
        accountManager.createPersonalAccount(indivIdMain, "CY", 0.015, secondaryOwners);
        mainIndivAccounts = accountManager.findAccountsByIndividualId(indivIdMain);
        assertEquals("Main Individual should now have 2 accounts.", 2, mainIndivAccounts.size());
        
        // Find the new account (CYprus based)
        PersonalAccount pa2 = null;
        for(PersonalAccount acc : mainIndivAccounts) {
            if(acc.getIBAN().startsWith("CY100")) {
                pa2 = acc;
                break;
            }
        }
        assertNotNull("Could not find the CY account", pa2);
        assertEquals(indivIdMain, pa2.getOwnerId());
        assertEquals(0.015, pa2.getInterestRate(), 0.001);
        assertEquals(2, pa2.getSecondaryOwnerIds().size());
        assertTrue(pa2.getSecondaryOwnerIds().contains(indivIdSec1));
        assertTrue(pa2.getSecondaryOwnerIds().contains(indivIdSec2));


        // 6. mainIndiv fails to create Personal Account 3 with one non-existent secondary owner
        ArrayList<String> secondaryOwnersFail1 = new ArrayList<String>(Arrays.asList(indivIdSec1, nonExistentUserId));
        try {
            accountManager.createPersonalAccount(indivIdMain, "DE", 0.01, secondaryOwnersFail1);
            fail("Creating account with non-existent secondary owner should fail. Expected IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
        assertEquals("Account count should remain 2 after failed creation.", 2, accountManager.findAccountsByIndividualId(indivIdMain).size());


        // 7. mainIndiv fails to create Personal Account 4 with a Company as a secondary owner
        ArrayList<String> secondaryOwnersFail2 = new ArrayList<String>(Arrays.asList(indivIdSec1, otherCompId));
        try {
            accountManager.createPersonalAccount(indivIdMain, "FR", 0.01, secondaryOwnersFail2);
            fail("Creating account with Company as secondary owner should fail. Expected IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
        assertEquals("Account count should remain 2 after failed creation.", 2, accountManager.findAccountsByIndividualId(indivIdMain).size());
    }

    @Test
    public void testCompanyScenario_AccountManagement() throws Exception { // Added throws Exception
        // 1. Register users
        userManager.register("Company", "mainComp", "compPass", "Main Company Ltd.", "300000001");
        companyIdMain = userManager.login("mainComp", "compPass").getId();

        userManager.register("Individual", "otherIndiv", "indPass", "Other Individual", "400000001");
        // String otherIndivId = userManager.login("otherIndiv", "indPass").getId(); // Not used directly in this test

        // 2. Login "mainComp"
        User loggedInUser = userManager.login("mainComp", "compPass");
        assertNotNull(loggedInUser);
        assertEquals(companyIdMain, loggedInUser.getId());

        // 3. mainComp fails to create Personal Account
        try {
            accountManager.createPersonalAccount(companyIdMain, "US", 0.01, new ArrayList<String>());
            fail("Company user should not be able to create a personal account. Expected IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        // 4. mainComp creates Business Account 1
        accountManager.createBusinessAccount(companyIdMain, "GB", 0.005);
        BusinessAccount ba1 = accountManager.findAccountByBusinessId(companyIdMain);
        assertNotNull("Company should have one business account.", ba1);
        assertEquals(companyIdMain, ba1.getOwnerId());
        assertEquals(0.005, ba1.getInterestRate(), 0.001);
        assertEquals(0.0, ba1.getBalance(), 0.001);
        assertTrue(ba1.getIBAN().startsWith("GB200"));

        // 5. mainComp fails to create a second Business Account
        try {
            accountManager.createBusinessAccount(companyIdMain, "CA", 0.007);
            fail("Company should not be able to create more than one business account. Expected IllegalStateException.");
        } catch (IllegalStateException e) {
            // Expected exception
        }
        
        BusinessAccount baAfterAttempt = accountManager.findAccountByBusinessId(companyIdMain);
        assertNotNull(baAfterAttempt);
        assertEquals("No new account should have been created.", ba1.getIBAN(), baAfterAttempt.getIBAN());
        // Further check if only one account exists for the company.
        // This can be tricky without a method like getAllAccountsForOwner.
        // We assume findAccountByBusinessId returns the *only* one if it exists.
        // A more direct way would be to count all BusinessAccount objects in accountManager's list
        // that belong to this companyIdMain.
        // int businessAccountCount = 0;
        // Accessing bankAccountList directly is not possible from outside AccountManager.
        // This part of the assertion relies on the behavior of findAccountByBusinessId
        // and the previous successful creation.
        // If a method like `getAllAccounts()` existed in AccountManager, we could do:
        // List<BankAccount> allAccounts = accountManager.getAllAccounts();
        // for (BankAccount acc : allAccounts) {
        // if (acc instanceof BusinessAccount && acc.getOwnerId().equals(companyIdMain)) { // Ensure String comparison
        // businessAccountCount++;
        // }
        // }
        // assertEquals("Company should only have one business account.", 1, businessAccountCount);
        // For now, the check that baAfterAttempt is the same as ba1 is a good indicator.
    }
}