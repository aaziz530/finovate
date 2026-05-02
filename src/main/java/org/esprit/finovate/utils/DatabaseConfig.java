package org.esprit.finovate.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized database configuration
 */
public class DatabaseConfig {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finovate";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Get a database connection
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Get database URL
     */
    public static String getUrl() {
        return DB_URL;
    }
    
    /**
     * Get database user
     */
    public static String getUser() {
        return DB_USER;
    }
    
    /**
     * Get database password
     */
    public static String getPassword() {
        return DB_PASSWORD;
    }
}
