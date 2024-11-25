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

        // Header
        JLabel headerLabel = new JLabel("Order History");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        // Main content panel with current orders and history
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 0, 10));
        mainContent.setBackground(BACKGROUND_COLOR);

        // Order History Section
        JPanel orderHistorySection = new JPanel(new BorderLayout());
        orderHistorySection.setBackground(BACKGROUND_COLOR);
        JLabel historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        historyLabel.setForeground(TEXT_COLOR);
        orderHistorySection.add(historyLabel, BorderLayout.NORTH);

        orderHistoryPanel = new JPanel();
        orderHistoryPanel.setLayout(new BoxLayout(orderHistoryPanel, BoxLayout.Y_AXIS));
        orderHistoryPanel.setBackground(BACKGROUND_COLOR);
        orderHistoryScroll = new JScrollPane(orderHistoryPanel);
        orderHistoryScroll.setBorder(null);
        orderHistorySection.add(orderHistoryScroll, BorderLayout.CENTER);

        mainContent.add(orderHistorySection);

        add(mainContent, BorderLayout.CENTER);
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
            WHERE o.client_id = ? AND o.status = 'delivered' 
            ORDER BY o.actual_delivery DESC
            """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
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
                    true
                );
                orderHistoryPanel.add(orderPanel);
                orderHistoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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

	            if (isCompleted && !isPaid) {
	                actionButton = createStyledButton("Pay");
	                actionButton.addActionListener(e -> showPaymentDialog(orderId));
	                buttonPanel.add(actionButton);
	            } else if (isCompleted && isPaid) {
	                actionButton = createStyledButton("View Bill");
	                actionButton.addActionListener(e -> handleViewBill(orderId));
	                buttonPanel.add(actionButton);
	            } else {	
	                JButton trackButton = createStyledButton("Track Order");
	                trackButton.addActionListener(e -> handleTrackOrder(orderId));
	                buttonPanel.add(trackButton);
	            }

	            add(buttonPanel, BorderLayout.EAST);
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
	            double remainingAmount = calculateRemainingAmount(orderId);

	            JLabel amountLabel = new JLabel("Remaining Amount:");
	            amountLabel.setForeground(TEXT_COLOR);
	            JLabel amountValueLabel = new JLabel(String.format("$%.2f", remainingAmount));
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

	            JButton payButton = createStyledButton("Pay");
	            payButton.addActionListener(e -> {
	                // Simulate payment processing
	                isPaid = true;
	                JOptionPane.showMessageDialog(paymentDialog, 
	                    "Payment Successful!", 
	                    "Payment Confirmation", 
	                    JOptionPane.INFORMATION_MESSAGE);
	                
	                // Update button to View Bill
	                actionButton.setText("View Bill");
	                actionButton.removeActionListener(actionButton.getActionListeners()[0]);
	                actionButton.addActionListener(evt -> handleViewBill(orderId));
	                
	                paymentDialog.dispose();
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

	        private double calculateRemainingAmount(int orderId) {
	            // Placeholder method to calculate remaining amount
	            // In a real implementation, this would fetch from database
	            return 100.50; // Example remaining amount
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