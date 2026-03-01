package org.esprit.finovate.dao;

import org.esprit.finovate.database.DatabaseConnection;
import org.esprit.finovate.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean create(Message m) {
        String sql = "INSERT INTO message (idTicket, content, senderRole) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, m.getIdTicket());
            ps.setString(2, m.getContent());
            ps.setString(3, m.getSenderRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur CREATE MESSAGE: " + e.getMessage());
            return false;
        }
    }

    public List<Message> findAll() {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message ORDER BY sentAt";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur FIND ALL MESSAGES: " + e.getMessage());
        }
        return list;
    }

    public List<Message> findByTicketId(Long idTicket) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE idTicket = ? ORDER BY sentAt";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTicket);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Erreur FIND BY TICKET: " + e.getMessage());
        }
        return list;
    }

    public boolean update(Message m) {
        String sql = "UPDATE message SET content=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getContent());
            ps.setLong(2, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur UPDATE MESSAGE: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM message WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur DELETE MESSAGE: " + e.getMessage());
            return false;
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getLong("id"));
        m.setIdTicket(rs.getLong("idTicket"));
        m.setContent(rs.getString("content"));
        m.setSentAt(rs.getTimestamp("sentAt").toLocalDateTime());
        m.setSenderRole(rs.getString("senderRole"));
        return m;
    }
}