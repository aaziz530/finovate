package org.esprit.finovate;

import org.esprit.finovate.models.Project;
import org.esprit.finovate.services.ProjectService;
import org.esprit.finovate.utils.Session;
import org.esprit.finovate.utils.StubLoggedInUser;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProjectService}.
 * Requires user ID 1 to exist in database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectServiceTest {

    static ProjectService projectService;
    static StubLoggedInUser testUser;
    private Long projectId = null;

    @BeforeAll
    static void setup() {
        projectService = new ProjectService();
        testUser = new StubLoggedInUser(1L); // Requires user 1 in DB
    }

    @BeforeEach
    void setSession() {
        Session.currentUser = testUser;
    }

    @AfterEach
    void cleanup() throws SQLException {
        if (projectId != null) {
            projectService.deleteProject(projectId);
            projectId = null;
        }
    }

    @Test
    @Order(1)
    void testAddProject() throws SQLException {
        Project p = new Project();
        p.setTitle("Test Project " + System.currentTimeMillis());
        p.setDescription("Description for test project");
        p.setGoal_amount(5000.0);
        p.setCurrent_amount(0);
        p.setCreated_at(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        p.setDeadline(cal.getTime());
        p.setStatus("OPEN");

        Long id = projectService.addProject(p);
        assertNotNull(id, "addProject should return generated ID");
        projectId = id;
        assertEquals(id, p.getProject_id());

        Project found = projectService.getProjectById(id);
        assertNotNull(found);
        assertEquals(p.getTitle(), found.getTitle());
        assertEquals(p.getDescription(), found.getDescription());
        assertEquals(p.getGoal_amount(), found.getGoal_amount(), 0.01);
        assertEquals(testUser.getId(), found.getOwner_id());
    }

    @Test
    @Order(2)
    void testGetProjectById() throws SQLException {
        Project p = createTestProject();
        Long id = projectService.addProject(p);
        projectId = id;

        Project found = projectService.getProjectById(id);
        assertNotNull(found);
        assertEquals(id, found.getProject_id());
        assertEquals(p.getTitle(), found.getTitle());

        assertNull(projectService.getProjectById(999999L));
    }

    @Test
    @Order(3)
    void testUpdateProject() throws SQLException {
        Project p = createTestProject();
        Long id = projectService.addProject(p);
        projectId = id;

        p.setProject_id(id);
        p.setTitle("Updated Title");
        p.setDescription("Updated description");
        p.setGoal_amount(10000.0);
        projectService.updateProject(p);

        Project found = projectService.getProjectById(id);
        assertNotNull(found);
        assertEquals("Updated Title", found.getTitle());
        assertEquals("Updated description", found.getDescription());
        assertEquals(10000.0, found.getGoal_amount(), 0.01);
    }

    @Test
    @Order(4)
    void testDeleteProject() throws SQLException {
        Project p = createTestProject();
        Long id = projectService.addProject(p);
        projectId = id;

        assertNotNull(projectService.getProjectById(id));
        projectService.deleteProject(id);
        projectId = null;

        assertNull(projectService.getProjectById(id));
    }

    @Test
    @Order(5)
    void testGetProjectsByOwnerId() throws SQLException {
        Project p = createTestProject();
        Long id = projectService.addProject(p);
        projectId = id;

        List<Project> byOwner = projectService.getProjectsByOwnerId(testUser.getId());
        assertFalse(byOwner.isEmpty());
        assertTrue(byOwner.stream().anyMatch(pr -> pr.getProject_id().equals(id)));
    }

    @Test
    @Order(6)
    void testAddProjectWithoutUserThrows() {
        Session.currentUser = null;
        Project p = createTestProject();
        assertThrows(IllegalStateException.class, () -> projectService.addProject(p),
                "Should throw when no user is logged in");
    }

    private Project createTestProject() {
        Project p = new Project();
        p.setTitle("Test " + System.currentTimeMillis());
        p.setDescription("Test description");
        p.setGoal_amount(1000.0);
        p.setCurrent_amount(0);
        p.setCreated_at(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        p.setDeadline(cal.getTime());
        p.setStatus("OPEN");
        return p;
    }
}
