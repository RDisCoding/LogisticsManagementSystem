package LogisticsManagementSystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class UserCount extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(136, 46, 224);
    private static final Color HOVER_COLOR = new Color(48, 54, 61);

    private User currentUser;
    private JPanel statsPanel;
    private Timer refreshTimer;

    public UserCount(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
        setupRefreshTimer();
    }

    private void initializeComponents() {
        // Top section with stats cards
        statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        // Center section with user list or additional info
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(BACKGROUND_COLOR);

    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Will be populated by refreshStats()
        panel.add(createStatCard("Total Users", "0"));
        panel.add(createStatCard("Clients", "0"));
        panel.add(createStatCard("Drivers", "0"));
        panel.add(createStatCard("Warehouse Managers", "0"));
        
        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(titleLabel, gbc);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, gbc);

        // Store the value label in the card to update it later
        card.putClientProperty("valueLabel", valueLabel);
        
        return card;
    }

    
    private void setupRefreshTimer() {
        refreshTimer = new Timer(60000, e -> refreshStats()); // Refresh every minute
        refreshTimer.start();
        refreshStats(); // Initial refresh
    }

    private void refreshStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update total users
            updateStatCard(0, "SELECT COUNT(*) FROM users");
            
            // Update clients
            updateStatCard(1, "SELECT COUNT(*) FROM users WHERE user_type = 'client'");
            
            // Update drivers
            updateStatCard(2, "SELECT COUNT(*) FROM users WHERE user_type = 'driver'");
            
            // Update warehouse managers
            updateStatCard(3, "SELECT COUNT(*) FROM users WHERE user_type = 'warehouse'");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStatCard(int cardIndex, String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JPanel card = (JPanel) statsPanel.getComponent(cardIndex);
                JLabel valueLabel = (JLabel) card.getClientProperty("valueLabel");
                valueLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
    }
}