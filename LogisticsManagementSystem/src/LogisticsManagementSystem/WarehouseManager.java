
package LogisticsManagementSystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseManager {
    private Connection conn;

    public WarehouseManager() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    public boolean updateInventory(int warehouseId, String itemName, int quantity) {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE warehouse_id = ? AND item_name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, warehouseId);
            pstmt.setString(3, itemName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getWarehouseTemperature(int warehouseId) {
        String sql = "SELECT temperature FROM warehouse WHERE warehouse_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("temperature");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
