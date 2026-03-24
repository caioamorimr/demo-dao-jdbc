package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DB {

    private static volatile Connection conn = null;

    public static Connection getConnection() {
        if (conn == null) {
            synchronized (DB.class) {
                if (conn == null) {
                    Properties prop = loadProperties();
                    String url = prop.getProperty("dburl");
                    try {
                        conn = DriverManager.getConnection(url, prop);
                    } catch (SQLException e) {
                        throw new DbException(e.getMessage(), e);
                    }
                }
            }
        } else {
            try {
                if (conn.isClosed()) {
                    conn = null;
                    getConnection();
                }
            } catch (SQLException e) {
                throw new DbException(e.getMessage(), e);
            }
        }
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new DbException(e.getMessage(), e);
            } finally {
                conn = null;
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new DbException(e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new DbException(e.getMessage(), e);
            }
        }
    }

    private static Properties loadProperties() {
        try (InputStream is = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new DbException("Properties file not found");
            }
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        } catch (IOException e) {
            throw new DbException(e.getMessage(), e);
        }
    }
}