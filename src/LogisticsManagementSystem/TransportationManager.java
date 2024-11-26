
package LogisticsManagementSystem;

import java.sql.*;
import java.util.Random;

public class TransportationManager {
    private Connection conn;

    public TransportationManager() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    public boolean assignDriver(int orderId) {
        String sql = "SELECT d.driver_id, v.vehicle_id FROM drivers d " +
                    "JOIN vehicles v ON d.vehicle_id = v.vehicle_id " +
                    "WHERE d.status = 'available' LIMIT 1";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int driverId = rs.getInt("driver_id");
                int vehicleId = rs.getInt("vehicle_id");
                
                // Create delivery assignment
                String assignSql = "INSERT INTO delivery_assignments (order_id, driver_id, vehicle_id, started_at) " +
                                 "VALUES (?, ?, ?, NOW())";
                
                try (PreparedStatement assignPstmt = conn.prepareStatement(assignSql)) {
                    assignPstmt.setInt(1, orderId);
                    assignPstmt.setInt(2, driverId);
                    assignPstmt.setInt(3, vehicleId);
                    return assignPstmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateDeliveryOTP(int orderId) {
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000));
        
        String sql = "UPDATE orders SET delivery_otp = ? WHERE order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            return otp;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyDeliveryOTP(int orderId, String otp) {
        String sql = "SELECT delivery_otp FROM orders WHERE order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return otp.equals(rs.getString("delivery_otp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
