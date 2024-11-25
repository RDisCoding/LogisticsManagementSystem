package LogisticsManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class currentOrdersAdmin extends JPanel {
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

    public currentOrdersAdmin(User user) {
        this.currentUser = user;
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Current Orders");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        // Main content panel with current orders
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);

        // Current Orders Section
        currentOrdersPanel = new JPanel();
        currentOrdersPanel.setLayout(new BoxLayout(currentOrdersPanel, BoxLayout.Y_AXIS));
        currentOrdersPanel.setBackground(BACKGROUND_COLOR);

        // Wrap currentOrdersPanel in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(currentOrdersPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Optional: To match the clean border style

        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
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
        WHERE o.status = 'assigned'
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
	            addDetailRow(infoPanel, gbc, row++, "Status:", formatStatus(status));
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
	                JButton trackButton = createStyledButton("Track Order");
	                trackButton.addActionListener(e -> handleTrackOrder(orderId));
	                buttonPanel.add(trackButton);
	            }

	            add(buttonPanel, BorderLayout.EAST);
	        }

	        private String formatStatus(String status) {
	            return status.substring(0, 1).toUpperCase() + 
	                   status.substring(1).toLowerCase().replace('_', ' ');
	        }
	        
	        private void handleTrackOrder(int orderId) {
	            try (Connection conn = DatabaseConnection.getConnection()) {
	                String query = """
	                    SELECT 
	                        d.driver_id, 
	                        u.username as driver_name, 
	                        d.current_location, 
	                        d.location_updated_at
	                    FROM 
	                        orders o
	                    JOIN 
	                        drivers d ON o.driver_id = d.driver_id
	                    JOIN 
	                        users u ON d.user_id = u.user_id
	                    WHERE 
	                        o.order_id = ?
	                """;
	                
	                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                    pstmt.setInt(1, orderId);
	                    
	                    try (ResultSet rs = pstmt.executeQuery()) {
	                        if (rs.next()) {
	                            int driverId = rs.getInt("driver_id");
	                            String driverName = rs.getString("driver_name");
	                            String currentLocation = rs.getString("current_location");
	                            Timestamp locationUpdatedAt = rs.getTimestamp("location_updated_at");
	                            
	                            // Create custom dialog
	                            JDialog trackDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
	                                                              "Order Tracking Details", true);
	                            trackDialog.setLayout(new GridBagLayout());
	                            trackDialog.getContentPane().setBackground(BACKGROUND_COLOR);
	                            
	                            GridBagConstraints gbc = new GridBagConstraints();
	                            gbc.insets = new Insets(10, 10, 10, 10);
	                            gbc.anchor = GridBagConstraints.WEST;
	                            
	                            // Format location update time
	                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	                            String updatedTimeStr = dateFormat.format(locationUpdatedAt);
	                            
	                            // Create and style tracking details
	                            JLabel[] labels = {
	                                createTrackingLabel("Order ID: " + orderId),
	                                createTrackingLabel("Driver ID: " + driverId),
	                                createTrackingLabel("Driver Name: " + driverName),
	                                createTrackingLabel("Current Location: " + currentLocation),
	                                createTrackingLabel("Location Updated: " + updatedTimeStr)
	                            };
	                            
	                            // Add labels to dialog
	                            for (int i = 0; i < labels.length; i++) {
	                                gbc.gridy = i;
	                                trackDialog.add(labels[i], gbc);
	                            }
	                            
	                            // Close button
	                            JButton closeButton = createCloseButton(trackDialog);
	                            gbc.gridy = labels.length;
	                            gbc.anchor = GridBagConstraints.CENTER;
	                            trackDialog.add(closeButton, gbc);
	                            
	                            trackDialog.pack();
	                            trackDialog.setLocationRelativeTo(this);
	                            trackDialog.setVisible(true);
	                        } else {
	                            showError("No driver details found for this order.");
	                        }
	                    }
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	                showError("Failed to retrieve driver details: " + e.getMessage());
	            }
	        }
	        
	        private JLabel createTrackingLabel(String text) {
	            JLabel label = new JLabel(text);
	            label.setForeground(TEXT_COLOR);
	            label.setFont(new Font("Arial", Font.PLAIN, 14));
	            return label;
	        }

	        private JButton createCloseButton(JDialog dialog) {
	            JButton closeButton = new JButton("Close") {
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
	            
	            closeButton.setPreferredSize(new Dimension(100, 35));
	            closeButton.setFocusPainted(false);
	            closeButton.setBorderPainted(false);
	            closeButton.setContentAreaFilled(false);
	            closeButton.addActionListener(e -> dialog.dispose());
	            
	            return closeButton;
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