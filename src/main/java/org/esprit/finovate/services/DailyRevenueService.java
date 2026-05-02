package org.esprit.finovate.services;

import org.esprit.finovate.models.DailyRevenue;
import org.esprit.finovate.entities.Project;
import org.esprit.finovate.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyRevenueService {

    private final Connection connection;
    private final ProjectService projectService;

    public DailyRevenueService() {
        this.connection = MyDataBase.getInstance().getConnection();
        this.projectService = new ProjectService();
    }

    /**
     * Add revenue for a specific project on a specific date
     */
    public void addRevenue(Long projectId, java.util.Date date, double amount) throws SQLException {
        String sql = "INSERT INTO daily_revenue (project_id, revenue_date, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDouble(3, amount);
            ps.executeUpdate();
        }
    }

    /**
     * Get all revenues declared for a project
     */
    public List<DailyRevenue> getRevenuesByProject(Long projectId) throws SQLException {
        List<DailyRevenue> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_revenue WHERE project_id = ? ORDER BY revenue_date ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyRevenue dr = new DailyRevenue();
                    try {
                        dr.setRevenue_id(rs.getLong("revenue_id"));
                    } catch (SQLException ex) {
                        dr.setRevenue_id(rs.getLong("id"));
                    }
                    dr.setProject_id(rs.getLong("project_id"));
                    dr.setRevenue_date(rs.getDate("revenue_date"));
                    dr.setAmount(rs.getDouble("amount"));
                    list.add(dr);
                }
            }
        }
        return list;
    }

    /**
     * Magic Method: Get all days since project was funded where revenue was NOT
     * declared.
     */
    public List<LocalDate> getMissingRevenueDates(Long projectId) throws SQLException {
        List<LocalDate> missingDates = new ArrayList<>();
        Project project = projectService.getProjectById(projectId);

        // If project not funded yet, or doesn't have a completion date, no missing
        // revenues
        if (project == null || !"FUNDED".equals(project.getStatus()) || project.getFunding_completed_at() == null) {
            return missingDates;
        }

        LocalDate start = new java.sql.Date(project.getFunding_completed_at().getTime()).toLocalDate();
        LocalDate today = LocalDate.now();

        // Get declared dates from DB
        List<LocalDate> declaredDates = new ArrayList<>();
        String sql = "SELECT revenue_date FROM daily_revenue WHERE project_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("revenue_date");
                    if (sqlDate != null) {
                        declaredDates.add(sqlDate.toLocalDate());
                    }
                }
            }
        }

        // Loop through each day from start to today (exclusive, assuming you declare
        // YESTERDAY's revenue today, or inclusive if today's is expected)
        for (LocalDate date = start; date.isBefore(today); date = date.plusDays(1)) {
            if (!declaredDates.contains(date)) {
                missingDates.add(date);
            }
        }

        return missingDates;
    }

    /**
     * Calculate total earnings for an investor across all their investments based
     * on their requested revenuePercentage.
     */
    public double getInvestorEarnings(Long investorId) throws SQLException {
        String sql = "SELECT i.revenue_percentage, (SELECT COALESCE(SUM(amount), 0) FROM daily_revenue dr WHERE dr.project_id = i.project_id) as total_project_revenue "
                +
                "FROM investissement i " +
                "WHERE i.user_id = ? AND i.status = 'CONFIRMED' AND i.revenue_percentage > 0";

        double totalGained = 0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double percentage = rs.getDouble("revenue_percentage");
                    double projectRevenue = rs.getDouble("total_project_revenue");

                    double investorCut = projectRevenue * (percentage / 100.0);
                    totalGained += investorCut;
                }
            }
        }
        return totalGained;
    }
}
