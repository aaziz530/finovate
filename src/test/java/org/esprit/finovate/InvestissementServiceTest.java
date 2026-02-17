package org.esprit.finovate;

import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.services.InvestissementService;
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
 * Unit tests for {@link InvestissementService}.
 * Requires user IDs 1 and 2 in database (1=owner, 2=investor).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InvestissementServiceTest {

    static InvestissementService investissementService;
    static ProjectService projectService;
    static StubLoggedInUser ownerUser;
    static StubLoggedInUser investorUser;
    private Long projectId = null;
    private Long investissementId = null;

    @BeforeAll
    static void setup() {
        investissementService = new InvestissementService();
        projectService = new ProjectService();
        ownerUser = new StubLoggedInUser(1L);   // Requires user 1 in DB
        investorUser = new StubLoggedInUser(2L); // Requires user 2 in DB
    }

    @AfterEach
    void cleanup() throws SQLException {
        if (investissementId != null) {
            investissementService.deleteInvestissement(investissementId);
            investissementId = null;
        }
        if (projectId != null) {
            projectService.deleteProject(projectId);
            projectId = null;
        }
    }

    @Test
    @Order(1)
    void testAddInvestissement() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = investorUser;
        Investissement inv = new Investissement(projId, null, 100.0);
        Long invId = investissementService.addInvestissement(inv);
        investissementId = invId;

        assertNotNull(invId);

        Investissement found = investissementService.getInvestissementById(invId);
        assertNotNull(found);
        assertEquals(projId, found.getProject_id());
        assertEquals(investorUser.getId(), found.getInvestor_id());
        assertEquals(100.0, found.getAmount(), 0.01);
        assertEquals("PENDING", found.getStatus());
    }

    @Test
    @Order(2)
    void testCannotInvestInOwnProject() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Investissement inv = new Investissement(projId, null, 50.0);
        assertThrows(IllegalStateException.class, () -> investissementService.addInvestissement(inv),
                "Should throw when trying to invest in own project");
    }

    @Test
    @Order(3)
    void testGetPendingInvestmentsForOwner() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = investorUser;
        Investissement inv = new Investissement(projId, null, 200.0);
        Long invId = investissementService.addInvestissement(inv);
        investissementId = invId;

        List<Investissement> pending = investissementService.getPendingInvestmentsForOwner(ownerUser.getId());
        assertFalse(pending.isEmpty());
        assertTrue(pending.stream().anyMatch(i -> i.getInvestissement_id().equals(invId)));
    }

    @Test
    @Order(4)
    void testAcceptInvestissement() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = investorUser;
        Investissement inv = new Investissement(projId, null, 150.0);
        Long invId = investissementService.addInvestissement(inv);
        investissementId = invId;

        investissementService.acceptInvestissement(invId);

        Investissement accepted = investissementService.getInvestissementById(invId);
        assertEquals("CONFIRMED", accepted.getStatus());

        Project updatedProject = projectService.getProjectById(projId);
        assertEquals(150.0, updatedProject.getCurrent_amount(), 0.01);
    }

    @Test
    @Order(5)
    void testDeclineInvestissement() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = investorUser;
        Investissement inv = new Investissement(projId, null, 75.0);
        Long invId = investissementService.addInvestissement(inv);
        investissementId = invId;

        investissementService.declineInvestissement(invId);

        Investissement declined = investissementService.getInvestissementById(invId);
        assertEquals("DECLINED", declined.getStatus());

        Project project = projectService.getProjectById(projId);
        assertEquals(0.0, project.getCurrent_amount(), 0.01);
    }

    @Test
    @Order(6)
    void testGetInvestissementsByInvestorId() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = investorUser;
        Investissement inv = new Investissement(projId, null, 300.0);
        Long invId = investissementService.addInvestissement(inv);
        investissementId = invId;

        List<Investissement> byInvestor = investissementService.getInvestissementsByInvestorId(investorUser.getId());
        assertFalse(byInvestor.isEmpty());
        assertTrue(byInvestor.stream().anyMatch(i -> i.getInvestissement_id().equals(invId)));
    }

    @Test
    @Order(7)
    void testAddInvestissementWithoutUserThrows() throws SQLException {
        Session.currentUser = ownerUser;
        Project p = createTestProject();
        Long projId = projectService.addProject(p);
        projectId = projId;

        Session.currentUser = null;
        Investissement inv = new Investissement(projId, null, 100.0);
        assertThrows(IllegalStateException.class, () -> investissementService.addInvestissement(inv),
                "Should throw when no user is logged in");
    }

    private Project createTestProject() {
        Project p = new Project();
        p.setTitle("Test Project " + System.currentTimeMillis());
        p.setDescription("Test description");
        p.setGoal_amount(5000.0);
        p.setCurrent_amount(0);
        p.setCreated_at(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        p.setDeadline(cal.getTime());
        p.setStatus("OPEN");
        return p;
    }
}
