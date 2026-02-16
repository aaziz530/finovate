package org.esprit.finovate.services;

import org.esprit.finovate.entities.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    static UserService userService;
    private Long createdUserId;

    @BeforeAll
    public static void setup() {
        userService = new UserService();
    }

    @AfterEach
    public void cleanUp() {
        if (createdUserId != null) {
            try {
                userService.deleteUser(createdUserId);
                System.out.println("[DEBUG_LOG] Cleanup: Deleted User with ID: " + createdUserId);
            } catch (SQLException e) {
                System.out.println("[DEBUG_LOG] Cleanup warning: " + e.getMessage());
            }
            createdUserId = null;
        }
    }

    @Test
    @Order(1)
    public void testRegisterUser() {
        String email = "test_create_" + System.currentTimeMillis() + "@finovate.com";
        try {
            User newUser = userService.register(email, "password123", "Test", "User", new Date(),
                    "12345678901234567890");
            assertNotNull(newUser, "Registered user returned null");
            assertNotNull(newUser.getId(), "Registered user ID is null");

            this.createdUserId = newUser.getId();
            System.out.println("[DEBUG_LOG] Created User with ID: " + createdUserId);

            User fetched = userService.getUserById(createdUserId);
            assertNotNull(fetched, "Could not fetch created user");
            assertEquals(email, fetched.getEmail(), "Email mismatch");

        } catch (SQLException e) {
            fail("Exception in testRegisterUser: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testUpdateUser() {
        String email = "test_update_" + System.currentTimeMillis() + "@finovate.com";
        try {
            // 1. Create a user to update
            User newUser = userService.register(email, "password123", "Original", "Name", new Date(),
                    "12345678901234567891");
            assertNotNull(newUser.getId());
            this.createdUserId = newUser.getId();

            // 2. Modify local object
            newUser.setFirstName("UpdatedFirst");
            newUser.setLastName("UpdatedLast");

            // 3. Perform update
            userService.updateUser(newUser);
            System.out.println("[DEBUG_LOG] Updated User ID " + createdUserId);

            // 4. Verify update
            User updatedUser = userService.getUserById(createdUserId);
            assertNotNull(updatedUser);
            assertEquals("UpdatedFirst", updatedUser.getFirstName());
            assertEquals("UpdatedLast", updatedUser.getLastName());

        } catch (SQLException e) {
            fail("Exception in testUpdateUser: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    public void testGetAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            assertNotNull(users);
            // We can't strictly assert notEmpty because DB might be empty,
            // but usually it won't be if we just ran tests (though they clean up).
            // Let's just create one temporarily to ensure list is not empty, or just check
            // notNull.

            // To be safer, let's create a temp user, assert list has it, them remove it
            // (via cleanup)
            String email = "test_list_" + System.currentTimeMillis() + "@finovate.com";
            User tempUser = userService.register(email, "pass", "List", "Test", new Date(), "12345678901234567892");
            this.createdUserId = tempUser.getId();

            users = userService.getAllUsers();
            assertFalse(users.isEmpty(), "User list should not be empty after adding a user");

            boolean found = users.stream().anyMatch(u -> u.getId().equals(tempUser.getId()));
            assertTrue(found, "Created user should be in the list");

        } catch (SQLException e) {
            fail("Exception in testGetAllUsers: " + e.getMessage());
        }
    }
}
