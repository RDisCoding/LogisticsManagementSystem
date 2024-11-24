
package LogisticsManagementSystem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class DriverPanel extends JPanel {
    private User currentUser;
    private TransportationManager transportManager;
    
    public DriverPanel(User user) {
        this.currentUser = user;
        try {
            this.transportManager = new TransportationManager();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error");
        }
        
        setLayout(new BorderLayout());
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Create tabbed pane for different driver functionalities
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Add Current Delivery Panel
        tabbedPane.addTab("Current Delivery", createCurrentDeliveryPanel());
        
        // Add Delivery History Panel
        tabbedPane.addTab("Delivery History", createDeliveryHistoryPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createCurrentDeliveryPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Delivery Status
            JLabel statusLabel = new JLabel("Current Status: ");
            JLabel currentStatus = new JLabel("No Active Delivery");
            
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(statusLabel, gbc);
            gbc.gridx = 1;
            panel.add(currentStatus, gbc);
            
            // Delivery Details
            JTextArea deliveryDetails = new JTextArea(10, 40);
            deliveryDetails.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(deliveryDetails);
            
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.gridwidth = 2;
            panel.add(scrollPane, gbc);
            
            // OTP Verification Panel
            JPanel otpPanel = new JPanel(new FlowLayout());
            JTextField otpField = new JTextField(6);
            JButton verifyButton = new JButton("Verify OTP");
            
            verifyButton.addActionListener(e -> {
                String otp = otpField.getText();
                // Implement OTP verification logic
            });
            
            otpPanel.add(new JLabel("Enter OTP: "));
            otpPanel.add(otpField);
            otpPanel.add(verifyButton);
            
            gbc.gridy = 2;
            panel.add(otpPanel, gbc);
            
            // Update Status Button
            JButton updateStatusButton = new JButton("Update Delivery Status");
            gbc.gridy = 3;
            panel.add(updateStatusButton, gbc);
            
            return panel;
        }
        
        private JPanel createDeliveryHistoryPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            
            // Create table model for delivery history
            String[] columns = {"Delivery ID", "Date", "From", "To", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            JTable historyTable = new JTable(model);
            
            // Add some sample data
            // In practice, this would be populated from the database
            JScrollPane scrollPane = new JScrollPane(historyTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            return panel;
        }
    }
