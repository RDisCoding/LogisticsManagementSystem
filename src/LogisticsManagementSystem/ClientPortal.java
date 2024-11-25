package LogisticsManagementSystem;

import java.sql.*;
import java.util.Vector;
import java.time.LocalDateTime;

public class ClientPortal {
    private Connection conn;

    public ClientPortal() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    /**
     * Creates a new order in the system
     */
    public int createOrder(int clientId, String pickupLocation, String deliveryLocation, 
                          String itemType, double quantity, boolean isVip) {
        String sql = "INSERT INTO orders (client_id, pickup_location, delivery_location, " +
                    "item_type, quantity, is_vip, status, estimated_delivery) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'pending', DATE_ADD(NOW(), INTERVAL 2 DAY))";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, pickupLocation);
            pstmt.setString(3, deliveryLocation);
            pstmt.setString(4, itemType);
            pstmt.setDouble(5, quantity);
            pstmt.setBoolean(6, isVip);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int orderId = rs.getInt(1);
                generateBill(orderId); // Automatically generate bill for new order
                return orderId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves active orders for tracking
     */
    public Vector<Vector<Object>> getActiveOrders(int clientId) throws SQLException {
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
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("order_id"));
                row.add(rs.getString("status"));
                row.add(rs.getString("current_location"));
                row.add(rs.getTimestamp("estimated_delivery"));
                data.add(row);
            }
        }
        
        return data;
    }

    /**
     * Retrieves order history
     */
    public Vector<Vector<Object>> getOrderHistory(int clientId) throws SQLException {
        String sql = "SELECT order_id, pickup_location, delivery_location, " +
                    "item_type, quantity, is_vip, created_at, status " +
                    "FROM orders WHERE client_id = ? " +
                    "ORDER BY created_at DESC";

        Vector<Vector<Object>> data = new Vector<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
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
        
        return data;
    }

    /**
     * Generates bill for an order
     */
    private boolean generateBill(int orderId) {
        // Calculate base amount based on distance and quantity
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves bill details for an order
     */
    public BillDetails getBillDetails(int orderId) throws SQLException {
        String sql = "SELECT b.bill_id, b.amount, b.vip_charges, b.total_amount, " +
                    "b.generated_at, b.status, " +
                    "o.pickup_location, o.delivery_location, o.item_type, " +
                    "o.quantity, o.is_vip " +
                    "FROM bills b " +
                    "JOIN orders o ON b.order_id = o.order_id " +
                    "WHERE b.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
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
        
        return null;
    }

    /**
     * Updates order status
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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