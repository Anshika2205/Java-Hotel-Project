import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HotelAdminGUI extends JFrame {

    public HotelAdminGUI() {
        // Window Setup
        setTitle("Hotel Management System - Admin");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Panel (Purple / Dark Gray Theme)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 52, 54));
        headerPanel.setLayout(new GridLayout(2, 1));

        JLabel titleLabel = new JLabel("HOTEL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Welcome, Hotel Admin", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Buttons Grid Panel (Same as Image Grid)
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 2, 20, 20));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JButton btnSetupRooms = createStyledButton("1. Setup / Manage Rooms");
        JButton btnViewGuests = createStyledButton("2. View Available Rooms");
        JButton btnBookRoom = createStyledButton("3. Register Guest & Book Room");
        JButton btnViewBookings = createStyledButton("4. View Active Bookings (JOIN)");
        JButton btnCheckout = createStyledButton("5. Guest Check-Out");
        JButton btnExit = createStyledButton("6. Logout / Exit");

        gridPanel.add(btnSetupRooms);
        gridPanel.add(btnViewGuests);
        gridPanel.add(btnBookRoom);
        gridPanel.add(btnViewBookings);
        gridPanel.add(btnCheckout);
        gridPanel.add(btnExit);

        add(gridPanel, BorderLayout.CENTER);

        // --- BUTTON ACTIONS ---

        // 1. Setup Initial Rooms
        btnSetupRooms.addActionListener(e -> {
            String sql = "INSERT IGNORE INTO rooms (room_no, type, price_per_night, status) VALUES " +
                    "(101, 'Deluxe Single', 2000, 'Available'), " +
                    "(102, 'Deluxe Single', 2000, 'Available'), " +
                    "(103, 'Super Deluxe Double', 4000, 'Available'), " +
                    "(104, 'Super Deluxe Double', 4000, 'Available'), " +
                    "(105, 'Executive Suite', 7500, 'Available')";

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                int rows = stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "✅ 5 Rooms database mein initialize ho gaye hain!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // 2. View Available Rooms
        btnViewGuests.addActionListener(e -> showTableWindow("SELECT room_no, type, price_per_night, status FROM rooms WHERE status='Available'",
                new String[]{"Room No", "Type", "Price/Night", "Status"}, "Available Rooms Inventory"));

        // 3. Register & Book Room
        btnBookRoom.addActionListener(e -> openBookingForm());

        // 4. View All Active Bookings (3-Table JOIN)
        btnViewBookings.addActionListener(e -> {
            String sql = "SELECT b.booking_id, c.name, c.phone, r.room_no, r.type, b.days, b.total_bill " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_no = r.room_no";
            showTableWindow(sql, new String[]{"Booking ID", "Guest Name", "Phone", "Room No", "Type", "Days", "Total Bill (with GST)"}, "Active Hotel Bookings");
        });

        // 5. Checkout
        btnCheckout.addActionListener(e -> {
            String roomStr = JOptionPane.showInputDialog(this, "Checkout ke liye Room Number enter karein (e.g. 101):");
            if (roomStr != null && !roomStr.isEmpty()) {
                int roomNo = Integer.parseInt(roomStr);
                try (Connection conn = DBConnection.getConnection()) {
                    String delSql = "DELETE FROM bookings WHERE room_no = ?";
                    PreparedStatement pstmtDel = conn.prepareStatement(delSql);
                    pstmtDel.setInt(1, roomNo);
                    int rows = pstmtDel.executeUpdate();

                    if (rows > 0) {
                        PreparedStatement pstmtFree = conn.prepareStatement("UPDATE rooms SET status = 'Available' WHERE room_no = ?");
                        pstmtFree.setInt(1, roomNo);
                        pstmtFree.executeUpdate();
                        JOptionPane.showMessageDialog(this, "✅ Check-out Successful! Room " + roomNo + " ab vacant ho gaya hai.");
                    } else {
                        JOptionPane.showMessageDialog(this, "❌ Is Room No. par koi active booking nahi mili.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        // 6. Exit
        btnExit.addActionListener(e -> System.exit(0));
    }

    // Custom Button Helper
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(236, 240, 241));
        btn.setFocusPainted(false);
        return btn;
    }

    // Helper Window to Display SQL Data in a Swing JTable
    private void showTableWindow(String query, String[] columnNames, String title) {
        JFrame tableFrame = new JFrame(title);
        tableFrame.setSize(700, 400);
        tableFrame.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        tableFrame.add(new JScrollPane(table));
        tableFrame.setVisible(true);
    }

    // Popup Form for Guest Registration
    private void openBookingForm() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField idField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField daysField = new JTextField();

        Object[] message = {
                "Customer Name:", nameField,
                "Phone Number:", phoneField,
                "Email ID:", emailField,
                "ID Proof:", idField,
                "Room Number (101-105):", roomField,
                "Stay Days:", daysField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "New Booking Form", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                int roomNo = Integer.parseInt(roomField.getText());
                int days = Integer.parseInt(daysField.getText());

                String roomCheck = "SELECT price_per_night, status FROM rooms WHERE room_no = ?";
                PreparedStatement pstmtRoom = conn.prepareStatement(roomCheck);
                pstmtRoom.setInt(1, roomNo);
                ResultSet rsRoom = pstmtRoom.executeQuery();

                if (rsRoom.next()) {
                    if ("Booked".equalsIgnoreCase(rsRoom.getString("status"))) {
                        JOptionPane.showMessageDialog(this, "❌ Yeh Room pehle se Booked hai!");
                        return;
                    }

                    double subtotal = rsRoom.getDouble("price_per_night") * days;
                    double totalBill = subtotal + (subtotal * 0.18);

                    // Insert Customer
                    String custSql = "INSERT INTO customers (name, phone, email, id_proof) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmtCust = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS);
                    pstmtCust.setString(1, nameField.getText());
                    pstmtCust.setString(2, phoneField.getText());
                    pstmtCust.setString(3, emailField.getText());
                    pstmtCust.setString(4, idField.getText());
                    pstmtCust.executeUpdate();

                    ResultSet gk = pstmtCust.getGeneratedKeys();
                    int custId = 0;
                    if (gk.next()) custId = gk.getInt(1);

                    // Insert Booking
                    String bookSql = "INSERT INTO bookings (customer_id, room_no, days, total_bill, payment_status) VALUES (?, ?, ?, ?, 'Paid')";
                    PreparedStatement pstmtBook = conn.prepareStatement(bookSql);
                    pstmtBook.setInt(1, custId);
                    pstmtBook.setInt(2, roomNo);
                    pstmtBook.setInt(3, days);
                    pstmtBook.setDouble(4, totalBill);
                    pstmtBook.executeUpdate();

                    // Update Status
                    PreparedStatement pstmtUp = conn.prepareStatement("UPDATE rooms SET status = 'Booked' WHERE room_no = ?");
                    pstmtUp.setInt(1, roomNo);
                    pstmtUp.executeUpdate();

                    JOptionPane.showMessageDialog(this, "🎉 BOOKING SUCCESSFUL!\nTotal Bill (with 18% GST): ₹" + totalBill);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Galat Room Number!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Booking Failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        DBConnection.initDatabase();
        SwingUtilities.invokeLater(() -> new HotelAdminGUI().setVisible(true));
    }
}