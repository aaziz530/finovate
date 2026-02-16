package org.esprit.finovate.services;

import org.esprit.finovate.entities.User;
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
    public User register(String email, String password, String firstName, String lastName, Date birthdate,
            String cardNumber)
            throws SQLException {
        return register(email, password, firstName, lastName, birthdate, cardNumber, null);
    }

    @Override
    public User register(String email, String password, String firstName, String lastName, Date birthdate,
            String cardNumber, String cinNumber)
            throws SQLException {
        User user = new User(email, password, firstName, lastName, birthdate, cardNumber, cinNumber);
        user.setPassword(PasswordUtils.sha256(password));

        if (connection == null) {
            throw new SQLException("Connexion DB est null. Vérifie MyDataBase");
        }

        if (emailExists(user.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }
        if (cardNumberExists(user.getCardNumber())) {
            throw new IllegalStateException("Card Number already exists");
        }
        if (cinExists(user.getCinNumber())) {
            throw new IllegalStateException("CIN already exists");
        }

        String sql = "INSERT INTO `user` (email, password, firstname, lastname, role, points, createdAt, solde, birthdate, cardNumber, cin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getPoints());
            ps.setTimestamp(7, new Timestamp(user.getCreatedAt().getTime()));
            ps.setFloat(8, user.getSolde());

            if (user.getBirthdate() == null)
                ps.setNull(9, Types.DATE);
            else
                ps.setDate(9, new java.sql.Date(user.getBirthdate().getTime()));

            ps.setString(10, user.getCardNumber());

            ps.setString(11, user.getCinNumber());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }

        return user;
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }
        String sql = "SELECT 1 FROM `user` WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean cardNumberExists(String cardNumber) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }
        String sql = "SELECT 1 FROM `user` WHERE cardNumber=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean cinExists(String cin) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }
        String sql = "SELECT 1 FROM `user` WHERE cin=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cin);
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

        u.setBirthdate(rs.getDate("birthdate"));
        u.setCardNumber(rs.getString("cardNumber"));
        u.setCinNumber(rs.getString("cin"));
        return u;
    }

    @Override
    public java.util.List<User> getAllUsers() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM `user` ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    @Override
    public User getUserById(Long id) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT * FROM `user` WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void updateUser(User user) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update");
        }

        // Uniqueness checks for update
        String checkSql = "SELECT id FROM `user` WHERE (email = ? OR cardNumber = ? OR cin = ?) AND id != ?";
        try (PreparedStatement psCheck = connection.prepareStatement(checkSql)) {
            psCheck.setString(1, user.getEmail());
            psCheck.setString(2, user.getCardNumber());
            psCheck.setString(3, user.getCinNumber());
            psCheck.setLong(4, user.getId());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Conflict: Email, Card Number, or CIN already used by another account.");
                }
            }
        }

        String sql = "UPDATE `user` SET email=?, firstname=?, lastname=?, role=?, points=?, solde=?, birthdate=?, cardNumber=?, cin=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getPoints());
            ps.setFloat(6, user.getSolde());

            if (user.getBirthdate() == null) {
                ps.setNull(7, Types.DATE);
            } else {
                ps.setDate(7, new java.sql.Date(user.getBirthdate().getTime()));
            }

            ps.setString(8, user.getCardNumber());
            ps.setString(9, user.getCinNumber());
            ps.setLong(10, user.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Update failed, user not found with ID: " + user.getId());
            }
        }
    }

    @Override
    public void deleteUser(Long id) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null for deletion");
        }

        String sql = "DELETE FROM `user` WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Delete failed, user not found with ID: " + id);
            }
        }
    }

    @Override
    public int getTotalUsersCount() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT COUNT(*) as total FROM `user`";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    @Override
    public int getActiveUsersCount() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        // Active users = users created in the last 30 days
        String sql = "SELECT COUNT(*) as active FROM `user` WHERE createdAt >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("active");
            }
        }

        return 0;
    }

    @Override
    public java.util.List<User> searchUsers(String searchTerm) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE " +
                "email LIKE ? OR " +
                "firstname LIKE ? OR " +
                "lastname LIKE ? OR " +
                "CONCAT(firstname, ' ', lastname) LIKE ? " +
                "ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }

        return users;
    }

    @Override
    public void updatePassword(String email, String newPassword) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String hashedPassword = PasswordUtils.sha256(newPassword);
        String sql = "UPDATE `user` SET password=? WHERE email=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Password update failed, user not found with email: " + email);
            }
        }
    }
}
