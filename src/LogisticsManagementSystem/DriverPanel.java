package LogisticsManagementSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

//Optional: Extend JTextField to add placeholder functionality
class PlaceholderTextField extends JTextField {
    private String placeholder;

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null) {
            g.setColor(Color.GRAY);
            g.drawString(placeholder, getInsets().left, 
                g.getFontMetrics().getMaxAscent() + getInsets().top);
        }
    }
}

public class DriverPanel extends JPanel {
    private User currentUser;
    private int driverId;
    private Connection conn;
    private JLabel statusLabel;
    private JTextArea currentDeliveryDetails;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private Timer refreshTimer;
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private Set<String> generatedOTPs = new HashSet<>();
    
    // Constants for UI
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color SIDEBAR_COLOR = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(45, 49, 54);
    private static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Color BUTTON_BACKGROUND_COLOR = new Color(138, 43, 226); // Purple
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE; // White
    

    private static final String[] HISTORY_COLUMNS = {
        "Order ID", "Pickup Location", "Drop Location", 
        "Started At", "Completed At", "Status"
    };
    
    public DriverPanel(User user) {
        this.currentUser = user;
        initializeDatabase();
        fetchDriverId();
        setupMainPanel();
        setupRefreshTimer();
    }
    
    private void styleButton(JButton button) {
        button.setBackground(new Color(138, 43, 226)); // Purple
        button.setForeground(Color.WHITE); // White
        button.setFocusPainted(false); // Remove focus border
        button.setBorderPainted(false); // Remove border
        button.setFont(MAIN_FONT); // Ensure consistent font
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set a hand cursor
    }

    
    private void initializeDatabase() {
        try {
            conn = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            handleDatabaseError("Database connection failed", e);
        }
    }
    
