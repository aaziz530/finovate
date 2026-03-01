package org.esprit.finovate.dao;

import org.esprit.finovate.entities.User;
import org.esprit.finovate.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.out.println("UserDAO findAll: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setFirstName(rs.getString("firstname"));
        u.setLastName(rs.getString("lastname"));
        u.setRole(rs.getString("role"));
        u.setPoints(rs.getInt("points"));
        u.setSolde(rs.getFloat("solde"));
        u.setCreatedAt(rs.getTimestamp("createdAt"));
        long nc = rs.getLong("numeroCarte");
        u.setNumeroCarte(rs.wasNull() ? null : nc);
        u.setBirthdate(rs.getDate("birthdate"));
        return u;
    }
}
