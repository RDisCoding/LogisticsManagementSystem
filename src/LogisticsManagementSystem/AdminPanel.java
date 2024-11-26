package LogisticsManagementSystem;

import java.awt.BasicStroke;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(AdminPanel.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color SIDEBAR_COLOR = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(136, 46, 224);
    private static final Color HOVER_COLOR = new Color(48, 54, 61);
    
    private JLabel timeLabel;
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private DashboardPanel dashboardPanel;
    private newOrdersAdmin newOrders;
    private currentOrdersAdmin currentOrders;
    private completedOrdersAdmin completedOrders;
    
    public AdminPanel(User user) {
    	setupGlobalExceptionHandler();
//        setupTimeAndDateDisplay();
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        try {
            // Create main container
            JPanel mainContainer = new JPanel(new BorderLayout());
            mainContainer.setBackground(BACKGROUND_COLOR);
            
            // Create sidebar
            JPanel sidebar = createSidebar();
            mainContainer.add(sidebar, BorderLayout.WEST);
            
            // Create content panel with CardLayout
            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);
            contentPanel.setBackground(BACKGROUND_COLOR);
            
            // Initialize panels
            dashboardPanel = new DashboardPanel(currentUser);
            newOrders = new newOrdersAdmin(currentUser);
            currentOrders = new currentOrdersAdmin(currentUser);
            completedOrders = new completedOrdersAdmin(currentUser);
            
            // Add panels to card layout
            contentPanel.add(dashboardPanel, "Dashboard");
            contentPanel.add(newOrders, "New Orders");
            contentPanel.add(currentOrders, "Current Orders");
            contentPanel.add(completedOrders, "Completed Orders");
            
            mainContainer.add(contentPanel, BorderLayout.CENTER);
            add(mainContainer);
            
            // Show dashboard by default
            cardLayout.show(contentPanel, "Dashboard");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing admin panel components", e);
            JOptionPane.showMessageDialog(this, 
                "Unable to initialize admin panel: " + e.getMessage(), 
                "Initialization Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
//    private void setupTimeAndDateDisplay() {
//        try {
//            // Initialize timeLabel before using it
//            if (timeLabel == null) {
//                timeLabel = new JLabel();
//                timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
//                timeLabel.setForeground(Color.WHITE);
//                
//                // Add the label to the appropriate container
//                // For example: somePanel.add(timeLabel);
//            }
//
//            // Setup timer with null check and logging
//            Timer timer = new Timer(1000, e -> {
//                try {
//                    updateTimeAndDate();
//                } catch (Exception ex) {
//                    LOGGER.log(Level.SEVERE, "Error updating time and date", ex);
//                }
//            });
//            timer.start();
//
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Error setting up time and date display", e);
//            // Optional: Show user-friendly error message
//            JOptionPane.showMessageDialog(this, 
//                "Unable to setup time display. Please restart the application.", 
//                "System Error", 
//                JOptionPane.ERROR_MESSAGE);
//        }
//    }
    
//    private void updateTimeAndDate() {
//        try {
//            if (timeLabel != null) {
//                // Create a date formatter with the desired format
//                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm:ss a");
//
//                // Get the current date and format it
//                Date now = new Date();
//                String formattedDateTime = dateFormat.format(now);
//
//                // Update the label with the formatted date and time
//                timeLabel.setText(formattedDateTime);
//            } else {
//                LOGGER.warning("Time label is null. Reinitializing...");
//                setupTimeAndDateDisplay();
//            }
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Error in updateTimeAndDate", e);
//        }
//    }
    
    // Enhanced database connection method with proper exception handling
    private Connection getDatabaseConnection() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Unable to connect to database. Please check your connection.", 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
        }
        return conn;
    }
    
 // Example of a method with comprehensive exception handling
    private void loadUserData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getDatabaseConnection();
            if (conn == null) return;  // Exit if no connection

            String query = "SELECT * FROM users";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            // Process result set...
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading user data", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Failed to load user data: " + e.getMessage(), 
                    "Data Load Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
        } finally {
            // Always close resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database resources", e);
            }
        }
    }

    // Global exception handler for uncaught exceptions
    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, "Uncaught exception", throwable);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "An unexpected error occurred: " + throwable.getMessage(), 
                    "Unexpected Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
        });
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Logo placeholder
//        JLabel logo = new JLabel("Logistics");
//        logo.setForeground(TEXT_COLOR);
//        logo.setFont(new Font("Arial", Font.BOLD, 24));
//        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
//        sidebar.add(logo);
//        sidebar.add(Box.createVerticalStrut(50));
     
