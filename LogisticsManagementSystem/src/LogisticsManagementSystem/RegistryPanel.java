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

public class RegistryPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(136, 46, 224);
    private static final Color HOVER_COLOR = new Color(48, 54, 61);

    private User currentUser;
    private JPanel statsPanel;
    private Timer refreshTimer;

    public RegistryPanel(User user) {
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

        // Add Register button
        JButton registerButton = createRegisterButton();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(registerButton);
        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
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

    private JButton createRegisterButton() {
        JButton button = new JButton("Register New User");
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        button.addActionListener(e -> showRegisterDialog());
        return button;
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New User", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBackground(CARD_BACKGROUND);
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Field labels and text fields
        String[] labels = {"Username:", "Password:", "Confirm Password:", "Email:", "Phone:", "User Type:"};
        JTextField[] fields = new JTextField[5];
        
        Map<String, String> userTypeMap = new LinkedHashMap<>();
        userTypeMap.put("Client", "client");
        userTypeMap.put("Warehouse Manager", "warehouse");
        userTypeMap.put("Driver", "driver");

        JComboBox<String> userTypeDropdown = new JComboBox<>(userTypeMap.keySet().toArray(new String[0]));

        // Add fields to panel
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            
            JLabel label = new JLabel(labels[i]);
            label.setForeground(TEXT_COLOR);
            registerPanel.add(label, gbc);

            gbc.gridx = 1;
            if (i == 1 || i == 2) { // Password fields
                fields[i] = new JPasswordField(20);
            } else if (i == 5) { // User Type Dropdown
                registerPanel.add(userTypeDropdown, gbc);
                continue;
            } else {
                fields[i] = new JTextField(20);
            }
            fields[i].setBackground(BACKGROUND_COLOR);
            fields[i].setForeground(TEXT_COLOR);
            fields[i].setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
            registerPanel.add(fields[i], gbc);
        }

        // Submit button
        JButton submitButton = new JButton("Register");
        submitButton.setBackground(ACCENT_COLOR);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        registerPanel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String username = fields[0].getText();
            String password = new String(((JPasswordField) fields[1]).getPassword());
            String confirmPassword = new String(((JPasswordField) fields[2]).getPassword());
            String email = fields[3].getText();
            String phone = fields[4].getText();
            String userTypeFriendly = (String) userTypeDropdown.getSelectedItem();
            String userType = userTypeMap.get(userTypeFriendly);

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO users (username, password, email, phone, user_type) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.setString(3, email);
                    pstmt.setString(4, phone);
                    pstmt.setString(5, userType);

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Registration successful!");
                    refreshStats(); // Refresh the stats after successful registration
                    dialog.dispose();
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(dialog, "Username or email already exists!");
                } else {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Registration failed: " + ex.getMessage());
                }
            }
        });

        dialog.add(registerPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
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