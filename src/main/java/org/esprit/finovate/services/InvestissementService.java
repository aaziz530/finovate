package org.esprit.finovate.services;

import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.MyDataBase;
import org.esprit.finovate.utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestissementService {

    private final Connection connection;
    private final ProjectService projectService;

    public InvestissementService() {
        this.connection = MyDataBase.getInstance().getConnection();
        this.projectService = new ProjectService();
    }

    /**
     * Create - Add an investment (investor = current logged user).
     * @return Generated investissement_id, or null if DB does not return keys.
     */
    public Long addInvestissement(Investissement inv) throws SQLException {
        if (Session.currentUser == null) {
            throw new IllegalStateException("❌ No user logged in!");
        }

        Project project = projectService.getProjectById(inv.getProject_id());
        if (project == null) {
            throw new IllegalStateException("❌ Project not found with ID: " + inv.getProject_id());
        }
        if (project.getOwner_id() != null && project.getOwner_id().equals(Session.currentUser.getId())) {
            throw new IllegalStateException("You cannot invest in your own project.");
        }

        String sql = "INSERT INTO investissement (project_id, investor_id, amount, investment_date, status) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, Session.currentUser.getId());
            ps.setDouble(3, inv.getAmount());
            ps.setTimestamp(4, inv.getInvestment_date() != null
                    ? new Timestamp(inv.getInvestment_date().getTime())
                    : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, "PENDING");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    inv.setInvestissement_id(id);
                    System.out.println("✅ Investment request sent with ID: " + id + " for project ID: " + inv.getProject_id() + " (awaiting owner approval)");
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Admin: Add investment with any project_id, investor_id, amount, status.
     * If status is CONFIRMED, adds amount to project.
     */
    public Long addInvestissementAsAdmin(Investissement inv) throws SQLException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            throw new IllegalStateException("Admin only.");
        }
        Project project = projectService.getProjectById(inv.getProject_id());
        if (project == null) {
            throw new IllegalStateException("Project not found.");
        }
        String status = inv.getStatus() != null ? inv.getStatus() : "PENDING";

        String sql = "INSERT INTO investissement (project_id, investor_id, amount, investment_date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, inv.getInvestor_id());
            ps.setDouble(3, inv.getAmount());
            ps.setTimestamp(4, inv.getInvestment_date() != null ? new Timestamp(inv.getInvestment_date().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    inv.setInvestissement_id(id);
                    if ("CONFIRMED".equals(status)) {
                        projectService.addToCurrentAmount(inv.getProject_id(), inv.getAmount());
                    }
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Admin: Update investment (amount, status). Adjusts project amount if needed.
     */
    public void updateInvestissementAsAdmin(Investissement inv) throws SQLException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            throw new IllegalStateException("Admin only.");
        }
        Investissement old = getInvestissementById(inv.getInvestissement_id());
        if (old == null) throw new IllegalStateException("Investment not found.");

        String newStatus = inv.getStatus() != null ? inv.getStatus() : old.getStatus();
        double newAmount = inv.getAmount() >= 0 ? inv.getAmount() : old.getAmount();

        String sql = "UPDATE investissement SET project_id=?, investor_id=?, amount=?, status=? WHERE investissement_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, inv.getInvestor_id());
            ps.setDouble(3, newAmount);
            ps.setString(4, newStatus);
            ps.setLong(5, inv.getInvestissement_id());
            ps.executeUpdate();
        }

        if ("CONFIRMED".equals(old.getStatus())) {
            projectService.addToCurrentAmount(old.getProject_id(), -old.getAmount());
        }
        if ("CONFIRMED".equals(newStatus)) {
            projectService.addToCurrentAmount(inv.getProject_id(), newAmount);
        }
    }

    /**
     * Read - Get all investments
     */
    public List<Investissement> getAllInvestissements() throws SQLException {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement ORDER BY investment_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToInvestissement(rs));
            }
        }
        return list;
    }

    /**
     * Read - Get investment by ID
     */
    public Investissement getInvestissementById(Long investissementId) throws SQLException {
        String sql = "SELECT * FROM investissement WHERE investissement_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvestissement(rs);
                }
            }
        }
        return null;
    }

    /**
     * Read - Get all investments for a project
     */
    public List<Investissement> getInvestissementsByProjectId(Long projectId) throws SQLException {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement WHERE project_id = ? ORDER BY investment_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvestissement(rs));
                }
            }
        }
        return list;
    }

    /**
     * Check if a project has at least one CONFIRMED investment
     */
    public boolean hasInvestments(Long projectId) throws SQLException {
        String sql = "SELECT 1 FROM investissement WHERE project_id = ? AND status = 'CONFIRMED' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Get pending investments for projects owned by a user
     */
    public List<Investissement> getPendingInvestmentsForOwner(Long ownerId) throws SQLException {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT i.* FROM investissement i " +
                "JOIN project p ON i.project_id = p.project_id " +
                "WHERE p.owner_id = ? AND i.status = 'PENDING' ORDER BY i.investment_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvestissement(rs));
                }
            }
        }
        return list;
    }

    /**
     * Accept an investment: set status CONFIRMED and update project amount
     */
    public void acceptInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null) throw new IllegalStateException("Investment not found.");
        if (!"PENDING".equals(inv.getStatus())) {
            throw new IllegalStateException("Only pending investments can be accepted.");
        }

        String sql = "UPDATE investissement SET status = 'CONFIRMED' WHERE investissement_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            ps.executeUpdate();
        }
        projectService.addToCurrentAmount(inv.getProject_id(), inv.getAmount());
        System.out.println("✅ Investment accepted for project " + inv.getProject_id());
    }

    /**
     * Decline an investment: set status DECLINED (no amount added to project)
     */
    public void declineInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null) throw new IllegalStateException("Investment not found.");
        if (!"PENDING".equals(inv.getStatus())) {
            throw new IllegalStateException("Only pending investments can be declined.");
        }

        String sql = "UPDATE investissement SET status = 'DECLINED' WHERE investissement_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            ps.executeUpdate();
        }
        System.out.println("Investment declined.");
    }

    /**
     * Read - Get all investments by a user (investor)
     */
    public List<Investissement> getInvestissementsByInvestorId(Long investorId) throws SQLException {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement WHERE investor_id = ? ORDER BY investment_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvestissement(rs));
                }
            }
        }
        return list;
    }

    /**
     * Update - Modify investment status (or amount - but updating amount would require adjusting project.current_amount)
     */
    public void updateInvestissement(Investissement inv) throws SQLException {
        String sql = "UPDATE investissement SET status=? WHERE investissement_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, inv.getStatus());
            ps.setLong(2, inv.getInvestissement_id());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Investment updated successfully");
            } else {
                System.out.println("⚠️ No investment found with ID: " + inv.getInvestissement_id());
            }
        }
    }

    /**
     * Delete - Remove an investment by ID
     * For PENDING: just delete. For CONFIRMED: subtract amount from project.
     */
    public void deleteInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null) {
            System.out.println("⚠️ No investment found with ID: " + investissementId);
            return;
        }

        String sql = "DELETE FROM investissement WHERE investissement_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                if ("CONFIRMED".equals(inv.getStatus())) {
                    String updateProject = "UPDATE project SET current_amount = current_amount - ? WHERE project_id = ?";
                    try (PreparedStatement ups = connection.prepareStatement(updateProject)) {
                        ups.setDouble(1, inv.getAmount());
                        ups.setLong(2, inv.getProject_id());
                        ups.executeUpdate();
                    }
                }
                System.out.println("✅ Investment deleted successfully");
            }
        }
    }

    private Investissement mapResultSetToInvestissement(ResultSet rs) throws SQLException {
        Investissement inv = new Investissement();
        inv.setInvestissement_id(rs.getLong("investissement_id"));
        inv.setProject_id(rs.getLong("project_id"));
        inv.setInvestor_id(rs.getLong("investor_id"));
        inv.setAmount(rs.getDouble("amount"));
        Timestamp invDate = rs.getTimestamp("investment_date");
        inv.setInvestment_date(invDate != null ? new java.util.Date(invDate.getTime()) : null);
        inv.setStatus(rs.getString("status"));
        return inv;
    }
}
