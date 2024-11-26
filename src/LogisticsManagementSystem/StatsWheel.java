package LogisticsManagementSystem;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsWheel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(22, 27, 34);
    private static final Color[] SEGMENT_COLORS = {
        new Color(80, 227, 194),   // Mint green for clients
        new Color(91, 143, 249),   // Blue for drivers
        new Color(136, 46, 224),   // Purple for warehouse
        new Color(255, 105, 180)   // Pink for total
    };
    
    private int totalUsers = 0;
    private int clients = 0;
    private int drivers = 0;
    private int warehouse = 0;
    private Timer refreshTimer;

    public StatsWheel() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(400, 400));
        initializeData();
        setupRefreshTimer();
    }

    private void initializeData() {
        try {
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer(60000, e -> {
            try {
                updateStats();
                repaint();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        refreshTimer.start();
    }

    private void updateStats() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total users
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }
            
            // Clients
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE user_type = 'client'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    clients = rs.getInt(1);
                }
            }
            
            // Drivers
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE user_type = 'driver'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    drivers = rs.getInt(1);
                }
            }
            
            // Warehouse
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE user_type = 'warehouse'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    warehouse = rs.getInt(1);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw outer ring segments
        int outerRadius = Math.min(width, height) / 2 - 40;
        int innerRadius = outerRadius - 40;
        
        double total = clients + drivers + warehouse;
        double startAngle = 90;
        
        // Draw segments
        drawSegment(g2, centerX, centerY, innerRadius, outerRadius, startAngle, (clients / total) * 360, SEGMENT_COLORS[0]);
        startAngle += (clients / total) * 360;
        
        drawSegment(g2, centerX, centerY, innerRadius, outerRadius, startAngle, (drivers / total) * 360, SEGMENT_COLORS[1]);
        startAngle += (drivers / total) * 360;
        
        drawSegment(g2, centerX, centerY, innerRadius, outerRadius, startAngle, (warehouse / total) * 360, SEGMENT_COLORS[2]);

        // Draw center circle
        g2.setColor(BACKGROUND_COLOR);
        g2.fillOval(centerX - innerRadius + 40, centerY - innerRadius + 40, 
                    (innerRadius - 40) * 2, (innerRadius - 40) * 2);

        // Draw total users in center
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        String totalText = String.valueOf(totalUsers);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalText, 
                     centerX - fm.stringWidth(totalText) / 2,
                     centerY + fm.getHeight() / 4);
        
        // Draw "Total" label
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        String label = "Users";
        fm = g2.getFontMetrics();
        g2.drawString(label,
                     centerX - fm.stringWidth(label) / 2,
                     centerY + fm.getHeight());

        // Draw stats labels
        drawStatsLabel(g2, "Clients: " + clients, centerX - outerRadius + 40, centerY - outerRadius + 40, SEGMENT_COLORS[0]);
        drawStatsLabel(g2, "Drivers: " + drivers, centerX + outerRadius + 60, centerY + outerRadius - 40, SEGMENT_COLORS[1]);
        drawStatsLabel(g2, "Warehouse: " + warehouse, centerX + outerRadius - 40, centerY - outerRadius - 10, SEGMENT_COLORS[2]);
    }

    private void drawSegment(Graphics2D g2, int centerX, int centerY, int innerRadius, int outerRadius, 
                           double startAngle, double arcAngle, Color color) {
        g2.setColor(color);
        Arc2D.Double outer = new Arc2D.Double(
            centerX - outerRadius, centerY - outerRadius,
            outerRadius * 2, outerRadius * 2,
            startAngle, arcAngle, Arc2D.PIE);
        g2.fill(outer);
    }

    private void drawStatsLabel(Graphics2D g2, String text, int x, int y, Color color) {
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        
        // Draw color indicator dot
        g2.setColor(color);
        g2.fillOval(x - fm.stringWidth(text) - 15, y - fm.getHeight() / 2, 8, 8);
        
        // Draw text
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(text, x - fm.stringWidth(text), y);
    }

    public void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}