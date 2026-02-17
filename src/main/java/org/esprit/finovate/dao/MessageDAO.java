package org.esprit.finovate.dao;

import org.esprit.finovate.database.DatabaseConnection;
import org.esprit.finovate.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean create(Message m) {
        String sql = "INSERT INTO message (idTicket, content) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, m.getIdTicket());
            ps.setString(2, m.getContent());

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

            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getLong("id"));
                m.setIdTicket(rs.getLong("idTicket"));
                m.setContent(rs.getString("content"));
                m.setSentAt(rs.getTimestamp("sentAt").toLocalDateTime());
                list.add(m);
            }

        } catch (SQLException e) {
            System.out.println("Erreur FIND ALL MESSAGES: " + e.getMessage());
        }
        return list;
    }

    public Message findById(Long id) {
        String sql = "SELECT * FROM message WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Message m = new Message();
                m.setId(rs.getLong("id"));
                m.setIdTicket(rs.getLong("idTicket"));
                m.setContent(rs.getString("content"));
                m.setSentAt(rs.getTimestamp("sentAt").toLocalDateTime());
                return m;
            }

        } catch (SQLException e) {
            System.out.println("Erreur FIND BY ID MESSAGE: " + e.getMessage());
        }
        return null;
    }

    public List<Message> findByTicketId(Long idTicket) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE idTicket = ? ORDER BY sentAt";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idTicket);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getLong("id"));
                m.setIdTicket(rs.getLong("idTicket"));
                m.setContent(rs.getString("content"));
                m.setSentAt(rs.getTimestamp("sentAt").toLocalDateTime());
                list.add(m);
            }

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
}
