package org.esprit.finovate.services;

import org.esprit.finovate.models.Project;
import org.esprit.finovate.utils.MyDataBase;
import org.esprit.finovate.utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectService {

    private final Connection connection;

    public ProjectService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Create - Add a new project (owner = current logged user).
     * @return Generated project_id, or null if DB does not return keys.
     */
    public Long addProject(Project p) throws SQLException {
        if (Session.currentUser == null) {
            throw new IllegalStateException("❌ No user logged in!");
        }

        String sql = "INSERT INTO project (title, description, goal_amount, current_amount, created_at, deadline, status, owner_id, image_path, latitude, longitude, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getGoal_amount());
            ps.setDouble(4, p.getCurrent_amount());
            ps.setTimestamp(5, p.getCreated_at() != null ? new Timestamp(p.getCreated_at().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(6, p.getDeadline() != null ? new Timestamp(p.getDeadline().getTime()) : null);
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "OPEN");
            ps.setLong(8, Session.currentUser.getId());
            ps.setString(9, p.getImagePath());
            ps.setObject(10, p.getLatitude());
            ps.setObject(11, p.getLongitude());
            ps.setString(12, p.getCategory());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    p.setProject_id(id);
                    System.out.println("✅ Project added with ID: " + id + " for user ID: " + Session.currentUser.getId());
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Admin: Add project with any owner_id (bypasses Session.currentUser as owner).
     */
    public Long addProjectAsAdmin(Project p) throws SQLException {
        if (Session.currentUser == null || !"ADMIN".equals(Session.currentUser.getRole())) {
            throw new IllegalStateException("Admin only.");
        }
        Long ownerId = p.getOwner_id() != null ? p.getOwner_id() : Session.currentUser.getId();

        String sql = "INSERT INTO project (title, description, goal_amount, current_amount, created_at, deadline, status, owner_id, image_path, latitude, longitude, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getGoal_amount());
            ps.setDouble(4, p.getCurrent_amount() >= 0 ? p.getCurrent_amount() : 0);
            ps.setTimestamp(5, p.getCreated_at() != null ? new Timestamp(p.getCreated_at().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(6, p.getDeadline() != null ? new Timestamp(p.getDeadline().getTime()) : null);
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "OPEN");
            ps.setLong(8, ownerId);
            ps.setString(9, p.getImagePath());
            ps.setObject(10, p.getLatitude());
            ps.setObject(11, p.getLongitude());
            ps.setString(12, p.getCategory());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    p.setProject_id(id);
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Read - Get all projects
     */
    public List<Project> getAllProjects() throws SQLException {
        applyAutoStatusForAll();
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToProject(rs));
            }
        }
        return list;
    }

    /**
     * Read - Get all projects owned by a user
     */
    public List<Project> getProjectsByOwnerId(Long ownerId) throws SQLException {
        applyAutoStatusForAll();
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE owner_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProject(rs));
                }
            }
        }
        return list;
    }

    /**
     * Read - Get project by ID
     */
    public Project getProjectById(Long projectId) throws SQLException {
        String sql = "SELECT * FROM project WHERE project_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update - Modify an existing project
     */
    public void updateProject(Project p) throws SQLException {
        String sql = "UPDATE project SET title=?, description=?, goal_amount=?, current_amount=?, deadline=?, status=?, image_path=?, latitude=?, longitude=?, category=? WHERE project_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getGoal_amount());
            ps.setDouble(4, p.getCurrent_amount());
            ps.setDate(5, p.getDeadline() != null ? new java.sql.Date(p.getDeadline().getTime()) : null);
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getImagePath());
            ps.setObject(8, p.getLatitude());
            ps.setObject(9, p.getLongitude());
            ps.setString(10, p.getCategory());
            ps.setLong(11, p.getProject_id());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                applyAutoStatus(p.getProject_id());
                System.out.println("✅ Project updated successfully");
            } else {
                System.out.println("⚠️ No project found with ID: " + p.getProject_id());
            }
        }
    }

    /**
     * Delete - Remove a project by ID
     */
    public void deleteProject(Long projectId) throws SQLException {
        String sql = "DELETE FROM project WHERE project_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Project deleted successfully");
            } else {
                System.out.println("⚠️ No project found with ID: " + projectId);
            }
        }
    }

    /**
     * Update project's current_amount (used when an investment is added)
     */
    public void addToCurrentAmount(Long projectId, double amount) throws SQLException {
        String sql = "UPDATE project SET current_amount = current_amount + ? WHERE project_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setLong(2, projectId);
            ps.executeUpdate();
        }
        recordAmountHistory(projectId, amount);
        applyAutoStatus(projectId);
    }

    private void recordAmountHistory(Long projectId, double deltaAmount) {
        try {
            Project p = getProjectById(projectId);
            if (p == null) return;
            double newTotal = p.getCurrent_amount();
            String sql = "INSERT INTO project_amount_history (project_id, amount) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, projectId);
                ps.setDouble(2, newTotal);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    public void applyAutoStatus(Long projectId) throws SQLException {
        Project p = getProjectById(projectId);
        if (p == null) return;
        String newStatus = null;
        if (p.getCurrent_amount() >= p.getGoal_amount()) newStatus = "FUNDED";
        else if (p.getDeadline() != null && p.getDeadline().before(new java.util.Date())) newStatus = "CLOSED";
        if (newStatus != null && !newStatus.equals(p.getStatus())) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE project SET status=? WHERE project_id=?")) {
                ps.setString(1, newStatus);
                ps.setLong(2, projectId);
                ps.executeUpdate();
            }
        }
    }

    private void applyAutoStatusForAll() {
        try {
            try (PreparedStatement ps1 = connection.prepareStatement("UPDATE project SET status='FUNDED' WHERE current_amount >= goal_amount AND status != 'FUNDED'")) {
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = connection.prepareStatement("UPDATE project SET status='CLOSED' WHERE deadline < NOW() AND status = 'OPEN'")) {
                ps2.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    public List<Project> getSimilarProjects(Long projectId, int limit) throws SQLException {
        Project ref = getProjectById(projectId);
        if (ref == null) return List.of();
        String cat = ref.getCategory();
        if (cat == null || cat.isBlank()) cat = ref.getTitle();
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE project_id != ? AND status = 'OPEN' AND (category = ? OR title LIKE ?) ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            ps.setString(2, cat);
            ps.setString(3, "%" + (ref.getTitle() != null ? ref.getTitle().split(" ")[0] : "") + "%");
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            String sql2 = "SELECT * FROM project WHERE project_id != ? AND status = 'OPEN' ORDER BY created_at DESC LIMIT " + limit;
            try (PreparedStatement ps = connection.prepareStatement(sql2)) {
                ps.setLong(1, projectId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToProject(rs));
                }
            }
        }
        return list;
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setProject_id(rs.getLong("project_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setGoal_amount(rs.getDouble("goal_amount"));
        p.setCurrent_amount(rs.getDouble("current_amount"));
        Timestamp created = rs.getTimestamp("created_at");
        p.setCreated_at(created != null ? new java.util.Date(created.getTime()) : null);
        Date deadline = rs.getDate("deadline");
        p.setDeadline(deadline != null ? new java.util.Date(deadline.getTime()) : null);
        p.setStatus(rs.getString("status"));
        p.setOwner_id(rs.getLong("owner_id"));
        try { p.setImagePath(rs.getString("image_path")); } catch (SQLException ignored) {}
        try { p.setLatitude(rs.getObject("latitude") != null ? rs.getDouble("latitude") : null); } catch (SQLException ignored) {}
        try { p.setLongitude(rs.getObject("longitude") != null ? rs.getDouble("longitude") : null); } catch (SQLException ignored) {}
        try { p.setCategory(rs.getString("category")); } catch (SQLException ignored) {}
        return p;
    }
}
