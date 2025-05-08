package test;

import managers.*;

import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import org.junit.Before; // JUnit 4
import org.junit.Test; // JUnit 4
import system.BankSystem;

import static org.junit.Assert.*; // JUnit 4

public class UserManagerTest {

    private BankSystem bankSystem;
    private UserManager userManager;
    private static int idCounterForTest = 10000000; // Used to predict IDs if they are sequential strings like "0", "1", ...

    private String generateExpectedId() {
        return String.valueOf(idCounterForTest++);
    }

    @Before // Changed from @BeforeEach
    public void setUp() { // Method must be public for JUnit 4 @Before
        // Initialize BankSystem, which in turn initializes UserManager
        bankSystem = new BankSystem();
        userManager = bankSystem.getUserManager();
        idCounterForTest = 10000000; // Reset for each test
    }

    // --- Registration Tests ---

    @Test
    // @DisplayName("Register Individual: Successful case") // Removed
    public void testRegisterIndividual_Success() {
        String expectedId = generateExpectedId();
        userManager.register("Individual", "johnDoe", "password123", "John Doe", "123456789");
        User user = userManager.login("johnDoe", "password123");
        assertNotNull("User should be registered and logged in.", user);
        assertTrue("User should be an instance of Individual.", user instanceof Individual);
        // Assuming User has getLegalName() - This needs to be added to your User class
        // If User class does not have getLegalName(), you might need to cast and then
        // call it from Admin, Individual, Company
        // For example, if Individual has getLegalName():
        assertEquals("John Doe", ((Individual) user).getLegalName());
        assertEquals("VAT number should match.", "123456789", ((Individual) user).getVAT());
        assertEquals("User ID should match expected string ID.", expectedId, user.getId());
    }

    @Test
    public void testRegisterCompany_Success() {
        String expectedId = generateExpectedId();
        userManager.register("Company", "companya", "securePass", "Company A Ltd.", "987654321");
        User user = userManager.login("companya", "securePass");
        assertNotNull(user);
        assertTrue(user instanceof Company);
        assertEquals("Company A Ltd.", ((Company) user).getLegalName());
        assertEquals("987654321", ((Company) user).getVAT());
        assertEquals(expectedId, user.getId());
    }

    @Test
    public void testRegisterAdmin_Success() {
        String expectedId = generateExpectedId();
        userManager.register("Admin", "adminUser", "adminPass", "Main Admin", null); // VAT is null for Admin
        User user = userManager.login("adminUser", "adminPass");
        assertNotNull(user);
        assertTrue(user instanceof Admin);
        assertEquals("Main Admin", ((Admin) user).getLegalName());
        assertEquals(expectedId, user.getId());
    }

    @Test(expected = RuntimeException.class) // JUnit 4 style for expected exception
    public void testRegister_UsernameTaken_CaseInsensitive_ThrowsException() {
        userManager.register("Individual", "testUser", "pass1", "Test User", "111222333");
        // Assuming UserManager.register or isUsernameTaken checks are case-insensitive
        userManager.register("Company", "TestUser", "pass2", "Another Test", "222333444");
        // If the second register call doesn't throw, this test will fail.
    }

