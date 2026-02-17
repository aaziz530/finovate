package org.example.services;

import org.example.entities.User;
import org.example.utils.Databaseconnection;
import org.example.utils.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    /**
     * CREATE - Créer un utilisateur
     * Contrôles: validation email, username, password
     */
    public boolean createUser(User user) throws SQLException {
        // Validation
        if (!ValidationUtils.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username invalide (3-50 caractères alphanum)");
        }
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email invalide");
        }
        if (!ValidationUtils.isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe invalide (min 6 caractères)");
        }

        String query = "INSERT INTO users (username, email, password, role, is_blocked) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ValidationUtils.sanitize(user.getUsername()));
            stmt.setString(2, ValidationUtils.sanitize(user.getEmail()));
            stmt.setString(3, user.getPassword()); // TODO: Hasher le mot de passe
            stmt.setString(4, user.getRole().name());
            stmt.setBoolean(5, user.isBlocked());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * READ - Récupérer un utilisateur par ID
     */
    public User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer un utilisateur par username
     */
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * READ - Récupérer tous les utilisateurs
     * Restriction: Seulement ADMIN et MODERATOR
     */
    public List<User> getAllUsers(User.Role requestingUserRole) throws SQLException {
        if (requestingUserRole != User.Role.ADMIN && requestingUserRole != User.Role.MODERATOR) {
            throw new SecurityException("Accès refusé: seuls ADMIN et MODERATOR peuvent voir tous les utilisateurs");
        }

        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * UPDATE - Modifier un utilisateur
     * Contrôles:
     * - USER peut modifier son propre profil (sauf role et is_blocked)
     * - ADMIN peut tout modifier
     * - MODERATOR ne peut pas modifier
     */
    public boolean updateUser(User user, int requestingUserId, User.Role requestingUserRole) throws SQLException {
        // Vérifications de sécurité
        if (requestingUserRole == User.Role.MODERATOR) {
            throw new SecurityException("Les modérateurs ne peuvent pas modifier les utilisateurs");
        }

        if (requestingUserRole == User.Role.USER && user.getId() != requestingUserId) {
            throw new SecurityException("Vous ne pouvez modifier que votre propre profil");
        }

        // Validation
        if (!ValidationUtils.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username invalide");
        }
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email invalide");
        }

        String query;
        if (requestingUserRole == User.Role.ADMIN) {
            // Admin peut tout modifier
            query = "UPDATE users SET username = ?, email = ?, password = ?, role = ?, is_blocked = ?, " +
                    "firstname = ?, lastname = ?, birthdate = ? WHERE id = ?";
        } else {
            // User peut modifier username, email, password et infos personnelles
            query = "UPDATE users SET username = ?, email = ?, password = ?, " +
                    "firstname = ?, lastname = ?, birthdate = ? WHERE id = ?";
        }

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ValidationUtils.sanitize(user.getUsername()));
            stmt.setString(2, ValidationUtils.sanitize(user.getEmail()));
            stmt.setString(3, user.getPassword());

            if (requestingUserRole == User.Role.ADMIN) {
                stmt.setString(4, user.getRole().name());
                stmt.setBoolean(5, user.isBlocked());
                stmt.setString(6, user.getFirstname());
                stmt.setString(7, user.getLastname());
                if (user.getBirthdate() != null) {
                    stmt.setDate(8, new java.sql.Date(user.getBirthdate().getTime()));
                } else {
                    stmt.setNull(8, Types.DATE);
                }
                stmt.setInt(9, user.getId());
            } else {
                stmt.setString(4, user.getFirstname());
                stmt.setString(5, user.getLastname());
                if (user.getBirthdate() != null) {
                    stmt.setDate(6, new java.sql.Date(user.getBirthdate().getTime()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }
                stmt.setInt(7, user.getId());
            }

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DELETE - Supprimer un utilisateur
     * Restriction: Seulement ADMIN
     */
    public boolean deleteUser(int userId, User.Role requestingUserRole) throws SQLException {
        if (requestingUserRole != User.Role.ADMIN) {
            throw new SecurityException("Seuls les administrateurs peuvent supprimer des utilisateurs");
        }

        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * BLOQUER un utilisateur
     * Restriction: Seulement MODERATOR et ADMIN
     */
    public boolean blockUser(int userId, User.Role requestingUserRole) throws SQLException {
        if (requestingUserRole != User.Role.MODERATOR && requestingUserRole != User.Role.ADMIN) {
            throw new SecurityException("Seuls MODERATOR et ADMIN peuvent bloquer des utilisateurs");
        }

        String query = "UPDATE users SET is_blocked = TRUE WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * DÉBLOQUER un utilisateur
     * Restriction: Seulement MODERATOR et ADMIN
     */
    public boolean unblockUser(int userId, User.Role requestingUserRole) throws SQLException {
        if (requestingUserRole != User.Role.MODERATOR && requestingUserRole != User.Role.ADMIN) {
            throw new SecurityException("Seuls MODERATOR et ADMIN peuvent débloquer des utilisateurs");
        }

        String query = "UPDATE users SET is_blocked = FALSE WHERE id = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Authentification
     */
    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // TODO: Comparer avec hash

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * Login - Authentification avec email ou username
     */
    public User login(String emailOrUsername, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE (email = ? OR username = ?) AND password = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, emailOrUsername);
            stmt.setString(2, emailOrUsername);
            stmt.setString(3, password); // TODO: Comparer avec hash

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                // Check if user is blocked
                if (user.isBlocked()) {
                    throw new SQLException("Your account has been blocked. Please contact support.");
                }
                return user;
            }
            return null;
        }
    }

    /**
     * Mapper ResultSet vers User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                User.Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_blocked"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
        
        // Add financial fields
        user.setFirstname(rs.getString("firstname"));
        user.setLastname(rs.getString("lastname"));
        user.setPoints(rs.getInt("points"));
        user.setSolde(rs.getFloat("solde"));
        user.setBirthdate(rs.getDate("birthdate"));
        
        Long numeroCarte = rs.getObject("numero_carte", Long.class);
        user.setNumeroCarte(numeroCarte);
        
        user.setCinNumber(rs.getString("cin_number"));
        
        return user;
    }
}
