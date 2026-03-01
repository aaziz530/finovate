package org.esprit.finovate.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Databaseconnection {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/fintechforum";
    private static final String USER = "root";  // Default XAMPP username
    private static final String PASSWORD = "";  // Default XAMPP password (empty)

    // Singleton instance
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private Databaseconnection() {}

    /**
     * Get database connection (creates if not exists)
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create connection if it doesn't exist or is closed
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database Connected Successfully!");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed!");
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database Connection Closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}