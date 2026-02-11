package org.esprit.finovate.services;

import org.esprit.finovate.entities.User;

import java.sql.SQLException;
import java.util.Date;

public interface IUserService {
    User login(String email, String password) throws SQLException;

    void logout();

    User register(String email, String password, String firstName, String lastName, Date birthdate) throws SQLException;
}