//        JLabel logo = new JLabel();
//        ImageIcon logoIcon = new ImageIcon("C:/Users/rudra/OneDrive/Desktop/Stuff/WebD Stuff/Logo/codeconqueror-high-resolution-logo-white.png/");
//        logo.setIcon(logoIcon);
        
        // Navigation buttons
        String[] menuItems = {"Dashboard", "New Orders", "Current Orders", "Completed Orders"};
        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createVerticalStrut(10));
        }
        
        // Logout button at bottom
        sidebar.add(Box.createVerticalGlue());
        JButton logoutButton = createMenuButton("Logout");
        logoutButton.setBackground(new Color(138, 43, 226));
        logoutButton.addActionListener(e -> handleLogout());
        sidebar.add(logoutButton);
        
        return sidebar;
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(210, 40));
        button.setBackground(SIDEBAR_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });
        
        // Add action listener for navigation
        button.addActionListener(e -> {
            if (!text.equals("Logout")) {
                cardLayout.show(contentPanel, text);
            }
        });
        
        return button;
    }
    
    private void handleLogout() {
        // Remove current panel
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();
            
            // Show login panel
            LoginPanel3 loginPanel = new LoginPanel3();
            frame.getContentPane().add(loginPanel);
            frame.revalidate();
            frame.repaint();
        }
    }
}

// Separate the dashboard content into its own class
//class DashboardPanel extends JPanel {
//    // Move all existing AdminPanel dashboard content here
//    // Copy everything from the original AdminPanel except the new navigation code
//    // Remember to update the constructor to take User parameter
//    // This includes all the charts, stats, and refresh timer functionality
//	
//}

class DashboardPanel extends JPanel {
    // Add logging
    private static final Logger LOGGER = Logger.getLogger(DashboardPanel.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(136, 46, 224);
    
    private User currentUser;
    private Timer refreshTimer;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JLabel temperatureLabel;
    private CircularProgressPanel warehouseCapacityPanel;
    
    private JFreeChart revenueChart;
    private JFreeChart deliveryStatsChart;
    private ChartPanel revenueChartPanel;
    private ChartPanel deliveryStatsChartPanel;

    public DashboardPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupRefreshTimer();
    }

