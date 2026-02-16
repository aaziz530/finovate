package org.esprit.finovate.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;

    private final String URL = "jdbc:mysql://localhost:3306/finovate";
    private final String USER = "admin";
    private final String PSR = "admin";
    private Connection connection;

    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL,USER,PSR);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
