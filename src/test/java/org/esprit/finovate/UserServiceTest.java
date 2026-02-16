package org.esprit.finovate;

import org.esprit.finovate.models.User;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.DevAccount;
import org.esprit.finovate.utils.Session;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserService}.
 * Tests login (including dev account), register, and logout.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    static UserService userService;

    @BeforeAll
    static void setup() {
        userService = new UserService();
    }

    @BeforeEach
    void resetSession() {
        Session.currentUser = null;
    }

    @Test
    @Order(1)
    void testLoginWithDevAccount() throws SQLException {
        User loggedIn = userService.login(DevAccount.EMAIL, DevAccount.PASSWORD);
        assertNotNull(loggedIn, "Dev login should return a user");
        assertEquals(DevAccount.EMAIL, loggedIn.getEmail());
        assertEquals("Dev", loggedIn.getFirstName());
        assertEquals("ADMIN", loggedIn.getRole());
        assertSame(loggedIn, Session.currentUser, "Session should hold the logged-in user");
    }

    @Test
    @Order(2)
    void testLoginWithWrongCredentials() throws SQLException {
        User loggedIn = userService.login("wrong@test.com", "wrongpassword");
        assertNull(loggedIn, "Wrong credentials should return null");
        assertNull(Session.currentUser);
    }

    @Test
    @Order(3)
    void testLogout() throws SQLException {
        userService.login(DevAccount.EMAIL, DevAccount.PASSWORD);
        assertNotNull(Session.currentUser);
        userService.logout();
        assertNull(Session.currentUser);
    }

    @Test
    @Order(4)
    void testRegisterAndLogin() throws SQLException {
        // Use unique email to avoid "Email existe déjà" when re-running tests
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@finovate.test";
        String password = "Test123!";
        String firstName = "Test";
        String lastName = "User";
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JANUARY, 1);
        Date birthdate = cal.getTime();

        User registered = userService.register(uniqueEmail, password, firstName, lastName, birthdate);
        assertNotNull(registered, "Register should return the created user");
        assertTrue(registered.getId() != null && registered.getId() > 0, "User should have a generated ID");
        assertEquals(uniqueEmail, registered.getEmail());
        assertEquals(firstName, registered.getFirstName());
        assertEquals(lastName, registered.getLastName());

        // Verify we can login with the new user
        User loggedIn = userService.login(uniqueEmail, password);
        assertNotNull(loggedIn, "Should be able to login with newly registered credentials");
        assertEquals(registered.getId(), loggedIn.getId());
    }

    @Test
    @Order(5)
    void testRegisterDuplicateEmailThrows() throws SQLException {
        String uniqueEmail = "duplicate_" + System.currentTimeMillis() + "@finovate.test";
        userService.register(uniqueEmail, "Pass123!", "A", "B", new Date());

        assertThrows(IllegalStateException.class, () ->
                userService.register(uniqueEmail, "Pass456!", "C", "D", new Date()),
                "Registering with same email should throw IllegalStateException"
        );
    }
}
