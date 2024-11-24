package LogisticsManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class ClientPanel extends JPanel {
    private User currentUser;
    private ClientPortal clientPortal;

    public ClientPanel(User user) {
        this.currentUser = user;
        try {
            this.clientPortal = new ClientPortal();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error");
        }

        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create tabbed pane for different client functionalities
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add Order Panel
        tabbedPane.addTab("New Order", createOrderPanel());

        // Add Order Tracking Panel
        tabbedPane.addTab("Track Orders", createTrackingPanel());

        // Add Order History Panel
        tabbedPane.addTab("Order History", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add order form components
        JTextField pickupLocation = new JTextField(20);
        JTextField deliveryLocation = new JTextField(20);
        JTextField itemType = new JTextField(20);
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        JCheckBox vipService = new JCheckBox("VIP Service");

        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Pickup Location:"), gbc);
        gbc.gridx = 1;
        panel.add(pickupLocation, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Delivery Location:"), gbc);
        gbc.gridx = 1;
        panel.add(deliveryLocation, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Item Type:"), gbc);
        gbc.gridx = 1;
        panel.add(itemType, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(quantity, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(vipService, gbc);

        JButton submitButton = new JButton("Place Order");
        submitButton.addActionListener(e -> {
            int orderId = clientPortal.createOrder(
                currentUser.getUserId(),
                pickupLocation.getText(),
                deliveryLocation.getText(),
                itemType.getText(),
                (Double) quantity.getValue(),
                vipService.isSelected()
            );

            if (orderId != -1) {
                JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + orderId);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to place order");
            }
        });

        gbc.gridy = 5;
        panel.add(submitButton, gbc);

        return panel;
    }

    private JPanel createTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Order Tracking Feature Coming Soon!", JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Order History Feature Coming Soon!", JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        // Create a dummy user for demonstration purposes
        User dummyUser = new User(1, "1", "client", "a", "1");

        // Set up the JFrame
        JFrame frame = new JFrame("Client Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        // Initialize the ClientPanel with the dummy user
        ClientPanel clientPanel = new ClientPanel(dummyUser);

        // Add the ClientPanel to the frame
        frame.add(clientPanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }

}