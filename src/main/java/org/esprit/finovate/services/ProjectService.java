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

        String sql = "INSERT INTO project (title, description, goal_amount, current_amount, created_at, deadline, status, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getGoal_amount());
            ps.setDouble(4, p.getCurrent_amount());
            ps.setTimestamp(5, p.getCreated_at() != null ? new Timestamp(p.getCreated_at().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(6, p.getDeadline() != null ? new Timestamp(p.getDeadline().getTime()) : null);
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "OPEN");
            ps.setLong(8, Session.currentUser.getId());
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
     * Read - Get all projects
     */
    public List<Project> getAllProjects() throws SQLException {
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
        String sql = "UPDATE project SET title=?, description=?, goal_amount=?, current_amount=?, deadline=?, status=? WHERE project_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getGoal_amount());
            ps.setDouble(4, p.getCurrent_amount());
            ps.setDate(5, p.getDeadline() != null ? new java.sql.Date(p.getDeadline().getTime()) : null);
            ps.setString(6, p.getStatus());
            ps.setLong(7, p.getProject_id());
            int rows = ps.executeUpdate();
            if (rows > 0) {
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
        return p;
    }
}
