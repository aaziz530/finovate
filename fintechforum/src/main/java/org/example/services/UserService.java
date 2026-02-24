package org.example.services;

import org.example.entities.User;
import org.example.utils.Databaseconnection;
import org.example.utils.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserService {

    private Connection connection;

    public UserService() {
        this.connection = Databaseconnection.getConnection();
    }

    /**
     * Login - Authentification avec email et password
     */
    public User login(String email, String password) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password); // TODO: Hash password

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    // Check if user is blocked
                    if (user.isBlocked()) {
                        throw new SQLException("Your account has been blocked. Please contact support.");
                    }
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * CREATE - Créer un utilisateur
     */
    public boolean createUser(User user) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        // Validation
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email invalide");
        }
        if (!ValidationUtils.isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe invalide (min 6 caractères)");
        }

        // Check if email already exists
        if (emailExists(user.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        String sql = "INSERT INTO users (email, password, firstname, lastname, role, is_blocked, points, solde, birthdate, numero_carte, cin_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ValidationUtils.sanitize(user.getEmail()));
            stmt.setString(2, user.getPassword()); // TODO: Hash password
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getRole() != null ? user.getRole() : "USER");
            stmt.setBoolean(6, user.isBlocked());
            stmt.setInt(7, user.getPoints());
            stmt.setFloat(8, user.getSolde());

            if (user.getBirthdate() != null) {
                stmt.setDate(9, new java.sql.Date(user.getBirthdate().getTime()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            if (user.getNumeroCarte() != null) {
                stmt.setLong(10, user.getNumeroCarte());
            } else {
                stmt.setNull(10, Types.BIGINT);
            }

            stmt.setString(11, user.getCinNumber());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * READ - Récupérer un utilisateur par ID
     */
    public User getUserById(Long id) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer un utilisateur par email
     */
    public User getUserByEmail(String email) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * UPDATE - Modifier un utilisateur
     */
    public void updateUser(User user) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update");
        }

        // Validation
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email invalide");
        }

        String sql = "UPDATE users SET email = ?, password = ?, firstname = ?, lastname = ?, " +
                "role = ?, is_blocked = ?, points = ?, solde = ?, birthdate = ?, " +
                "numero_carte = ?, cin_number = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ValidationUtils.sanitize(user.getEmail()));
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isBlocked());
            stmt.setInt(7, user.getPoints());
            stmt.setFloat(8, user.getSolde());

            if (user.getBirthdate() != null) {
                stmt.setDate(9, new java.sql.Date(user.getBirthdate().getTime()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            if (user.getNumeroCarte() != null) {
                stmt.setLong(10, user.getNumeroCarte());
            } else {
                stmt.setNull(10, Types.BIGINT);
            }

            stmt.setString(11, user.getCinNumber());
            stmt.setLong(12, user.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Update failed, user not found with ID: " + user.getId());
            }
        }
    }

    /**
     * DELETE - Supprimer un utilisateur
     */
    public void deleteUser(Long userId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null for deletion");
        }

        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Delete failed, user not found with ID: " + userId);
            }
        }
    }

    /**
     * BLOQUER un utilisateur
     */
    public boolean blockUser(Long userId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "UPDATE users SET is_blocked = TRUE WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DÉBLOQUER un utilisateur
     */
    public boolean unblockUser(Long userId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "UPDATE users SET is_blocked = FALSE WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Vérifier si un email existe
     */
    public boolean emailExists(String email) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Vérifier si un CIN existe
     */
    public boolean cinExists(String cin) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT 1 FROM users WHERE cin_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Obtenir le nombre total d'utilisateurs
     */
    public int getTotalUsersCount() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT COUNT(*) as total FROM users";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Obtenir le nombre d'utilisateurs actifs (créés dans les 30 derniers jours)
     */
    public int getActiveUsersCount() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "SELECT COUNT(*) as active FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("active");
            }
        }

        return 0;
    }

    /**
     * Rechercher des utilisateurs
     */
    public List<User> searchUsers(String searchTerm) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE " +
                "email LIKE ? OR " +
                "firstname LIKE ? OR " +
                "lastname LIKE ? OR " +
                "CONCAT(firstname, ' ', lastname) LIKE ? " +
                "ORDER BY created_at DESC";

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

    /**
     * Mettre à jour le mot de passe par email
     */
    public void updatePassword(String email, String newPassword) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        String sql = "UPDATE users SET password = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword); // TODO: Hash password
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Password update failed, user not found with email: " + email);
            }
        }
    }

    /**
     * Changer le mot de passe (avec vérification de l'ancien)
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        // Vérifier l'ancien mot de passe
        String checkSql = "SELECT 1 FROM users WHERE id = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setLong(1, userId);
            ps.setString(2, oldPassword); // TODO: Hash password
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Old password is incorrect");
                }
            }
        }

        // Mettre à jour le mot de passe
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setString(1, newPassword); // TODO: Hash password
            ps.setLong(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Password update failed, user not found with ID: " + userId);
            }
        }
    }

    /**
     * Mapper ResultSet vers User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setRole(rs.getString("role"));
        user.setBlocked(rs.getBoolean("is_blocked"));
        user.setPoints(rs.getInt("points"));
        user.setSolde(rs.getFloat("solde"));

        java.sql.Date birthdate = rs.getDate("birthdate");
        if (birthdate != null) {
            user.setBirthdate(new Date(birthdate.getTime()));
        }

        Long numeroCarte = rs.getObject("numero_carte", Long.class);
        user.setNumeroCarte(numeroCarte);

        user.setCinNumber(rs.getString("cin_number"));

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(new Date(createdAt.getTime()));
        }

        return user;
    }
}
