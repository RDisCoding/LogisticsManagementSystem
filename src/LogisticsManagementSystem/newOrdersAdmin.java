package LogisticsManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class newOrdersAdmin extends JPanel {
    // UI Components
    private JPanel currentOrdersPanel;
    private JScrollPane currentOrdersScroll;
    private JScrollPane orderHistoryScroll;
    private User currentUser;
    
    // UI Colors (matching LoginPanel3)
    private static final Color DARK_THEME = new Color(18, 18, 18);
    private static final Color ACCENT_COLOR = new Color(255, 255, 255);
    private static final Color PURPLE_ACCENT = new Color(138, 43, 226);
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR2 = new Color(136, 46, 224);
    private static final Color HOVER_COLOR = new Color(48, 54, 61);

    public newOrdersAdmin(User user) {
        this.currentUser = user;
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
     // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JLabel headerLabel = new JLabel("New Orders");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

     // Refresh Button
        JButton refreshButton = createRefreshButton();

     // Create a panel to hold the refresh button and align it
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(refreshButton);

        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Current Orders Section
        currentOrdersPanel = new JPanel();
        currentOrdersPanel.setLayout(new BoxLayout(currentOrdersPanel, BoxLayout.Y_AXIS));
        currentOrdersPanel.setBackground(BACKGROUND_COLOR);

        // Wrap the currentOrdersPanel in a JScrollPane that extends to full height
        currentOrdersScroll = new JScrollPane(currentOrdersPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        currentOrdersScroll.setBorder(null);
        currentOrdersScroll.setBackground(BACKGROUND_COLOR);
        currentOrdersScroll.getViewport().setBackground(BACKGROUND_COLOR);

        add(currentOrdersScroll, BorderLayout.CENTER);
    }

 // New method to create styled refresh button
    private JButton createRefreshButton() {
        JButton refreshButton = new JButton("Refresh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                  RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(PURPLE_ACCENT.darker());
                } else {
                    g2.setColor(PURPLE_ACCENT);
                }
                
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2.setColor(ACCENT_COLOR);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setForeground(ACCENT_COLOR);
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.setToolTipText("Refresh Orders");

        // Add action listener to reload orders
        refreshButton.addActionListener(e -> {
            try {
                // Simulate loading animation (optional)
                refreshButton.setEnabled(false);
                refreshButton.setText("Refreshing...");

                // Reload orders
                loadOrders();

                // Reset button after loading
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setText("Refresh");
                    refreshButton.setEnabled(true);
                });
            } catch (Exception ex) {
                showError("Failed to refresh orders: " + ex.getMessage());
                refreshButton.setText("Refresh");
                refreshButton.setEnabled(true);
            }
        });

        return refreshButton;
    }
    
    private void loadOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadCurrentOrders(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    private void loadCurrentOrders(Connection conn) throws SQLException {
        currentOrdersPanel.removeAll();
        String query = """
        SELECT o.*, u.username as client_name
        FROM orders o
        JOIN users u ON o.client_id = u.user_id
        WHERE o.status = 'pending'
        ORDER BY o.created_at DESC
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderPanel orderPanel = new OrderPanel(
                    rs.getInt("order_id"),
                    rs.getString("item_type"),
                    rs.getDouble("quantity"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("estimated_delivery"),
                    rs.getString("pickup_location"),
                    rs.getString("delivery_location"),
                    rs.getBoolean("is_vip"),
                    false
                );
                currentOrdersPanel.add(orderPanel);
                currentOrdersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        if (currentOrdersPanel.getComponentCount() == 0) {
            addNoOrdersMessage(currentOrdersPanel, "No pending orders found");
        }

        currentOrdersPanel.revalidate();
        currentOrdersPanel.repaint();
    }

    
    private void addNoOrdersMessage(JPanel panel, String message) {
        JLabel noOrdersLabel = new JLabel(message);
        noOrdersLabel.setForeground(TEXT_COLOR);
        noOrdersLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        noOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noOrdersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(noOrdersLabel);
    }

    private class OrderPanel extends JPanel {
    	public OrderPanel(int orderId, String itemType, double quantity, 
                String status, Timestamp createdAt, Timestamp deliveryTime,
                String pickup, String delivery, boolean isVip, boolean isCompleted) {
    		
	    		// Set a fixed preferred size for the order card
	            setPreferredSize(new Dimension(400, 300));
	            setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
    		
			   setLayout(new BorderLayout(10, 10));
			   setBackground(CARD_BACKGROUND);
			   setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            	// Main info panel
			   JPanel infoPanel = new JPanel(new GridBagLayout());
			   infoPanel.setBackground(CARD_BACKGROUND);
			   GridBagConstraints gbc = new GridBagConstraints();
			   gbc.anchor = GridBagConstraints.WEST;
			   gbc.insets = new Insets(2, 5, 2, 5);
   
			   // Format timestamps
			   SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			   String createDateStr = dateFormat.format(createdAt);
			   String deliveryDateStr = deliveryTime != null ? dateFormat.format(deliveryTime) : "Pending";

			// Add order details
	            int row = 0;
	            addDetailRow(infoPanel, gbc, row++, "Order ID:", String.valueOf(orderId));
	            addDetailRow(infoPanel, gbc, row++, "Item Type:", itemType);
	            addDetailRow(infoPanel, gbc, row++, "Quantity:", String.format("%.2f", quantity));
//	            addDetailRow(infoPanel, gbc, row++, "Status:", formatStatus(status));
	            addDetailRow(infoPanel, gbc, row++, "Created:", createDateStr);
	            
	            if (isCompleted) {
	                addDetailRow(infoPanel, gbc, row++, "Delivered:", deliveryDateStr);
	            } else {
	                addDetailRow(infoPanel, gbc, row++, "Est. Delivery:", deliveryDateStr);
	            }
	            
	            addDetailRow(infoPanel, gbc, row++, "Pickup:", pickup);
	            addDetailRow(infoPanel, gbc, row++, "Delivery:", delivery);
	            
	            if (isVip) {
	                JLabel vipLabel = new JLabel("VIP");
	                vipLabel.setForeground(new Color(255, 215, 0)); // Gold color
	                vipLabel.setFont(new Font("Arial", Font.BOLD, 12));
	                gbc.gridy = row++;
	                gbc.gridx = 0;
	                gbc.gridwidth = 2;
	                infoPanel.add(vipLabel, gbc);
	            }

	            add(infoPanel, BorderLayout.CENTER);

            // Button panel
	            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	            buttonPanel.setBackground(CARD_BACKGROUND);

	            if (isCompleted) {
	                JButton viewBillButton = createStyledButton("View Bill");
	                viewBillButton.addActionListener(e -> handleViewBill(orderId));
	                buttonPanel.add(viewBillButton);
	            } else {    
	                JButton acceptButton = createStyledButton("Accept");
	                acceptButton.setBackground(new Color(0, 128, 0)); // Green color
	                acceptButton.addActionListener(e -> handleAcceptOrder(orderId));
	                
	                JButton rejectButton = createStyledButton("Reject");
	                rejectButton.setBackground(new Color(255, 0, 0)); // Red color
	                rejectButton.addActionListener(e -> handleRejectOrder(orderId));
	                
	                buttonPanel.add(acceptButton);
	                buttonPanel.add(rejectButton);
	            }

	            add(buttonPanel, BorderLayout.EAST);
	        }

        private void addDetailRow(JPanel panel, GridBagConstraints gbc, 
                                int row, String label, String value) {
            gbc.gridy = row;
            gbc.gridx = 0;
            JLabel labelComp = new JLabel(label);
            labelComp.setForeground(TEXT_COLOR);
            panel.add(labelComp, gbc);

            gbc.gridx = 1;
            JLabel valueComp = new JLabel(value);
            valueComp.setForeground(ACCENT_COLOR);
            panel.add(valueComp, gbc);
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                      RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (getModel().isPressed()) {
                        g2.setColor(PURPLE_ACCENT.darker());
                    } else {
                        g2.setColor(PURPLE_ACCENT);
                    }
                    
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    
                    g2.setColor(ACCENT_COLOR);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            button.setPreferredSize(new Dimension(120, 35));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setForeground(ACCENT_COLOR);
            return button;
        }
    }

    private void handleAcceptOrder(int orderId) {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to accept this order?", 
            "Confirm Order Acceptance", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                List<DriverInfo> availableDrivers = getAvailableDrivers(conn);
                
                if (availableDrivers.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this, 
                        "No available drivers at the moment.", 
                        "Driver Assignment Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                DriverInfo selectedDriver = (DriverInfo) JOptionPane.showInputDialog(
                    this,
                    "Select a driver for this delivery:",
                    "Assign Driver",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    availableDrivers.toArray(),
                    availableDrivers.get(0)
                );
                
                if (selectedDriver != null) {
                    String updateOrderQuery = "UPDATE orders SET status = 'assigned', driver_id = ? WHERE order_id = ?";
                    String updateDriverQuery = "UPDATE drivers SET status = 'busy' WHERE driver_id = ?";
                    String createAssignmentQuery = "INSERT INTO delivery_assignments (order_id, driver_id, started_at, status) VALUES (?, ?, NOW(), 'assigned')";
                    
                    conn.setAutoCommit(false);
                    try (PreparedStatement orderStmt = conn.prepareStatement(updateOrderQuery);
                         PreparedStatement driverStmt = conn.prepareStatement(updateDriverQuery);
                         PreparedStatement assignmentStmt = conn.prepareStatement(createAssignmentQuery)) {
                        
                        // Update order status and driver
                        orderStmt.setInt(1, selectedDriver.getDriverId());
                        orderStmt.setInt(2, orderId);
                        orderStmt.executeUpdate();
                        
                        // Update driver status
                        driverStmt.setInt(1, selectedDriver.getDriverId());
                        driverStmt.executeUpdate();
                        
                        // Create delivery assignment
                        assignmentStmt.setInt(1, orderId);
                        assignmentStmt.setInt(2, selectedDriver.getDriverId());
                        assignmentStmt.executeUpdate();
                        
                        conn.commit();
                        
                        JOptionPane.showMessageDialog(
                            this, 
                            "Order " + orderId + " accepted and assigned to Driver ID: " + selectedDriver.getDriverId(), 
                            "Order Confirmed", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        loadOrders();
                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Failed to process order: " + e.getMessage());
            }
        }
    }

    private List<DriverInfo> getAvailableDrivers(Connection conn) throws SQLException {
        List<DriverInfo> drivers = new ArrayList<>();
        String query = "SELECT driver_id FROM drivers WHERE status = 'available'";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                drivers.add(new DriverInfo(rs.getInt("driver_id")));
            }
        }
        
        return drivers;
    }

    private class DriverInfo {
        private int driverId;
        
        public DriverInfo(int driverId) {
            this.driverId = driverId;
        }
        
        public int getDriverId() {
            return driverId;
        }
        
        @Override
        public String toString() {
            return "Driver ID: " + driverId;
        }
    }
    
    private void handleRejectOrder(int orderId) {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to reject this order?", 
            "Confirm Order Rejection", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Create a custom dialog for rejection reason
            JPanel panel = new JPanel(new BorderLayout());
            JTextArea reasonTextArea = new JTextArea(5, 30);
            reasonTextArea.setLineWrap(true);
            reasonTextArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(reasonTextArea);
            
            panel.add(new JLabel("Please provide a reason for rejecting the order:"), BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            int result = JOptionPane.showConfirmDialog(
                this, 
                panel, 
                "Reject Order", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result == JOptionPane.OK_OPTION) {
                String rejectionReason = reasonTextArea.getText().trim();
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String updateQuery = "UPDATE orders SET status = 'rejected', rejection_reason = ? WHERE order_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                        pstmt.setString(1, rejectionReason);
                        pstmt.setInt(2, orderId);
                        pstmt.executeUpdate();
                        
                        JOptionPane.showMessageDialog(
                            this, 
                            "Order " + orderId + " has been rejected.", 
                            "Order Rejected", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Refresh the orders list
                        loadOrders();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Failed to reject order: " + e.getMessage());
                }
            }
        }}

    private void handleTrackOrder(int orderId) {
        // Implement order tracking functionality
        JOptionPane.showMessageDialog(this, 
            "Tracking order: " + orderId + "\nThis feature will be implemented soon.",
            "Track Order",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleViewBill(int orderId) {
        // Implement bill viewing functionality
        JOptionPane.showMessageDialog(this,
            "Viewing bill for order: " + orderId + "\nThis feature will be implemented soon.",
            "View Bill",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    
}