package com.smarterevententrysystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * One reusable place that creates MySQL connections.
 * Every DAO class calls DatabaseConnection.getConnection().
 *
 * >>> CHANGE DB_PASSWORD to your own MySQL root password. <<<
 */
public class DatabaseConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/smart_event_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";   // <-- put YOUR password here

    private DatabaseConnection() {
        // utility class, no objects needed
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found. Check the mysql-connector-j dependency in pom.xml.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
