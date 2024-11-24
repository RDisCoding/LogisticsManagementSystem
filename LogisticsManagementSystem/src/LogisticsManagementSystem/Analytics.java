package LogisticsManagementSystem;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Analytics {
    private Connection conn;

    public Analytics() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    public Map<String, Double> getPerformanceMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        
        // Calculate average delivery time
        String sql = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, actual_delivery)) as avg_delivery_time, " +
                    "COUNT(*) as total_orders, " +
                    "SUM(CASE WHEN status = 'delivered' THEN 1 ELSE 0 END) / COUNT(*) * 100 as completion_rate " +
                    "FROM orders";
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                metrics.put("avgDeliveryTime", rs.getDouble("avg_delivery_time"));
                metrics.put("totalOrders", rs.getDouble("total_orders"));
                metrics.put("completionRate", rs.getDouble("completion_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return metrics;
    }
    
    public Map<String, Double> getCostAnalysis() {
        Map<String, Double> costs = new HashMap<>();
        
        String sql = "SELECT SUM(total_amount) as total_revenue, " +
                    "COUNT(DISTINCT order_id) as total_orders, " +
                    "AVG(total_amount) as avg_order_value, " +
                    "SUM(vip_charges) as vip_revenue " +
                    "FROM bills";
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                costs.put("totalRevenue", rs.getDouble("total_revenue"));
                costs.put("totalOrders", rs.getDouble("total_orders"));
                costs.put("avgOrderValue", rs.getDouble("avg_order_value"));
                costs.put("vipRevenue", rs.getDouble("vip_revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return costs;
    }

    public Map<String, Double> getResourceUtilization() {
        Map<String, Double> utilization = new HashMap<>();
        
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM drivers WHERE status = 'available') as available_drivers, " +
                    "(SELECT COUNT(*) FROM vehicles WHERE status = 'available') as available_vehicles, " +
                    "(SELECT AVG(current_utilization/capacity * 100) FROM warehouse) as warehouse_utilization";
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                utilization.put("availableDrivers", rs.getDouble("available_drivers"));
                utilization.put("availableVehicles", rs.getDouble("available_vehicles"));
                utilization.put("warehouseUtilization", rs.getDouble("warehouse_utilization"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return utilization;
    }
}