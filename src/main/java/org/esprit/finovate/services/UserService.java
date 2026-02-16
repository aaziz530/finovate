package org.esprit.finovate.services;

import org.esprit.finovate.models.User;
import org.esprit.finovate.utils.MyDataBase;
import org.esprit.finovate.utils.DevAccount;
import org.esprit.finovate.utils.PasswordUtils;
import org.esprit.finovate.utils.Session;

import java.sql.*;
import java.util.Date;

public class UserService implements IUserService {

    private final Connection connection;

    public UserService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public User login(String email, String password) throws SQLException {
        if (DevAccount.EMAIL.equals(email) && DevAccount.PASSWORD.equals(password)) {
            User devUser = DevAccount.createUser();
            Session.currentUser = devUser;
            return devUser;
        }

        if (connection == null) {
            throw new SQLException("Connexion DB est null. Vérifie MyDataBase");
        }

        String hashedPassword = PasswordUtils.sha256(password);

        String sql = "SELECT * FROM `user` WHERE email=? AND password=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = mapResultSetToUser(rs);
                    Session.currentUser = u;
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public void logout() {
        Session.currentUser = null;
    }

    @Override
    public User register(String email, String password, String firstName, String lastName, Date birthdate) throws SQLException {
        User user = new User(email, password, firstName, lastName, birthdate);
        user.setPassword(PasswordUtils.sha256(password));

        if (connection == null) {
            throw new SQLException("Connexion DB est null. Vérifie MyDataBase");
        }

        if (emailExists(connection, user.getEmail())) {
            throw new IllegalStateException("Email existe déjà");
        }

        String sql = "INSERT INTO `user` (email, password, firstname, lastname, role, points, createdAt, solde, numeroCarte, birthdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getPoints());
            ps.setTimestamp(7, new Timestamp(user.getCreatedAt().getTime()));
            ps.setFloat(8, user.getSolde());

            if (user.getNumeroCarte() == null) ps.setNull(9, Types.BIGINT);
            else ps.setLong(9, user.getNumeroCarte());

            if (user.getBirthdate() == null) ps.setNull(10, Types.DATE);
            else ps.setDate(10, new java.sql.Date(user.getBirthdate().getTime()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }

        return user;
    }

    private boolean emailExists(Connection cnx, String email) throws SQLException {
        String sql = "SELECT 1 FROM `user` WHERE email=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFirstName(rs.getString("firstname"));
        u.setLastName(rs.getString("lastname"));
        u.setRole(rs.getString("role"));
        u.setPoints(rs.getInt("points"));
        u.setCreatedAt(rs.getTimestamp("createdAt"));
        u.setSolde(rs.getFloat("solde"));

        long nc = rs.getLong("numeroCarte");
        u.setNumeroCarte(rs.wasNull() ? null : nc);

        u.setBirthdate(rs.getDate("birthdate"));
        return u;
    }
}
