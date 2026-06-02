package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
    private static volatile Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/kereta_db";
    private static final String USER = "root";
    private static final String PASS = "";

    // Private constructor mencegah inisialisasi dari luar
    private Connector() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }
}