package org.esprit.finovate.dao;

import org.esprit.finovate.database.DatabaseConnection;
import org.esprit.finovate.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    public boolean create(Ticket r) {
        String sql = "INSERT INTO ticket (type, description, priorite, statut) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getType());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getPriorite());
            ps.setString(4, r.getStatut());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur CREATE: " + e.getMessage());
            return false;
        }
    }

    public List<Ticket> findAll() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM ticket ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Ticket r = new Ticket();
                r.setId(rs.getLong("id"));
                r.setType(rs.getString("type"));
                r.setDescription(rs.getString("description"));
                r.setPriorite(rs.getString("priorite"));
                r.setStatut(rs.getString("statut"));
                try {
                    r.setDateCreation(rs.getTimestamp("date_creation"));
                } catch (SQLException ignored) {
                }
                try {
                    r.setDateResolution(rs.getTimestamp("date_resolution"));
                } catch (SQLException ignored) {
                }
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur FIND ALL: " + e.getMessage());
        }
        return list;
    }

    public Ticket findById(Long id) {
        String sql = "SELECT * FROM ticket WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Ticket r = new Ticket();
                r.setId(rs.getLong("id"));
                r.setType(rs.getString("type"));
                r.setDescription(rs.getString("description"));
                r.setPriorite(rs.getString("priorite"));
                r.setStatut(rs.getString("statut"));
                try {
                    r.setDateCreation(rs.getTimestamp("date_creation"));
                } catch (SQLException ignored) {
                }
                try {
                    r.setDateResolution(rs.getTimestamp("date_resolution"));
                } catch (SQLException ignored) {
                }
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Erreur FIND BY ID: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Ticket r) {
        String sql = "UPDATE ticket SET type=?, description=?, priorite=?, statut=?, date_resolution=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getType());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getPriorite());
            ps.setString(4, r.getStatut());
            ps.setTimestamp(5, r.getDateResolution());
            ps.setLong(6, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur UPDATE: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM ticket WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur DELETE: " + e.getMessage());
            return false;
        }
    }
}