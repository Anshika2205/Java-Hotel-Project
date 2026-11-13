import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        // Window Setup
        setTitle("Hotel Management System - Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Screen ke center mein khulega
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 52, 54));
        JLabel titleLabel = new JLabel("🔑 HOTEL ADMIN LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passField = new JPasswordField();

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(9, 132, 227));

        JButton btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Arial", Font.BOLD, 14));

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(btnLogin);
        formPanel.add(btnReset);

        add(formPanel, BorderLayout.CENTER);

        // --- BUTTON ACTIONS ---

        // Login Button Click Logic
        btnLogin.addActionListener(e -> performLogin());

        // Password Field par Enter dabane par bhi Login ho jaye
        passField.addActionListener(e -> performLogin());

        // Reset Button Click Logic
        btnReset.addActionListener(e -> {
            userField.setText("");
            passField.setText("");
        });
    }

    private void performLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        // Default Credentials (Username: admin | Password: admin123)
        if ("admin".equals(username) && "admin123".equals(password)) {
            JOptionPane.showMessageDialog(this, "🎉 Login Successful! Opening Dashboard...");

            this.dispose(); // Login window close karein

            // Database Initialize karke Main Dashboard Open Karein
            DBConnection.initDatabase();
            new HotelAdminGUI().setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this, "❌ Invalid Username or Password!", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}