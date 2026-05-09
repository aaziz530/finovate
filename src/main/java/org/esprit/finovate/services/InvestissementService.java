package org.esprit.finovate.services;

import org.esprit.finovate.entities.Investissement;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.utils.MyDataBase;
import org.esprit.finovate.utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC for {@code investissement}: PK {@code id}, FK {@code user_id} (not investor_id).
 */
public class InvestissementService {

    private final Connection connection;
    private final ProjectService projectService;

    private static final int PENDING_DAYS_LIMIT = 7;

    public InvestissementService() {
        this.connection = MyDataBase.getInstance().getConnection();
        this.projectService = new ProjectService();
    }

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

        double maxAllowed = getMaxInvestableAmount(inv.getProject_id(), Session.currentUser.getId());
        if (inv.getAmount() > maxAllowed) {
            throw new IllegalStateException(String.format("Maximum investable amount: %.2f TND.", maxAllowed));
        }

        String sql = "INSERT INTO investissement (project_id, user_id, amount, investment_date, status, revenue_percentage) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, Session.currentUser.getId());
            ps.setDouble(3, inv.getAmount());
            ps.setTimestamp(4, inv.getInvestment_date() != null
                    ? new Timestamp(inv.getInvestment_date().getTime())
                    : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, "PENDING");
            ps.setObject(6, inv.getRevenuePercentage() > 0 ? Double.valueOf(inv.getRevenuePercentage()) : null);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    inv.setInvestissement_id(id);
                    System.out.println("✅ Investment request sent with ID: " + id + " for project ID: "
                            + inv.getProject_id() + " (awaiting owner approval)");
                    return id;
                }
            }
        }
        return null;
    }

    public Long addInvestissementAsAdmin(Investissement inv) throws SQLException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            throw new IllegalStateException("Admin only.");
        }
        Project project = projectService.getProjectById(inv.getProject_id());
        if (project == null) {
            throw new IllegalStateException("Project not found.");
        }
        String status = inv.getStatus() != null ? inv.getStatus() : "PENDING";

        String sql = "INSERT INTO investissement (project_id, user_id, amount, investment_date, status, revenue_percentage) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, inv.getInvestor_id());
            ps.setDouble(3, inv.getAmount());
            ps.setTimestamp(4, inv.getInvestment_date() != null ? new Timestamp(inv.getInvestment_date().getTime())
                    : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, status);
            ps.setObject(6, inv.getRevenuePercentage() > 0 ? Double.valueOf(inv.getRevenuePercentage()) : null);
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

    public void updateInvestissementAsAdmin(Investissement inv) throws SQLException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            throw new IllegalStateException("Admin only.");
        }
        Investissement old = getInvestissementById(inv.getInvestissement_id());
        if (old == null)
            throw new IllegalStateException("Investment not found.");

        String newStatus = inv.getStatus() != null ? inv.getStatus() : old.getStatus();
        double newAmount = inv.getAmount() >= 0 ? inv.getAmount() : old.getAmount();

        String sql = "UPDATE investissement SET project_id=?, user_id=?, amount=?, status=?, revenue_percentage=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, inv.getProject_id());
            ps.setLong(2, inv.getInvestor_id());
            ps.setDouble(3, newAmount);
            ps.setString(4, newStatus);
            ps.setObject(5, inv.getRevenuePercentage() > 0 ? Double.valueOf(inv.getRevenuePercentage()) : null);
            ps.setLong(6, inv.getInvestissement_id());
            ps.executeUpdate();
        }

        if ("CONFIRMED".equals(old.getStatus())) {
            projectService.addToCurrentAmount(old.getProject_id(), -old.getAmount());
        }
        if ("CONFIRMED".equals(newStatus)) {
            projectService.addToCurrentAmount(inv.getProject_id(), newAmount);
        }
    }

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

    public Investissement getInvestissementById(Long investissementId) throws SQLException {
        String sql = "SELECT * FROM investissement WHERE id = ?";
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

    public boolean hasInvestments(Long projectId) throws SQLException {
        String sql = "SELECT 1 FROM investissement WHERE project_id = ? AND status = 'CONFIRMED' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Investissement> getPendingInvestmentsForOwner(Long ownerId) throws SQLException {
        autoDeclineExpiredPending();
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT i.* FROM investissement i "
                + "JOIN project p ON i.project_id = p.id "
                + "WHERE p.owner_id = ? AND i.status = 'PENDING' ORDER BY i.investment_date DESC";
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

    private void autoDeclineExpiredPending() {
        try {
            String sql = "UPDATE investissement SET status = 'DECLINED' WHERE status = 'PENDING' AND investment_date < DATE_SUB(NOW(), INTERVAL ? DAY)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, PENDING_DAYS_LIMIT);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    public double getTotalInvestedByUserForProject(Long projectId, Long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM investissement WHERE project_id = ? AND user_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public double getMaxInvestableAmount(Long projectId, Long userId) throws SQLException {
        Project p = projectService.getProjectById(projectId);
        if (p == null)
            return 0;
        double remaining = p.getGoal_amount() - p.getCurrent_amount();
        return Math.max(0, remaining);
    }

    public int getInvestorCount(Long projectId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM investissement WHERE project_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void acceptInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null)
            throw new IllegalStateException("Investment not found.");
        if (!"PENDING".equals(inv.getStatus())) {
            throw new IllegalStateException("Only pending investments can be accepted.");
        }

        String sql = "UPDATE investissement SET status = 'CONFIRMED' WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            ps.executeUpdate();
        }
        projectService.addToCurrentAmount(inv.getProject_id(), inv.getAmount());
        System.out.println("✅ Investment accepted for project " + inv.getProject_id());

        Project proj = projectService.getProjectById(inv.getProject_id());
        if (proj != null && proj.getCurrent_amount() >= proj.getGoal_amount()) {
            String updateGoal = "UPDATE project SET status = 'FUNDED', funding_completed_at = ? WHERE id = ? AND funding_completed_at IS NULL";
            try (PreparedStatement ugs = connection.prepareStatement(updateGoal)) {
                ugs.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ugs.setLong(2, proj.getProject_id());
                ugs.executeUpdate();
            }
        }
    }

    public void declineInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null)
            throw new IllegalStateException("Investment not found.");
        if (!"PENDING".equals(inv.getStatus())) {
            throw new IllegalStateException("Only pending investments can be declined.");
        }

        String sql = "UPDATE investissement SET status = 'DECLINED' WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            ps.executeUpdate();
        }
        System.out.println("Investment declined.");
    }

    public List<Investissement> getInvestissementsByInvestorId(Long investorId) throws SQLException {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement WHERE user_id = ? ORDER BY investment_date DESC";
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

    public void updateInvestissement(Investissement inv) throws SQLException {
        String sql = "UPDATE investissement SET status=? WHERE id=?";
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

    public void deleteInvestissement(Long investissementId) throws SQLException {
        Investissement inv = getInvestissementById(investissementId);
        if (inv == null) {
            System.out.println("⚠️ No investment found with ID: " + investissementId);
            return;
        }

        String sql = "DELETE FROM investissement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investissementId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                if ("CONFIRMED".equals(inv.getStatus())) {
                    String updateProject = "UPDATE project SET current_amount = current_amount - ? WHERE id = ?";
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
        inv.setInvestissement_id(rs.getLong("id"));
        inv.setProject_id(rs.getLong("project_id"));
        inv.setInvestor_id(rs.getLong("user_id"));
        inv.setAmount(rs.getDouble("amount"));
        Timestamp invDate = rs.getTimestamp("investment_date");
        inv.setInvestment_date(invDate != null ? new java.util.Date(invDate.getTime()) : null);
        inv.setStatus(rs.getString("status"));
        double rp = rs.getDouble("revenue_percentage");
        inv.setRevenuePercentage(rs.wasNull() ? 0 : rp);
        return inv;
    }
}
