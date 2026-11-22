package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection demonstrates Encapsulation and File I/O.
 * Manages loading database credentials from config.properties and provides
 * JDBC Connections.
 */
public class DatabaseConnection {
    private static Properties props = new Properties();

    static {
        loadProperties();
        try {
            // Explicitly load PostgreSQL JDBC driver class
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        // Try file in current directory or backend/config.properties
        File configFile = new File("config.properties");
        if (!configFile.exists()) {
            configFile = new File("backend/config.properties");
        }

        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
            } catch (IOException ex) {
                System.err.println("Could not load config.properties file: " + ex.getMessage());
            }
        } else {
            System.err.println("config.properties not found at " + configFile.getAbsolutePath());
        }
    }

    public static Connection getConnection() throws SQLException {
        // Priority 1: Cloud Environment Variables (for Render / Docker / Supabase)
        String envDbUrl = System.getenv("DB_URL");
        if (envDbUrl == null) {
            envDbUrl = System.getenv("DATABASE_URL");
        }
        String envDbUser = System.getenv("DB_USER");
        String envDbPass = System.getenv("DB_PASSWORD");

        if (envDbUrl != null && !envDbUrl.trim().isEmpty()) {
            if (envDbUser != null && envDbPass != null) {
                return DriverManager.getConnection(envDbUrl.trim(), envDbUser.trim(), envDbPass.trim());
            } else {
                return DriverManager.getConnection(envDbUrl.trim());
            }
        }

        // Priority 2: Local config.properties file
        String url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/lockmyseat");
        String user = props.getProperty("db.user", "postgres");
        String pass = props.getProperty("db.password", "breaking@bad");
        return DriverManager.getConnection(url, user, pass);
    }

    public static int getServerPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return Integer.parseInt(props.getProperty("server.port", "8080"));
    }
}
