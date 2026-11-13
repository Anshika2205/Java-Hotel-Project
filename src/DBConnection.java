import java.sql.*;

public class DBConnection {
    // Apne MySQL ka URL, Username aur Password yahan set karein
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASS = "ANSHI2205"; // <-- Apna Password yahan likhein

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL + "hotel_db", USER, PASS);
    }

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // 1. Database create karein
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS hotel_db");
            stmt.executeUpdate("USE hotel_db");

            // 2. Rooms Table
            String createRooms = "CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_no INT PRIMARY KEY, " +
                    "type VARCHAR(30), " +
                    "price_per_night DECIMAL(10,2), " +
                    "status VARCHAR(20) DEFAULT 'Available')";
            stmt.executeUpdate(createRooms);

            // 3. Customers Table
            String createCustomers = "CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(50), " +
                    "phone VARCHAR(15), " +
                    "email VARCHAR(50), " +
                    "id_proof VARCHAR(30))";
            stmt.executeUpdate(createCustomers);

            // 4. Bookings Table (Joins Rooms & Customers via Foreign Keys)
            String createBookings = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "booking_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "customer_id INT, " +
                    "room_no INT, " +
                    "days INT, " +
                    "total_bill DECIMAL(10,2), " +
                    "payment_status VARCHAR(20) DEFAULT 'Pending', " +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (room_no) REFERENCES rooms(room_no) ON DELETE CASCADE)";
            stmt.executeUpdate(createBookings);

            System.out.println("✅ Database & Tables initialised successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Connection/Database Error: " + e.getMessage());
        }
    }
}
