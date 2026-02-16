package org.esprit.finovate.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;

    private static final String URL = "jdbc:mysql://localhost:3306/finovate";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    // Private constructor (Singleton)
    private MyDataBase() {
        try {
            // Load MySQL Driver (optional for new versions but professional)
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to database successfully");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed");
            e.printStackTrace();
        }
    }

    // Singleton instance
    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    // Getter for connection
    public Connection getConnection() {
        return connection;
    }
}
