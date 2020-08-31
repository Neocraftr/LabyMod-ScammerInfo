package de.neocraftr.scammerlist.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private Connection connection;
    private String filePath;

    public Database(String filePath) {
        this.filePath = filePath;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load the JDBC-Driver.");
            e.printStackTrace();
            return false;
        }

        if(connection != null) return true;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+getFilePath());
            if(!connection.isClosed()) {
                setupDatabase();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        if(connection == null) return;
        try {
            if(!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
    }

    private void setupDatabase() {
        try {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS scammer (uuid TEXT UNIQUE, name TEXT, type TEXT, PRIMARY KEY(uuid));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
