package LogisticsManagementSystem;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class completedOrdersAdmin extends JPanel {
    // Logging setup
    private static final Logger LOGGER = Logger.getLogger(completedOrdersAdmin.class.getName());
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
    private static final Color PENDING_COLOR = new Color(255, 165, 0); // Orange for pending status
    private static final Color COMPLETED_COLOR = new Color(46, 204, 113); // Green for completed status


    public completedOrdersAdmin(User user) {
        this.currentUser = user;
        try {
            initComponents();
            loadOrders();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    private void handleInitializationError(Exception e) {
        LOGGER.log(Level.SEVERE, "Failed to initialize CompletedOrdersAdmin", e);
        JOptionPane.showMessageDialog(this, 
            "Unable to load Completed Orders: " + e.getMessage(), 
            "Initialization Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
     // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Header
        JLabel headerLabel = new JLabel("Completed Orders");
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
        
        // Main content panel with current orders
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);

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
            handleDatabaseError("Failed to load orders", e);
        }
    }

    private void loadCurrentOrders(Connection conn) {
        try {
            currentOrdersPanel.removeAll();
            String query = """
            SELECT o.*, u.username as client_name, o.payment_status, o.total_amount
            FROM orders o
            JOIN users u ON o.client_id = u.user_id
            WHERE o.status = 'delivered'
            ORDER BY o.created_at DESC
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                boolean hasOrders = false;
                while (rs.next()) {
                    try {
                        OrderPanel orderPanel = createOrderPanel(rs);
                        currentOrdersPanel.add(orderPanel);
                        currentOrdersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                        hasOrders = true;
                    } catch (SQLException orderException) {
                        LOGGER.log(Level.WARNING, "Error processing individual order", orderException);
                        // Continue processing other orders
                    }
                }

                if (!hasOrders) {
                    addNoOrdersMessage(currentOrdersPanel, "No completed orders found");
                }
            }

            currentOrdersPanel.revalidate();
            currentOrdersPanel.repaint();

        } catch (SQLException e) {
            handleDatabaseError("Error loading current orders", e);
        }
    }
    
    private OrderPanel createOrderPanel(ResultSet rs) throws SQLException {
        return new OrderPanel(
            rs.getInt("order_id"),
            rs.getString("item_type"),
            rs.getDouble("quantity"),
            rs.getString("status"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("estimated_delivery"),
            rs.getString("pickup_location"),
            rs.getString("delivery_location"),
            rs.getBoolean("is_vip"),
            rs.getString("payment_status"),
            rs.getDouble("total_amount"),
            rs.getString("client_name")
        );
    }

    private void handleDatabaseError(String message, SQLException e) {
        LOGGER.log(Level.SEVERE, message, e);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                message + ": " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            addNoOrdersMessage(currentOrdersPanel, "Unable to load orders");
        });
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
                String pickup, String delivery, boolean isVip, 
                String paymentStatus, double totalAmount, String clientName) {
        	
        	 // Set a fixed preferred size for the order card
            setPreferredSize(new Dimension(400, 300));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
            
            setLayout(new BorderLayout(10, 10));
            setBackground(CARD_BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBackground(CARD_BACKGROUND);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 5, 2, 5);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String createDateStr = dateFormat.format(createdAt);
            String deliveryDateStr = dateFormat.format(deliveryTime);

            int row = 0;
            addDetailRow(infoPanel, gbc, row++, "Order ID:", String.valueOf(orderId));
            addDetailRow(infoPanel, gbc, row++, "Client:", clientName);
            addDetailRow(infoPanel, gbc, row++, "Item Type:", itemType);
            addDetailRow(infoPanel, gbc, row++, "Quantity:", String.format("%.2f", quantity));
            addDetailRow(infoPanel, gbc, row++, "Total Amount:", String.format("$%.2f", totalAmount));
            addDetailRow(infoPanel, gbc, row++, "Created:", createDateStr);
            addDetailRow(infoPanel, gbc, row++, "Delivered:", deliveryDateStr);
            addDetailRow(infoPanel, gbc, row++, "Pickup:", pickup);
            addDetailRow(infoPanel, gbc, row++, "Delivery:", delivery);

            // Payment Status Label with dynamic color
            JLabel paymentStatusLabel = new JLabel("Payment Status: " + paymentStatus);
            paymentStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            paymentStatusLabel.setForeground(paymentStatus.equalsIgnoreCase("PENDING") ? 
                                          PENDING_COLOR : COMPLETED_COLOR);
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            infoPanel.add(paymentStatusLabel, gbc);

            if (isVip) {
                JLabel vipLabel = new JLabel("VIP");
                vipLabel.setForeground(new Color(255, 215, 0));
                vipLabel.setFont(new Font("Arial", Font.BOLD, 12));
                gbc.gridy = row++;
                infoPanel.add(vipLabel, gbc);
            }

            add(infoPanel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(CARD_BACKGROUND);

            // Conditional button display
            if (paymentStatus.equalsIgnoreCase("COMPLETED")) {
                // Check if bill has been generated
                boolean billGenerated = checkBillGenerationStatus(orderId);
                
                if (!billGenerated) {
                    JButton generateBillButton = createStyledButton("Generate Bill");
                    generateBillButton.addActionListener(e -> generateBill(orderId, totalAmount, clientName));
                    buttonPanel.add(generateBillButton);
                }
            }

            add(buttonPanel, BorderLayout.EAST);
        }
        
//        private boolean checkBillGenerationStatus(int orderId) {
//            try (Connection conn = DatabaseConnection.getConnection()) {
//                String query = "SELECT bill_generated FROM orders WHERE order_id = ?";
//                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
//                    pstmt.setInt(1, orderId);
//                    ResultSet rs = pstmt.executeQuery();
//                    return rs.next() && rs.getBoolean("bill_generated");
//                }
//            } catch (SQLException e) {
//                showError("Failed to check bill status: " + e.getMessage());
//                return false;
//            }
//        }
        
        
            private void generateBill(int orderId, double totalAmount, String clientName) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String billContent = createBillContent(orderId, clientName, totalAmount, sdf);

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        saveBillToDatabase(conn, orderId, billContent);
                        showBillGenerationSuccess();
                        revalidate();
                        repaint();
                    } catch (SQLException e) {
                        handleBillGenerationError("Failed to save bill to database", e);
                    }
                } catch (Exception e) {
                    handleBillGenerationError("Failed to generate bill", e);
                }
            }

            private String createBillContent(int orderId, String clientName, 
                                             double totalAmount, SimpleDateFormat sdf) {
                return String.format(
                    "LOGISTICS MANAGEMENT SYSTEM\n" +
                    "BILL INVOICE\n\n" +
                    "Order ID: %d\n" +
                    "Client Name: %s\n" +
                    "Bill Generation Date: %s\n\n" +
                    "Total Amount: $%.2f\n" +
                    "Status: Paid\n\n" +
                    "Thank you for your business!",
                    orderId, 
                    clientName, 
                    sdf.format(new Timestamp(System.currentTimeMillis())), 
                    totalAmount
                );
            }
            
            private void saveBillToDatabase(Connection conn, int orderId, String billContent) throws SQLException {
                String updateQuery = "UPDATE orders SET bill_generated = true, bill_data = ? WHERE order_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, billContent);
                    pstmt.setInt(2, orderId);
                    pstmt.executeUpdate();
                }
            }

            private void showBillGenerationSuccess() {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, 
                        "Bill generated successfully!", 
                        "Bill Generation", 
                        JOptionPane.INFORMATION_MESSAGE)
                );
            }

            private void handleBillGenerationError(String message, Exception e) {
                LOGGER.log(Level.SEVERE, message, e);
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, 
                        message + ": " + e.getMessage(), 
                        "Bill Generation Error", 
                        JOptionPane.ERROR_MESSAGE)
                );
            }
            
            private boolean checkBillGenerationStatus(int orderId) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return checkBillStatus(conn, orderId);
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to check bill status", e);
                    return false;
                }
            }

            private boolean checkBillStatus(Connection conn, int orderId) throws SQLException {
                String query = "SELECT bill_generated FROM orders WHERE order_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, orderId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        return rs.next() && rs.getBoolean("bill_generated");
                    }
                }
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
    
    private void handleGenerateBill(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT o.*, u.username, u.email, u.phone
                FROM orders o
                JOIN users u ON o.client_id = u.user_id
                WHERE o.order_id = ?
            """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    generateInvoicePDF(rs);
                }
            }
        } catch (SQLException e) {
            showError("Failed to generate bill: " + e.getMessage());
        }
    }

    private void generateInvoicePDF(ResultSet orderData) {
        try {
            // Create invoice content
            StringBuilder invoice = new StringBuilder();
            invoice.append("INVOICE\n\n");
            invoice.append("Order ID: ").append(orderData.getInt("order_id")).append("\n");
            invoice.append("Date: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(orderData.getTimestamp("created_at"))).append("\n\n");
            
            invoice.append("Client Details:\n");
            invoice.append("Name: ").append(orderData.getString("username")).append("\n");
            invoice.append("Email: ").append(orderData.getString("email")).append("\n");
            invoice.append("Phone: ").append(orderData.getString("phone")).append("\n\n");
            
            invoice.append("Order Details:\n");
            invoice.append("Item Type: ").append(orderData.getString("item_type")).append("\n");
            invoice.append("Quantity: ").append(orderData.getDouble("quantity")).append("\n");
            invoice.append("Pickup Location: ").append(orderData.getString("pickup_location")).append("\n");
            invoice.append("Delivery Location: ").append(orderData.getString("delivery_location")).append("\n");
            invoice.append("Total Amount: $").append(String.format("%.2f", orderData.getDouble("total_amount"))).append("\n");

            // Show invoice in a dialog (in real implementation, this would generate a PDF)
            JTextArea textArea = new JTextArea(invoice.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Invoice", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            showError("Failed to generate invoice: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}