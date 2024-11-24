
package LogisticsManagementSystem;

import java.sql.*;

public class ClientPortal {
    private Connection conn;

    public ClientPortal() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    public int createOrder(int clientId, String pickupLocation, String deliveryLocation, 
                          String itemType, double quantity, boolean isVip) {
        String sql = "INSERT INTO orders (client_id, pickup_location, delivery_location, " +
                    "item_type, quantity, is_vip) VALUES (?, ?, ?, ?, ?, ?)";
        
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
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean generateBill(int orderId) {
        // Calculate base amount based on distance and quantity
        String sql = "INSERT INTO bills (order_id, amount, vip_charges, total_amount) " +
                    "SELECT ?, " +
                    "(SELECT 100 * quantity FROM orders WHERE order_id = ?), " +
                    "(CASE WHEN is_vip THEN 500 ELSE 0 END), " +
                    "((SELECT 100 * quantity FROM orders WHERE order_id = ?) + " +
                    "(CASE WHEN is_vip THEN 500 ELSE 0 END)) " +
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
}
