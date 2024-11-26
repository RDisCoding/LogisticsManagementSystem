package LogisticsManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryPanel extends JPanel {
    // UI Components
    private JPanel orderHistoryPanel;
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

    public OrderHistoryPanel(User user) {
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
        JLabel headerLabel = new JLabel("Order History");
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

        // Main content panel with current orders and history
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 0, 10));
        mainContent.setBackground(BACKGROUND_COLOR);

     // Main content panel for order history
        orderHistoryPanel = new JPanel();
        orderHistoryPanel.setLayout(new BoxLayout(orderHistoryPanel, BoxLayout.Y_AXIS));
        orderHistoryPanel.setBackground(BACKGROUND_COLOR);

        // Create a scroll pane that takes up the entire remaining space
        orderHistoryScroll = new JScrollPane(orderHistoryPanel);
        orderHistoryScroll.setBorder(null);
        orderHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        orderHistoryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(orderHistoryScroll, BorderLayout.CENTER);
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
            loadOrderHistory(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load orders: " + e.getMessage());
        }
    }
    
    private void loadOrderHistory(Connection conn) throws SQLException {
        orderHistoryPanel.removeAll();
        String query = """
            SELECT o.*, u.username as client_name 
            FROM orders o 
            JOIN users u ON o.client_id = u.user_id 
            WHERE o.client_id = ? AND (o.status = 'delivered' OR o.status = 'rejected') 
            ORDER BY o.actual_delivery DESC
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            boolean isFirst = true;
            while (rs.next()) {
            	
            	// Add some vertical spacing, but only after the first item
                if (!isFirst) {
                    orderHistoryPanel.add(Box.createVerticalStrut(20)); // 20-pixel vertical gap
                }
                
                boolean isRejected = rs.getString("status").equalsIgnoreCase("rejected");
                OrderPanel orderPanel = new OrderPanel(
                    rs.getInt("order_id"),
                    rs.getString("item_type"),
                    rs.getDouble("quantity"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("actual_delivery"),
                    rs.getString("pickup_location"),
                    rs.getString("delivery_location"),
                    rs.getBoolean("is_vip"),
                    !isRejected,
                    isRejected ? rs.getString("rejection_reason") : null
                );
             // Align the order panel to the center horizontally
                orderPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                orderHistoryPanel.add(orderPanel);
                isFirst = false;
            }
        }
        
        if (orderHistoryPanel.getComponentCount() == 0) {
            addNoOrdersMessage(orderHistoryPanel, "No order history found");
        }
        
        orderHistoryPanel.revalidate();
        orderHistoryPanel.repaint();
    }

    private void addNoOrdersMessage(JPanel panel, String message) {
        JLabel noOrdersLabel = new JLabel(message);
        noOrdersLabel.setForeground(TEXT_COLOR);
        noOrdersLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        noOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noOrdersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(noOrdersLabel);
    }
    
    private JButton actionButton;
    private boolean isPaid = false;

    public class OrderPanel extends JPanel {
    	public OrderPanel(int orderId, String itemType, double quantity, 
                String status, Timestamp createdAt, Timestamp deliveryTime,
                String pickup, String delivery, boolean isVip, 
                boolean isCompleted, String rejectionReason) {
    		
    		// Set a fixed preferred size for consistent card layout
            setPreferredSize(new Dimension(400, 300)); // Adjust dimensions as needed
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 300)); // Allow horizontal expansion
            setMinimumSize(new Dimension(300, 300)); // Minimum size to ensure readability
            
            
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

	         // Check payment and bill generation status
	            try (Connection conn = DatabaseConnection.getConnection()) {
	                String statusQuery = "SELECT payment_status, bill_generated FROM orders WHERE order_id = ?";
	                try (PreparedStatement pstmt = conn.prepareStatement(statusQuery)) {
	                    pstmt.setInt(1, orderId);
	                    ResultSet rs = pstmt.executeQuery();
	                    
	                    if (rs.next()) {
	                        String paymentStatus = rs.getString("payment_status");
	                        boolean billGenerated = rs.getBoolean("bill_generated");

	                        if (isCompleted && paymentStatus.equals("PENDING")) {
	                            actionButton = createStyledButton("Pay");
	                            actionButton.addActionListener(e -> showPaymentDialog(orderId));
	                            buttonPanel.add(actionButton);
	                        } else if (isCompleted && paymentStatus.equals("COMPLETED") && billGenerated) {
	                            actionButton = createStyledButton("View Bill");
	                            actionButton.addActionListener(e -> handleViewBill(orderId));
	                            buttonPanel.add(actionButton);
	                        } else if (!isCompleted && rejectionReason != null) {
	                            actionButton = createStyledButton("View Reason");
	                            actionButton.addActionListener(e -> showRejectionReasonDialog(rejectionReason));
	                            buttonPanel.add(actionButton);
	                        }
	                    }
	                }
	            } catch (SQLException e) {
	                showError("Failed to check order status: " + e.getMessage());
	            }

	            add(buttonPanel, BorderLayout.EAST);
	        }
    	
    	private void showRejectionReasonDialog(String rejectionReason) {
            JTextArea reasonTextArea = new JTextArea(rejectionReason);
            reasonTextArea.setEditable(false);
            reasonTextArea.setLineWrap(true);
            reasonTextArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(reasonTextArea);
            scrollPane.setPreferredSize(new Dimension(300, 200));
            
            JOptionPane.showMessageDialog(
                this, 
                scrollPane, 
                "Rejection Reason", 
                JOptionPane.PLAIN_MESSAGE
            );
        }

	        private void showPaymentDialog(int orderId) {
	            JDialog paymentDialog = new JDialog();
	            paymentDialog.setTitle("Pay Remaining Charges");
	            paymentDialog.setModal(true);
	            paymentDialog.setLayout(new BorderLayout(10, 10));
	            paymentDialog.setPreferredSize(new Dimension(350, 200));

	            // Payment amount panel
	            JPanel amountPanel = new JPanel(new GridBagLayout());
	            amountPanel.setBackground(CARD_BACKGROUND);
	            GridBagConstraints gbc = new GridBagConstraints();
	            gbc.insets = new Insets(5, 5, 5, 5);

	            // Calculate or fetch remaining amount (placeholder)
	            double totalAmount = fetchTotalAmount(orderId);

	            JLabel amountLabel = new JLabel("Remaining Amount:");
	            amountLabel.setForeground(TEXT_COLOR);
	            JLabel amountValueLabel = new JLabel(String.format("$%.2f", totalAmount));
	            amountValueLabel.setForeground(ACCENT_COLOR);

	            gbc.gridx = 0;
	            gbc.gridy = 0;
	            amountPanel.add(amountLabel, gbc);
	            gbc.gridx = 1;
	            amountPanel.add(amountValueLabel, gbc);

	            paymentDialog.add(amountPanel, BorderLayout.CENTER);

	            // Payment buttons
	            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	            buttonPanel.setBackground(CARD_BACKGROUND);

	            JButton payButton = createStyledButton("Confirm Payment");
	            payButton.addActionListener(e -> {
	                try (Connection conn = DatabaseConnection.getConnection()) {
	                    // Update payment status in the bills table
	                    String updateBillQuery = """
	                        UPDATE bills 
	                        SET status = 'paid', paid_at = CURRENT_TIMESTAMP 
	                        WHERE order_id = ? AND status != 'cancelled'
	                    """;
	                    try (PreparedStatement pstmt = conn.prepareStatement(updateBillQuery)) {
	                        pstmt.setInt(1, orderId);
	                        int rowsUpdated = pstmt.executeUpdate();
	                        
	                        if (rowsUpdated > 0) {
	                            JOptionPane.showMessageDialog(paymentDialog, 
	                                "Payment Successful! Bill status updated to 'Paid'.", 
	                                "Payment Confirmation", 
	                                JOptionPane.INFORMATION_MESSAGE);
	                        } else {
	                            JOptionPane.showMessageDialog(paymentDialog, 
	                                "No valid bill found to update. Please check the order.", 
	                                "Payment Update Failed", 
	                                JOptionPane.WARNING_MESSAGE);
	                        }
	                    }
	                    
	                    // Keep the existing action: update orders table
	                    String updateOrderQuery = "UPDATE orders SET payment_status = 'COMPLETED' WHERE order_id = ?";
	                    try (PreparedStatement pstmt = conn.prepareStatement(updateOrderQuery)) {
	                        pstmt.setInt(1, orderId);
	                        pstmt.executeUpdate();
	                    }
	                } catch (SQLException ex) {
	                    showError("Payment update failed: " + ex.getMessage());
	                }
	                
	                // Close the payment dialog
	                paymentDialog.dispose();
	                
	                // Refresh the panel to reflect updated state
	                revalidate();
	                repaint();
	            });



	            JButton cancelButton = createStyledButton("Cancel");
	            cancelButton.addActionListener(e -> paymentDialog.dispose());

	            buttonPanel.add(payButton);
	            buttonPanel.add(cancelButton);

	            paymentDialog.add(buttonPanel, BorderLayout.SOUTH);

	            paymentDialog.pack();
	            paymentDialog.setLocationRelativeTo(this);
	            paymentDialog.setVisible(true);
	        }

	        private double fetchTotalAmount(int orderId) {
	            try (Connection conn = DatabaseConnection.getConnection()) {
	                String query = "SELECT total_amount FROM orders WHERE order_id = ?";
	                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                    pstmt.setInt(1, orderId);
	                    ResultSet rs = pstmt.executeQuery();
	                    return rs.next() ? rs.getDouble("total_amount")*(0.5) : 0.0;
	                }
	            } catch (SQLException e) {
	                showError("Failed to fetch total amount: " + e.getMessage());
	                return 0.0;
	            }
	        }

	        private String formatStatus(String status) {
	            return status.substring(0, 1).toUpperCase() + 
	                   status.substring(1).toLowerCase().replace('_', ' ');
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

    private void handleViewBill(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT bill_id, order_id, advance_amount, vip_charges, total_amount, 
                       generated_at, status, paid_at 
                FROM bills 
                WHERE order_id = ? AND status != 'cancelled'
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Retrieve data from the result set
                    int billId = rs.getInt("bill_id");
                    int fetchedOrderId = rs.getInt("order_id");
                    double advanceAmount = rs.getDouble("advance_amount");
                    double vipCharges = rs.getDouble("vip_charges");
                    double totalAmount = rs.getDouble("total_amount");
                    Timestamp generatedAt = rs.getTimestamp("generated_at");
                    String status = rs.getString("status");
                    Timestamp paidAt = rs.getTimestamp("paid_at");
                    
                    // Format the bill details for display
                    StringBuilder billContent = new StringBuilder();
                    billContent.append("Bill ID: ").append(billId).append("\n")
                               .append("Order ID: ").append(fetchedOrderId).append("\n")
                               .append("Advance Amount: ₹").append(advanceAmount).append("\n")
                               .append("VIP Charges: ₹").append(vipCharges).append("\n")
                               .append("Total Amount: ₹").append(totalAmount).append("\n")
                               .append("Generated At: ").append(generatedAt).append("\n")
                               .append("Status: ").append(status).append("\n")
                               .append("Paid At: ").append(paidAt == null ? "Not Paid" : paidAt).append("\n");
                    
                    // Display the bill in a dialog
                    displayBillDialog(billContent.toString());
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No valid bill found for the given Order ID.", 
                        "Bill Unavailable", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            showError("Failed to retrieve bill: " + e.getMessage());
        }
    }

    private void displayBillDialog(String billContent) {
        JTextArea billTextArea = new JTextArea(billContent);
        billTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(billTextArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(
            this, 
            scrollPane, 
            "Order Bill", 
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}