    @Test(expected = RuntimeException.class)
    public void testRegister_InvalidUserType_ThrowsException() {
        userManager.register("SuperUser", "super", "pass", "Super Man", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_AdminWithVAT_ThrowsIllegalArgumentException() {
        // This test assumes UserManager.register validates that Admins cannot have a
        // VAT.
        userManager.register("Admin", "adminWithVAT", "pass", "Admin VAT Test", "123456789");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_Individual_VATNot9Digits_ThrowsIllegalArgumentException() {
        // Assumes UserManager.register validates VAT format (9 digits)
        userManager.register("Individual", "userShortVAT", "pass", "User Short VAT", "12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_Company_VATNonNumeric_ThrowsIllegalArgumentException() {
        // Assumes UserManager.register validates VAT format (numeric)
        userManager.register("Company", "compNonNumericVAT", "pass", "Company NonNumeric", "12345678A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_Individual_NullVAT_ThrowsIllegalArgumentException() {
        // Assumes VAT is mandatory for Individuals and Companies
        userManager.register("Individual", "userNullVAT", "pass", "User Null VAT", null);
    }

    // --- Login Tests ---

    @Test
    public void testLogin_Success_CaseInsensitiveUsername() {
        userManager.register("Individual", "LoginUser", "Password123", "Login User", "123123123");
        // Assuming UserManager.login handles usernames case-insensitively
        User user = userManager.login("loginuser", "Password123");
        assertNotNull("Login should succeed with case-insensitive username.", user);
        assertEquals("LoginUser", user.getUserName()); // Original username casing is preserved
    }

    @Test
    public void testLogin_IncorrectPassword_ReturnsNull() {
        userManager.register("Individual", "loginFailUser", "correctPass", "Login Fail", "321321321");
        User user = userManager.login("loginFailUser", "wrongPass");
        assertNull("Login with incorrect password should return null.", user);
    }

    @Test
    public void testLogin_UsernameNotFound_ReturnsNull() {
        User user = userManager.login("nonExistentUser", "anyPass");
        assertNull("Login with non-existent username should return null.", user);
    }

    // --- Find User By ID Tests ---

    @Test
    public void testFindUserById_UserExists() {
        String adminExpectedId = generateExpectedId();
        userManager.register("Admin", "findAdmin", "pass", "Find Admin", null);
        String indivExpectedId = generateExpectedId();
        userManager.register("Individual", "findIndiv", "pass", "Find Indiv", "456456456");

        User admin = userManager.findUserById(adminExpectedId);
        assertNotNull(admin);
        assertEquals("findAdmin", admin.getUserName());

        User individual = userManager.findUserById(indivExpectedId);
        assertNotNull(individual);
        assertEquals("findIndiv", individual.getUserName());
    }

    @Test
    public void testFindUserById_UserNotFound_ReturnsNull() {
        User user = userManager.findUserById("99"); // Assuming no user with ID "99" exists
        assertNull("Finding a non-existent user ID should return null.", user);
    }

    // --- Get User Type Tests ---

    @Test
    public void testGetUserType_Admin() {
        User u = userManager.register("Admin", "typeAdmin", "pass", "Type Admin", null);
        String userType = userManager.getUserType(u.getId());
        assertEquals("User type should be Admin.", "Admin", userType);
    }

    @Test
    public void testGetUserType_Individual() {
        User u = userManager.register("Individual", "typeIndiv", "pass", "Type Indiv", "789789789");
        String userType = userManager.getUserType(u.getId());
        assertEquals("User type should be Individual.", "Individual", userType);
    }

    @Test
    public void testGetUserType_Company() {
        User u = userManager.register("Company", "typeComp", "pass", "Type Comp", "101010101");
        String userType = userManager.getUserType(u.getId());
        assertEquals("User type should be Company.", "Company", userType);
    }

    @Test(expected = NullPointerException.class) // Or specific "UserNotFoundException" if that's what it throws
    public void testGetUserType_UserNotFound() {
        // Based on current UserManager.getUserType, this might throw
        // NullPointerException
        // if findUserById returns null and then a method is called on it.
        // If findUserById itself throws an exception for not found, or returns null
        // and getUserType handles null by throwing, this would be the test.
        userManager.getUserType("nonExistentId99");
    }

    // --- isUsernameTaken Tests ---
    // isUsernameTaken is private, so we test its behavior through the public
    // register method.
    @Test(expected = RuntimeException.class)
    public void testIsUsernameTaken_True_CaseInsensitive() {
        userManager.register("Individual", "UniqueUser", "pass", "Unique", "123450987");
        // Assumes register method internally uses a case-insensitive check for username
        // uniqueness
        userManager.register("Admin", "uniqueuser", "adminpass", "Admin Unique", null);
    }

    @Test
    public void testIsUsernameTaken_False() {
        // We test this by successfully registering a new user, implying the username
        // wasn't taken.
        userManager.register("Individual", "newUser", "pass", "New", "000000001");
        User user = userManager.login("newUser", "pass");
        assertNotNull("User should be registered as username is not taken.", user);
    }
}