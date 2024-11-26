package LogisticsManagementSystem;

import java.sql.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientPortal {
    private static final Logger LOGGER = Logger.getLogger(ClientPortal.class.getName());
    private Connection conn;

    public ClientPortal() throws SQLException {
        try {
            this.conn = DatabaseConnection.getConnection();
            // Verify connection is not null and is valid
            if (conn == null || !conn.isValid(5)) {
                throw new SQLException("Invalid database connection");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e; // Rethrow to allow caller to handle
        }
    }

    /**
     * Creates a new order in the system
     * @return Order ID or -1 if order creation fails
     */
    public int createOrder(int clientId, String pickupLocation, String deliveryLocation, 
                          String itemType, double quantity, boolean isVip) {
        String sql = "INSERT INTO orders (client_id, pickup_location, delivery_location, " +
                    "item_type, quantity, is_vip, status, estimated_delivery) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'pending', DATE_ADD(NOW(), INTERVAL 2 DAY))";
        
        try {
            // Validate input parameters
            validateOrderInput(clientId, pickupLocation, deliveryLocation, itemType, quantity);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, clientId);
                pstmt.setString(2, pickupLocation);
                pstmt.setString(3, deliveryLocation);
                pstmt.setString(4, itemType);
                pstmt.setDouble(5, quantity);
                pstmt.setBoolean(6, isVip);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    LOGGER.warning("Creating order failed, no rows affected");
                    return -1;
                }
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        if (!generateBill(orderId)) {
                            LOGGER.warning("Failed to generate bill for order: " + orderId);
                        }
                        return orderId;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid order input", e);
            return -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while creating order", e);
            return -1;
        }
        
        return -1;
    }

    /**
     * Validates order input parameters
     */
    private void validateOrderInput(int clientId, String pickupLocation, String deliveryLocation, 
                                    String itemType, double quantity) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location cannot be empty");
        }
        if (deliveryLocation == null || deliveryLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery location cannot be empty");
        }
        if (itemType == null || itemType.trim().isEmpty()) {
            throw new IllegalArgumentException("Item type cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    /**
     * Retrieves active orders for tracking
     * @throws SQLException if database access error occurs
     */
    public Vector<Vector<Object>> getActiveOrders(int clientId) throws SQLException {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        
        String sql = "SELECT o.order_id, o.status, " +
                    "COALESCE(da.current_location, 'Waiting for pickup') as current_location, " +
                    "o.estimated_delivery " +
                    "FROM orders o " +
                    "LEFT JOIN delivery_assignments da ON o.order_id = da.order_id " +
                    "WHERE o.client_id = ? " +
                    "AND o.status IN ('pending', 'assigned', 'in_transit')";

        Vector<Vector<Object>> data = new Vector<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));
                    row.add(rs.getString("status"));
                    row.add(rs.getString("current_location"));
                    row.add(rs.getTimestamp("estimated_delivery"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving active orders for client " + clientId, e);
            throw e;
        }
        
        return data;
    }

    /**
     * Retrieves order history
     * @throws SQLException if database access error occurs
     */
    public Vector<Vector<Object>> getOrderHistory(int clientId) throws SQLException {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        
        String sql = "SELECT order_id, pickup_location, delivery_location, " +
                    "item_type, quantity, is_vip, created_at, status " +
                    "FROM orders WHERE client_id = ? " +
                    "ORDER BY created_at DESC";

        Vector<Vector<Object>> data = new Vector<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));
                    row.add(rs.getString("pickup_location"));
                    row.add(rs.getString("delivery_location"));
                    row.add(rs.getString("item_type"));
                    row.add(rs.getDouble("quantity"));
                    row.add(rs.getBoolean("is_vip") ? "Yes" : "No");
                    row.add(rs.getTimestamp("created_at"));
                    row.add(rs.getString("status"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving order history for client " + clientId, e);
            throw e;
        }
        
        return data;
    }

    /**
     * Generates bill for an order
     * @return true if bill generation successful, false otherwise
     */
    private boolean generateBill(int orderId) {
        if (orderId <= 0) {
            LOGGER.warning("Invalid order ID for bill generation: " + orderId);
            return false;
        }
        
        String sql = "INSERT INTO bills (order_id, amount, vip_charges, total_amount) " +
                    "SELECT ?, " +
                    "(SELECT 100 * quantity FROM orders WHERE order_id = ?) as base_amount, " +
                    "(CASE WHEN is_vip THEN 500 ELSE 0 END) as vip_charge, " +
                    "((SELECT 100 * quantity FROM orders WHERE order_id = ?) + " +
                    "(CASE WHEN is_vip THEN 500 ELSE 0 END)) as total " +
                    "FROM orders WHERE order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, orderId);
            pstmt.setInt(4, orderId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.warning("Bill generation failed for order: " + orderId);
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while generating bill for order " + orderId, e);
            return false;
        }
    }

    /**
     * Retrieves bill details for an order
     * @return BillDetails or null if no bill found
     * @throws SQLException if database access error occurs
     */
    public BillDetails getBillDetails(int orderId) throws SQLException {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        
        String sql = "SELECT b.bill_id, b.amount, b.vip_charges, b.total_amount, " +
                    "b.generated_at, b.status, " +
                    "o.pickup_location, o.delivery_location, o.item_type, " +
                    "o.quantity, o.is_vip " +
                    "FROM bills b " +
                    "JOIN orders o ON b.order_id = o.order_id " +
                    "WHERE b.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new BillDetails(
                        rs.getInt("bill_id"),
                        orderId,
                        rs.getDouble("amount"),
                        rs.getDouble("vip_charges"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("generated_at"),
                        rs.getString("status"),
                        rs.getString("pickup_location"),
                        rs.getString("delivery_location"),
                        rs.getString("item_type"),
                        rs.getDouble("quantity"),
                        rs.getBoolean("is_vip")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving bill details for order " + orderId, e);
            throw e;
        }
        
        return null;
    }

    /**
     * Updates order status
     * @return true if status update successful, false otherwise
     */
    public boolean updateOrderStatus(int orderId, String status) {
        // Validate input
        if (orderId <= 0) {
            LOGGER.warning("Invalid order ID for status update: " + orderId);
            return false;
        }
        if (status == null || status.trim().isEmpty()) {
            LOGGER.warning("Invalid status for order: " + orderId);
            return false;
        }
        
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.warning("No rows updated for order status: " + orderId);
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating order status", e);
            return false;
        }
    }

    /**
     * Inner class to hold bill details
     */
    public static class BillDetails {
        private int billId;
        private int orderId;
        private double amount;
        private double vipCharges;
        private double totalAmount;
        private Timestamp generatedAt;
        private String status;
        private String pickupLocation;
        private String deliveryLocation;
        private String itemType;
        private double quantity;
        private boolean isVip;

        public BillDetails(int billId, int orderId, double amount, double vipCharges,
                          double totalAmount, Timestamp generatedAt, String status,
                          String pickupLocation, String deliveryLocation, String itemType,
                          double quantity, boolean isVip) {
            this.billId = billId;
            this.orderId = orderId;
            this.amount = amount;
            this.vipCharges = vipCharges;
            this.totalAmount = totalAmount;
            this.generatedAt = generatedAt;
            this.status = status;
            this.pickupLocation = pickupLocation;
            this.deliveryLocation = deliveryLocation;
            this.itemType = itemType;
            this.quantity = quantity;
            this.isVip = isVip;
        }

        // Getters
        public int getBillId() { return billId; }
        public int getOrderId() { return orderId; }
        public double getAmount() { return amount; }
        public double getVipCharges() { return vipCharges; }
        public double getTotalAmount() { return totalAmount; }
        public Timestamp getGeneratedAt() { return generatedAt; }
        public String getStatus() { return status; }
        public String getPickupLocation() { return pickupLocation; }
        public String getDeliveryLocation() { return deliveryLocation; }
        public String getItemType() { return itemType; }
        public double getQuantity() { return quantity; }
        public boolean isVip() { return isVip; }
    }
}