    private void fetchDriverId() {
        String sql = "SELECT driver_id FROM drivers WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                driverId = rs.getInt("driver_id");
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to fetch driver information", e);
        }
    }
    
    private void setupMainPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);
        
        // Create and add sidebar
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);
        
        // Create and add main content panel
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        // Add content panels
        mainContentPanel.add(createCurrentDeliveryPanel(), "current");
        mainContentPanel.add(createDeliveryHistoryPanel(), "history");
        
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        
        // Top navigation panel
        JPanel navPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        navPanel.setBackground(SIDEBAR_COLOR);
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        // Add navigation buttons
        JButton currentDeliveryBtn = createNavButton("Current Delivery", "current");
        JButton historyBtn = createNavButton("Delivery History", "history");
        
        navPanel.add(currentDeliveryBtn);
        navPanel.add(historyBtn);
        
        sidebar.add(navPanel, BorderLayout.NORTH);
        
        // Add logout button at bottom
        JButton logoutButton = createLogoutButton();
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(SIDEBAR_COLOR);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        logoutPanel.add(logoutButton);
        
        sidebar.add(logoutPanel, BorderLayout.SOUTH);
        
        return sidebar;
    }
    
    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        styleButton(button); // Apply the purple button style

        button.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_BACKGROUND_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BACKGROUND_COLOR);
            }
        });

        return button;
    }

    
    private JPanel createCurrentDeliveryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        statusLabel = new JLabel("Current Status: No Active Delivery");
        statusLabel.setFont(HEADER_FONT);
        statusLabel.setForeground(TEXT_COLOR);
        headerPanel.add(statusLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("Refresh Current Delivery");
        styleButton(refreshButton);
        refreshButton.setFont(MAIN_FONT);
        refreshButton.addActionListener(e -> refreshCurrentDelivery());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Delivery Details Panel
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBackground(BACKGROUND_COLOR);
        
        currentDeliveryDetails = new JTextArea();
        currentDeliveryDetails.setFont(MAIN_FONT);
        currentDeliveryDetails.setEditable(false);
        currentDeliveryDetails.setBackground(ACCENT_COLOR);
        currentDeliveryDetails.setForeground(TEXT_COLOR);
        currentDeliveryDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(currentDeliveryDetails);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add OTP and Status Update panels
        JPanel controlsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        controlsPanel.setBackground(BACKGROUND_COLOR);
        controlsPanel.add(createOtpPanel());
        controlsPanel.add(createStatusUpdatePanel());
        detailsPanel.add(controlsPanel, BorderLayout.SOUTH);
        
        panel.add(detailsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOtpPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel otpLabel = new JLabel("Enter Delivery OTP:");
        otpLabel.setForeground(TEXT_COLOR);
        otpLabel.setFont(MAIN_FONT);
        
        JTextField otpField = new JTextField(6);
        otpField.setFont(MAIN_FONT);
        
        JButton verifyButton = new JButton("Verify OTP");
        styleButton(verifyButton);
        verifyButton.setFont(MAIN_FONT);
        verifyButton.addActionListener(e -> verifyOTP(otpField.getText()));
        
        panel.add(otpLabel);
        panel.add(otpField);
        panel.add(verifyButton);
        
        return panel;
    }
    
 // Modified status update panel to handle location and OTP generation
    private JPanel createStatusUpdatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical arrangement
        panel.setBackground(BACKGROUND_COLOR);

        // Top row panel for status and combo box
        JPanel topRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRowPanel.setBackground(BACKGROUND_COLOR);
        JLabel statusLabel = new JLabel("Update Status:");
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setFont(MAIN_FONT);

        String[] statuses = {"update_location", "mark_for_delivery"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(MAIN_FONT);
        statusCombo.setMaximumRowCount(2); // Improve dropdown visibility

        topRowPanel.add(statusLabel);
        topRowPanel.add(statusCombo);
        panel.add(topRowPanel);

        // Location input panel
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.setBackground(BACKGROUND_COLOR);
        PlaceholderTextField locationField = new PlaceholderTextField();
        locationField.setFont(MAIN_FONT);
        locationField.setPlaceholder("Enter current location");
        locationField.setPreferredSize(new Dimension(300, 30));
        locationField.setVisible(false);

        // Update button panel
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updatePanel.setBackground(BACKGROUND_COLOR);
        JButton updateButton = new JButton("Update");
        styleButton(updateButton);
        updateButton.setFont(MAIN_FONT);

        locationPanel.add(locationField);
        updatePanel.add(updateButton);
        panel.add(locationPanel);
        panel.add(updatePanel);

        // Action listener for ComboBox to toggle visibility of locationField
        statusCombo.addActionListener(e -> {
            String selectedStatus = (String) statusCombo.getSelectedItem();
            locationField.setVisible("update_location".equals(selectedStatus));
            locationField.setText(""); // Clear previous text
            panel.revalidate();
            panel.repaint();
        });

        // Action listener for Update button
        updateButton.addActionListener(e -> {
            String selectedStatus = (String) statusCombo.getSelectedItem();
            if ("update_location".equals(selectedStatus)) {
                String location = locationField.getText().trim();

                if (location.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid location.", 
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Update location in the database (existing code remains the same)
                String sql = "UPDATE drivers SET current_location = ?, location_updated_at = CURRENT_TIMESTAMP WHERE driver_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location);
                    stmt.setInt(2, driverId);

                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        JOptionPane.showMessageDialog(this, "Location updated successfully.", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        locationField.setText(""); // Clear the field after successful update
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update location. Please try again.", 
                            "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    handleDatabaseError("Failed to update location", ex);
                }
            } else if ("mark_for_delivery".equals(selectedStatus)) {
                markOrderForDelivery();
            }
        });

        return panel;
    }

    
    private Component createDeliveryHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        JLabel headerLabel = new JLabel("Delivery History");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Refresh button
        JButton refreshButton = new JButton("Refresh History");
        styleButton(refreshButton);
        refreshButton.setFont(MAIN_FONT);
        refreshButton.addActionListener(e -> refreshDeliveryHistory());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(BACKGROUND_COLOR);

        // Table Setup
        historyTableModel = new DefaultTableModel(HISTORY_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setBackground(ACCENT_COLOR);
        historyTable.setForeground(TEXT_COLOR);
        historyTable.setGridColor(new Color(40, 40, 40)); // Subtle grid color
        historyTable.setFont(MAIN_FONT);

        // Table Header Styling
        historyTable.getTableHeader().setBackground(ACCENT_COLOR);
        historyTable.getTableHeader().setForeground(TEXT_COLOR);
        historyTable.getTableHeader().setFont(MAIN_FONT);

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        refreshDeliveryHistory();
        return panel;
    }
    
    private void refreshCurrentDelivery() {
        String sql = """
            SELECT o.order_id, o.pickup_location, o.delivery_location, 
                   o.status, da.started_at, da.status as delivery_status
            FROM delivery_assignments da
            JOIN orders o ON da.order_id = o.order_id
            WHERE da.driver_id = ? AND da.status != 'completed'
            ORDER BY da.started_at DESC LIMIT 1
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Order ID: ").append(rs.getInt("order_id"))
                       .append("\n\nPickup Location: ").append(rs.getString("pickup_location"))
                       .append("\n\nDelivery Location: ").append(rs.getString("delivery_location"))
                       .append("\n\nOrder Status: ").append(rs.getString("status"))
                       .append("\n\nStarted At: ").append(rs.getTimestamp("started_at"))
                       .append("\n\nDelivery Status: ").append(rs.getString("delivery_status"));
                
                currentDeliveryDetails.setText(details.toString());
                statusLabel.setText("Current Status: " + rs.getString("delivery_status"));
            } else {
                currentDeliveryDetails.setText("No active delivery assignment");
                statusLabel.setText("Current Status: No Active Delivery");
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to refresh current delivery", e);
        }
    }
    
 // Modified refreshDeliveryHistory to only show completed orders
    private void refreshDeliveryHistory() {
        String sql = """
            SELECT o.order_id, o.pickup_location, o.delivery_location,
                   da.started_at, da.completed_at, da.status
            FROM delivery_assignments da
            JOIN orders o ON da.order_id = o.order_id
            WHERE da.driver_id = ? AND da.status = 'completed'
            ORDER BY da.completed_at DESC
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            
            historyTableModel.setRowCount(0);
            
            while (rs.next()) {
                historyTableModel.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("pickup_location"),
                    rs.getString("delivery_location"),
                    rs.getTimestamp("started_at"),
                    rs.getTimestamp("completed_at"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to refresh delivery history", e);
        }
    }
    
    // Modify verifyOTP to use the newly stored OTP in the orders table
    private void verifyOTP(String enteredOTP) {
        String sql = """
            SELECT o.order_id, o.delivery_otp 
            FROM delivery_assignments da
            JOIN orders o ON da.order_id = o.order_id
            WHERE da.driver_id = ? AND da.status != 'completed'
            LIMIT 1
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedOTP = rs.getString("delivery_otp");
                int orderId = rs.getInt("order_id");
                
                if (storedOTP != null && storedOTP.equals(enteredOTP)) {
                    markDeliveryComplete(orderId);
                    JOptionPane.showMessageDialog(this, 
                        "OTP verified successfully. Delivery marked as complete.");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid OTP. Please try again.", 
                        "OTP Verification Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No active delivery found for OTP verification.");
            }
        } catch (SQLException e) {
            handleDatabaseError("OTP verification failed", e);
        }
    }
    
    private void markDeliveryComplete(int orderId) {
        String sql = """
            UPDATE delivery_assignments da
            JOIN orders o ON da.order_id = o.order_id
            JOIN drivers d ON da.driver_id = d.driver_id
            SET da.status = 'completed',
                da.completed_at = ?,
                o.status = 'delivered',
                o.actual_delivery = ?,
                d.status = 'available'
            WHERE da.order_id = ? AND da.driver_id = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            stmt.setTimestamp(1, now);
            stmt.setTimestamp(2, now);
            stmt.setInt(3, orderId);
            stmt.setInt(4, driverId);
            
            stmt.executeUpdate();
            refreshCurrentDelivery();
            refreshDeliveryHistory();
        } catch (SQLException e) {
            handleDatabaseError("Failed to mark delivery as complete", e);
        } 
    }
    private void handleDatabaseError(String message, SQLException e) {
        System.err.println(message + ": " + e.getMessage());
        JOptionPane.showMessageDialog(this,
            message + "\nPlease try again or contact support if the problem persists.",
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    // Clean up resources when the panel is disposed
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    private void setupRefreshTimer() {
        // Refresh every 30 seconds
        refreshTimer = new Timer(30000, e -> {
            refreshCurrentDelivery();
            refreshDeliveryHistory();
        });
        refreshTimer.start();
    }
    private JButton createLogoutButton() {
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton); // Apply the purple button style

        logoutButton.addActionListener(e -> {
            // Clean up resources
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error closing database connection: " + ex.getMessage());
                }
            }

            // Switch to login panel
            Container container = SwingUtilities.getAncestorOfClass(JFrame.class, this);
            if (container instanceof JFrame) {
                JFrame frame = (JFrame) container;
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new LoginPanel3());
                frame.revalidate();
                frame.repaint();
            }
        });
        return logoutButton;
    }