    private void initializeComponents() {
        // Top stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);
        
        // Center panel with charts
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBackground(BACKGROUND_COLOR);
        
        // Add warehouse capacity chart
        warehouseCapacityPanel = new CircularProgressPanel();
        JPanel leftChartPanel = createChartPanel("Warehouse Capacity", warehouseCapacityPanel);
        centerPanel.add(leftChartPanel);
        
        // Add revenue chart
        JPanel revenueChartPanel = createRevenueChart();
        centerPanel.add(revenueChartPanel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with time and delivery stats
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        
        // Time widget panel
        JPanel timeWidget = createTimeWidget();
        bottomPanel.add(timeWidget);
        
        // Delivery stats panel
        JPanel deliveryStatsPanel = createDeliveryStatsChart();
        bottomPanel.add(deliveryStatsPanel);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createChartPanel(String title, JComponent chart) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        chart.setBackground(CARD_BACKGROUND);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRevenueChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        revenueChart = ChartFactory.createBarChart(
            null,                      // chart title
            null,                      // domain axis label
            null,                      // range axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            false,                     // tooltips
            false                      // urls
        );

        customizeChart(revenueChart);
        
        revenueChartPanel = new ChartPanel(revenueChart);
        revenueChartPanel.setPreferredSize(new Dimension(400, 300));
        
        updateRevenueChart();
        
        return createChartPanel("Revenue per Month", revenueChartPanel);
    }
    
    private JPanel createDeliveryStatsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        deliveryStatsChart = ChartFactory.createLineChart(
            null,                      // chart title
            null,                      // domain axis label
            null,                      // range axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            false,                     // tooltips
            false                      // urls
        );

        customizeChart(deliveryStatsChart);
        
        CategoryPlot plot = deliveryStatsChart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 150, 255));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        
        deliveryStatsChartPanel = new ChartPanel(deliveryStatsChart);
        deliveryStatsChartPanel.setPreferredSize(new Dimension(400, 300));
        
        updateDeliveryStatsChart();
        
        return createChartPanel("Deliveries Completed Over Time", deliveryStatsChartPanel);
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(CARD_BACKGROUND);
        chart.getLegend().setBackgroundPaint(CARD_BACKGROUND);
        chart.getLegend().setItemPaint(TEXT_COLOR);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD_BACKGROUND);
        plot.setDomainGridlinePaint(new Color(45, 55, 72));
        plot.setRangeGridlinePaint(new Color(45, 55, 72));
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(TEXT_COLOR);
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        panel.add(createStatCard("Orders Today", getOrdersToday(), "28.4%", true));
        panel.add(createStatCard("Active Deliveries", getActiveDeliveries(), "12.6%", false));
        panel.add(createStatCard("Available Drivers", getAvailableDrivers(), "3.1%", true));
        panel.add(createStatCard("Income Today", getIncomeToday(), "11.3%", true));
        
        return panel;
    }

    private JPanel createStatCard(String title, String value, String percentage, boolean isPositive) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        card.add(titleLabel, gbc);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, gbc);
        
        JLabel percentageLabel = new JLabel(percentage);
        percentageLabel.setForeground(isPositive ? new Color(34, 197, 94) : new Color(239, 68, 68));
        card.add(percentageLabel, gbc);
        
        return card;
    }

    private JPanel createTimeWidget() {
        
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_BACKGROUND);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        StatsWheel statsWheel = new StatsWheel();
        containerPanel.add(statsWheel, BorderLayout.CENTER);
        
        // Add this to ensure proper cleanup when the admin panel is closed
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                statsWheel.stopTimer();
            }
        });
        
        return containerPanel;
    }

    private void updateWarehouseCapacity() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT 
                    SUM(current_utilization) / SUM(capacity) * 100 as capacity_percentage 
                FROM warehouse
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int capacityPercentage = (int) rs.getDouble("capacity_percentage");
                    warehouseCapacityPanel.setPercentage(capacityPercentage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            warehouseCapacityPanel.setPercentage(0);
        }
    }

    private void updateRevenueChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT 
                    HOUR(generated_at) as hour,
                    SUM(total_amount) as revenue
                FROM bills 
                WHERE 
                    generated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
                    AND status = 'paid'
                GROUP BY 
                    hour
                ORDER BY 
                    hour
                """;
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    int hour = rs.getInt("hour");
                    double revenue = rs.getDouble("revenue");
                    
                    // Format hour for better display (e.g., 13 -> "1 PM")
                    String hourLabel = formatHourLabel(hour);
                    dataset.addValue(revenue, "Revenue", hourLabel);
                }
                
                if (!hasData) {
                    LOGGER.warning("No revenue data found for the last 24 hours");
                    dataset.addValue(0, "Revenue", "No Data");
                }
            }
            
            // Ensure we have a dataset
            if (dataset.getColumnCount() == 0) {
                dataset.addValue(0, "Revenue", "No Data");
            }
            
            revenueChart.getCategoryPlot().setDataset(dataset);
            
            // Update axis labels
            CategoryPlot plot = revenueChart.getCategoryPlot();
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setLabel("Hour of Day");
            
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setLabel("Revenue ($)");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating revenue chart", e);
            JOptionPane.showMessageDialog(this, 
                "Unable to update revenue chart: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDeliveryStatsChart() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT 
                    HOUR(completed_at) as hour,
                    COUNT(*) as completed_deliveries
                FROM delivery_assignments
                WHERE 
                    completed_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
                    AND status = 'completed'
                GROUP BY 
                    hour
                ORDER BY 
                    hour
                """;
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    int hour = rs.getInt("hour");
                    int deliveries = rs.getInt("completed_deliveries");
                    
                    // Format hour for better display (e.g., 13 -> "1 PM")
                    String hourLabel = formatHourLabel(hour);
                    dataset.addValue(deliveries, "Completed Deliveries", hourLabel);
                }
                
                if (!hasData) {
                    LOGGER.warning("No delivery data found for the last 24 hours");
                    dataset.addValue(0, "Completed Deliveries", "No Data");
                }
            }
            
            // Ensure we have a dataset
            if (dataset.getColumnCount() == 0) {
                dataset.addValue(0, "Completed Deliveries", "No Data");
            }
            
            deliveryStatsChart.getCategoryPlot().setDataset(dataset);
            
            // Update axis labels
            CategoryPlot plot = deliveryStatsChart.getCategoryPlot();
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setLabel("Hour of Day");
            
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setLabel("Number of Deliveries");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating delivery stats chart", e);
            JOptionPane.showMessageDialog(this, 
                "Unable to update delivery stats chart: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to format hour labels for better readability
    private String formatHourLabel(int hour) {
        if (hour == 0) return "12 AM";
        if (hour == 12) return "12 PM";
        return hour > 12 ? (hour - 12) + " PM" : hour + " AM";
    }
       // Database query methods
    private String getOrdersToday() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getActiveDeliveries() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM delivery_assignments WHERE status = 'assigned'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getAvailableDrivers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM drivers WHERE status = 'available'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getIncomeToday() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT SUM(total_amount) FROM bills WHERE DATE(generated_at) = CURDATE() AND status = 'paid'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double amount = rs.getDouble(1);
                    return String.format("$%.2f", amount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "$0.00";
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer(1000, e -> {
            try {
//                updateTimeAndDate();
                // Refresh other components every minute
                if (System.currentTimeMillis() % 60000 < 1000) {
                    refreshData();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error in refresh timer", ex);
                
                // Ensure UI labels are initialized
                if (timeLabel == null) {
                    timeLabel = new JLabel();
                }
                if (dateLabel == null) {
                    dateLabel = new JLabel();
                }
                
                // Set error text if possible
                timeLabel.setText("Error");
                dateLabel.setText("Refresh Failed");
            }
        });
        refreshTimer.start();
    }

//    private void updateTimeAndDate() {
//        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
//        LocalDateTime now = LocalDateTime.now();
//
//        timeLabel.setText(now.format(timeFormatter));
//        dateLabel.setText(now.format(dateFormatter));
//    }

    private void refreshData() {
        // Refresh all dynamic data
        SwingUtilities.invokeLater(() -> {
            // Update stats
            updateWarehouseCapacity();
            // Update charts
            updateRevenueChart();
            updateDeliveryStatsChart();
        });
    }
}

// Custom circular progress panel
class CircularProgressPanel extends JPanel {
    private int percentage = 0;
    
    public CircularProgressPanel() {
        setOpaque(false);
    }
    
    public void setPercentage(int percentage) {
        this.percentage = percentage;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int size = Math.min(getWidth(), getHeight()) - 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        
        // Draw background circle
        g2.setStroke(new BasicStroke(20f));
        g2.setColor(new Color(30, 37, 46));
        g2.drawArc(x, y, size, size, 0, 360);
        
        // Draw progress
        g2.setColor(new Color(136, 46, 224));
        g2.drawArc(x, y, size, size, 90, -(int)(percentage * 3.6));
        
        // Draw percentage text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String text = percentage + "%";
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2;
        g2.drawString(text, textX, textY);
    }
    
    
}