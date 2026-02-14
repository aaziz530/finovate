package org.esprit.finovate.services;

import org.esprit.finovate.entities.User;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IUserService {
    // Authentication methods
    User login(String email, String password) throws SQLException;

    void logout();

    User register(String email, String password, String firstName, String lastName, Date birthdate) throws SQLException;

    // Admin user management methods
    List<User> getAllUsers() throws SQLException;

    User getUserById(Long id) throws SQLException;

    void updateUser(User user) throws SQLException;

    void deleteUser(Long id) throws SQLException;

    // Statistics methods
    int getTotalUsersCount() throws SQLException;

    int getActiveUsersCount() throws SQLException;

    // Search method
    List<User> searchUsers(String searchTerm) throws SQLException;
}
