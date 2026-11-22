import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDbConnection {
    public static void main(String[] args) {
        System.out.println("Testing JDBC Connection to lockmyseat...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_database(), current_user, version();")) {
            if (rs.next()) {
                System.out.println("SUCCESS! Connected to Database: " + rs.getString(1));
                System.out.println("Logged in as User: " + rs.getString(2));
                System.out.println("Server version: " + rs.getString(3));
            }
        } catch (Exception e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