//    private void updateDeliveryStatus(String newStatus) {
//        String sql = """
//            UPDATE delivery_assignments
//            SET status = ?
//            WHERE driver_id = ? AND status != 'completed'
//        """;
//        
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, newStatus);
//            stmt.setInt(2, driverId);
//            
//            int updated = stmt.executeUpdate();
//            if (updated > 0) {
//                JOptionPane.showMessageDialog(this, "Status updated successfully");
//                refreshCurrentDelivery();
//                refreshDeliveryHistory();
//            } else {
//                JOptionPane.showMessageDialog(this, "No active delivery to update");
//            }
//        } catch (SQLException e) {
//            handleDatabaseError("Failed to update delivery status", e);
//        }
//    }
    private String generateUniqueOTP() {
        SecureRandom random = new SecureRandom();
        String otp;
        do {
            // Generate a 6-digit OTP
            otp = String.format("%06d", random.nextInt(1000000));
        } while (generatedOTPs.contains(otp));
        
        // Limit OTP memory to prevent excessive memory usage
        if (generatedOTPs.size() > 1000) {
            generatedOTPs.clear();
        }
        
        generatedOTPs.add(otp);
        return otp;
    }
 // New method to update driver's current location
//    private void updateDriverLocation(String location) {
//        String sql = "UPDATE drivers SET current_location = ?, location_updated_at = CURRENT_TIMESTAMP WHERE driver_id = ?";
//        
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, location);
//            stmt.setInt(2, driverId);
//            
//            int updated = stmt.executeUpdate();
//            if (updated > 0) {
//                JOptionPane.showMessageDialog(this, "Location updated successfully");
//            } else {
//                JOptionPane.showMessageDialog(this, "Failed to update location");
//            }
//        } catch (SQLException ex) {
//            handleDatabaseError("Location update failed", ex);
//        }
//    }
    
    // New method to mark order for delivery and generate OTP
    private void markOrderForDelivery() {
        String findActiveOrderSql = """
            SELECT order_id FROM delivery_assignments 
            WHERE driver_id = ? AND status != 'completed' 
            LIMIT 1
        """;
        
        String updateOrderSql = """
            UPDATE orders 
            SET delivery_otp = ?, status = 'in_transit' 
            WHERE order_id = ?
        """;
        
        try (PreparedStatement findStmt = conn.prepareStatement(findActiveOrderSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateOrderSql)) {
            
            findStmt.setInt(1, driverId);
            ResultSet rs = findStmt.executeQuery();
            
            if (rs.next()) {
                int orderId = rs.getInt("order_id");
                String otp = generateUniqueOTP();
                
                updateStmt.setString(1, otp);
                updateStmt.setInt(2, orderId);
                
                int updated = updateStmt.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "OTP generated. Verify OTP for order completion: " + otp);
                    refreshCurrentDelivery();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to mark order for delivery");
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No active delivery assignment found");
            }
        } catch (SQLException ex) {
            handleDatabaseError("Marking delivery failed", ex);
        }
    }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Driver Panel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);

                // Mock User instance (replace with actual user data as needed)
                User mockUser = new User(25, "John Doe", "driver","a","98");

                // Initialize DriverPanel
                DriverPanel driverPanel = new DriverPanel(mockUser);

                frame.getContentPane().add(driverPanel);
                frame.setVisible(true);
            });
        }
    }