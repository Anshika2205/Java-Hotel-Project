import java.sql.*;
import java.util.Scanner;

public class HotelSystem {

    public static void main(String[] args) {
        // Step 1: Database aur tables auto-setup karein
        DBConnection.initDatabase();

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==================================================");
            System.out.println("🏨 GRAND PALACE - HOTEL MANAGEMENT SYSTEM");
            System.out.println("==================================================");
            System.out.println("1. 🚪 Add Initial Rooms Setup (101-105)");
            System.out.println("2. 🔍 View Available Rooms");
            System.out.println("3. 📑 Register Customer & Book Room");
            System.out.println("4. 📋 View All Active Bookings (3-Table JOIN)");
            System.out.println("5. 🔎 Search Booking by Customer Phone");
            System.out.println("6. 💳 Checkout & Settle Bill");
            System.out.println("7. 📊 View Total Hotel Revenue");
            System.out.println("8. ❌ Exit");
            System.out.print("👉 Choice select karein (1-8): ");

            int choice = sc.nextInt();
            sc.nextLine(); // Clear scanner buffer

            switch (choice) {
                case 1: setupRooms(); break;
                case 2: viewAvailableRooms(); break;
                case 3: bookRoom(sc); break;
                case 4: viewAllBookings(); break;
                case 5: searchBooking(sc); break;
                case 6: checkOut(sc); break;
                case 7: viewRevenue(); break;
                case 8:
                    System.out.println("System closing. Thank you!");
                    return;
                default:
                    System.out.println("❌ Invalid choice! Phir se try karein.");
            }
        }
    }

    // 1. Initial Rooms Batch Setup
    private static void setupRooms() {
        String sql = "INSERT IGNORE INTO rooms (room_no, type, price_per_night, status) VALUES " +
                "(101, 'Deluxe Single', 2000, 'Available'), " +
                "(102, 'Deluxe Single', 2000, 'Available'), " +
                "(103, 'Super Deluxe Double', 4000, 'Available'), " +
                "(104, 'Super Deluxe Double', 4000, 'Available'), " +
                "(105, 'Executive Suite', 7500, 'Available')";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("✅ " + rows + " Rooms system mein ready ho gaye hain!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. View Available Rooms
    private static void viewAvailableRooms() {
        String sql = "SELECT * FROM rooms WHERE status = 'Available'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- AVAILABLE ROOMS LIST ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Room No: " + rs.getInt("room_no") +
                        " | Type: " + rs.getString("type") +
                        " | Price/Night: ₹" + rs.getDouble("price_per_night"));
            }
            if (!found) System.out.println("Koi room available nahi hai!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3. Register Customer & Book Room
    private static void bookRoom(Scanner sc) {
        System.out.println("\n--- CUSTOMER REGISTRATION & BOOKING ---");
        System.out.print("Customer Name: ");
        String name = sc.nextLine();
        System.out.print("Phone Number: ");
        String phone = sc.nextLine();
        System.out.print("Email ID: ");
        String email = sc.nextLine();
        System.out.print("ID Proof (Aadhar/Passport): ");
        String idProof = sc.nextLine();

        System.out.print("Select Room Number (e.g. 101): ");
        int roomNo = sc.nextInt();
        System.out.print("Number of Stay Days: ");
        int days = sc.nextInt();

        try (Connection conn = DBConnection.getConnection()) {
            // Check Room Status & Price
            String roomCheck = "SELECT price_per_night, status FROM rooms WHERE room_no = ?";
            PreparedStatement pstmtRoom = conn.prepareStatement(roomCheck);
            pstmtRoom.setInt(1, roomNo);
            ResultSet rsRoom = pstmtRoom.executeQuery();

            if (rsRoom.next()) {
                if ("Booked".equalsIgnoreCase(rsRoom.getString("status"))) {
                    System.out.println("❌ Yeh room pehle se Booked hai!");
                    return;
                }

                double pricePerNight = rsRoom.getDouble("price_per_night");
                double subtotal = pricePerNight * days;
                double gst = subtotal * 0.18; // 18% GST calculation
                double totalBill = subtotal + gst;

                // Step A: Insert Customer
                String custSql = "INSERT INTO customers (name, phone, email, id_proof) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmtCust = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS);
                pstmtCust.setString(1, name);
                pstmtCust.setString(2, phone);
                pstmtCust.setString(3, email);
                pstmtCust.setString(4, idProof);
                pstmtCust.executeUpdate();

                ResultSet generatedKeys = pstmtCust.getGeneratedKeys();
                int custId = 0;
                if (generatedKeys.next()) custId = generatedKeys.getInt(1);

                // Step B: Create Booking
                String bookSql = "INSERT INTO bookings (customer_id, room_no, days, total_bill, payment_status) VALUES (?, ?, ?, ?, 'Paid')";
                PreparedStatement pstmtBook = conn.prepareStatement(bookSql);
                pstmtBook.setInt(1, custId);
                pstmtBook.setInt(2, roomNo);
                pstmtBook.setInt(3, days);
                pstmtBook.setDouble(4, totalBill);
                pstmtBook.executeUpdate();

                // Step C: Update Room Status
                String updateRoom = "UPDATE rooms SET status = 'Booked' WHERE room_no = ?";
                PreparedStatement pstmtUp = conn.prepareStatement(updateRoom);
                pstmtUp.setInt(1, roomNo);
                pstmtUp.executeUpdate();

                System.out.println("\n🎉 BOOKING SUCCESSFUL!");
                System.out.println("Subtotal: ₹" + subtotal + " | GST (18%): ₹" + gst);
                System.out.println("Final Bill Amount Paid: ₹" + totalBill);

            } else {
                System.out.println("❌ Invalid Room Number!");
            }

        } catch (SQLException e) {
            System.out.println("Booking Error: " + e.getMessage());
        }
    }

    // 4. View All Bookings (JOIN Query across 3 tables)
    private static void viewAllBookings() {
        String sql = "SELECT b.booking_id, c.name, c.phone, r.room_no, r.type, b.days, b.total_bill, b.payment_status " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN rooms r ON b.room_no = r.room_no";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- ALL ACTIVE HOTEL BOOKINGS ---");
            while (rs.next()) {
                System.out.println("Booking ID: #" + rs.getInt("booking_id") +
                        " | Guest: " + rs.getString("name") + " (" + rs.getString("phone") + ")" +
                        " | Room: " + rs.getInt("room_no") + " [" + rs.getString("type") + "]" +
                        " | Days: " + rs.getInt("days") +
                        " | Total Bill: ₹" + rs.getDouble("total_bill") +
                        " | Payment: " + rs.getString("payment_status"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 5. Search Booking by Customer Phone
    private static void searchBooking(Scanner sc) {
        System.out.print("Search ke liye Customer Phone Number enter karein: ");
        String phone = sc.nextLine();

        String sql = "SELECT b.booking_id, c.name, c.email, r.room_no, r.type, b.total_bill " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN rooms r ON b.room_no = r.room_no " +
                "WHERE c.phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- SEARCH RESULTS ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: #" + rs.getInt("booking_id") +
                        " | Guest Name: " + rs.getString("name") +
                        " | Email: " + rs.getString("email") +
                        " | Room No: " + rs.getInt("room_no") + " (" + rs.getString("type") + ")" +
                        " | Bill: ₹" + rs.getDouble("total_bill"));
            }
            if (!found) System.out.println("❌ Is phone number par koi booking nahi mili!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 6. Checkout Customer
    private static void checkOut(Scanner sc) {
        System.out.print("Check-out ke liye Room Number enter karein: ");
        int roomNo = sc.nextInt();

        try (Connection conn = DBConnection.getConnection()) {
            // Delete Booking record
            String delSql = "DELETE FROM bookings WHERE room_no = ?";
            PreparedStatement pstmtDel = conn.prepareStatement(delSql);
            pstmtDel.setInt(1, roomNo);
            int rows = pstmtDel.executeUpdate();

            if (rows > 0) {
                // Free up Room
                String freeRoom = "UPDATE rooms SET status = 'Available' WHERE room_no = ?";
                PreparedStatement pstmtFree = conn.prepareStatement(freeRoom);
                pstmtFree.setInt(1, roomNo);
                pstmtFree.executeUpdate();

                System.out.println("✅ Room " + roomNo + " successfully checked out and freed up!");
            } else {
                System.out.println("❌ Is room par koi active booking nahi mili!");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 7. View Total Revenue
    private static void viewRevenue() {
        String sql = "SELECT SUM(total_bill) AS total_revenue FROM bookings";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                double revenue = rs.getDouble("total_revenue");
                System.out.println("\n💰 TOTAL HOTEL REVENUE GENERATED: ₹" + revenue);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}