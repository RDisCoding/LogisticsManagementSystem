package LogisticsManagementSystem;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

public class NewOrderPanel extends JPanel {
    private JTextField pickupLocationField;
    private JTextField deliveryLocationField;
    private JComboBox<String> itemTypeComboBox;
    private JSpinner quantitySpinner;
    private JCheckBox vipServiceCheckBox;
    private JButton placeOrderButton;
    private ClientPortal clientPortal;
    private User currentUser;

    // Enhanced color scheme
    private static final Color PANEL_BG = new Color(22, 25, 32);
    private static final Color FIELD_BG = new Color(45, 48, 56);
    private static final Color TEXT_COLOR = new Color(220, 220, 230);
    private static final Color LABEL_COLOR = new Color(170, 175, 205);
    private static final Color ACCENT_COLOR = new Color(103, 58, 183);
    private static final Color BUTTON_HOVER = new Color(81, 45, 168);

    public NewOrderPanel(User user, ClientPortal clientPortal) {
        this.currentUser = user;
        this.clientPortal = clientPortal;
        
        setLayout(new GridBagLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        initializeComponents();
        layoutComponents();
        styleComponents();
        setupOrderPlacement();
    }

    private void initializeComponents() {
        pickupLocationField = new JTextField(20);
        deliveryLocationField = new JTextField(20);
        
        String[] itemTypes = {"A", "B", "C", "D"};
        itemTypeComboBox = new JComboBox<>(itemTypes);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1.0, 1.0, 1000.0, 1.0);
        quantitySpinner = new JSpinner(spinnerModel);
        
        vipServiceCheckBox = new JCheckBox("VIP Service");
        placeOrderButton = new JButton("Place Order");

        // Add hover effect to button
        placeOrderButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                placeOrderButton.setBackground(BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                placeOrderButton.setBackground(ACCENT_COLOR);
            }
        });
    }

    private void setupOrderPlacement() {
        placeOrderButton.addActionListener(e -> {
            String pickupLocation = pickupLocationField.getText();
            String deliveryLocation = deliveryLocationField.getText();
            String itemType = (String) itemTypeComboBox.getSelectedItem();
            Double quantity = (Double) quantitySpinner.getValue();
            boolean isVipService = vipServiceCheckBox.isSelected();

            // Validation
            if (pickupLocation.trim().isEmpty() || deliveryLocation.trim().isEmpty()) {
                showError("Please fill in all location fields.");
                return;
            }

            // Show advance payment dialog
            JDialog advancePaymentDialog = createAdvancePaymentDialog(
                pickupLocation, 
                deliveryLocation, 
                itemType, 
                quantity, 
                isVipService
            );
            advancePaymentDialog.setVisible(true);
        });
    }

    private JDialog createAdvancePaymentDialog(
        String pickupLocation, 
        String deliveryLocation, 
        String itemType, 
        Double quantity, 
        boolean isVipService
    ) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Advance Payment", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PANEL_BG);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Calculate advance payment (example calculation)
        double advanceAmount = calculateAdvancePayment(itemType, quantity, isVipService);
        
        JLabel messageLabel = new JLabel(String.format("Advance Payment Required: $%.2f", advanceAmount));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton payButton = new JButton("Pay Advance");
        styleAdvancePaymentButton(payButton);
        
        payButton.addActionListener(e -> {
            try {
                int orderId = clientPortal.createOrder(
                    currentUser.getUserId(),
                    pickupLocation,
                    deliveryLocation,
                    itemType,
                    quantity,
                    isVipService
                );

                if (orderId != -1) {
                    showSuccess("Order placed successfully! Order ID: " + orderId);
                    clearForm();
                    dialog.dispose();
                } else {
                    showError("Failed to place order. Please try again.");
                }
            } catch (Exception ex) {
                showError("Error processing order: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(messageLabel, gbc);
        
        gbc.gridy = 1;
        dialog.add(payButton, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        return dialog;
    }

    private double calculateAdvancePayment(String itemType, Double quantity, boolean isVipService) {
        // Example advance payment calculation 
        double baseRate = 10.0;
        switch (itemType) {
            case "A": baseRate = 15.0; break;
            case "B": baseRate = 20.0; break;
            case "C": baseRate = 25.0; break;
            case "D": baseRate = 30.0; break;
        }
        
        double advanceAmount = baseRate * quantity;
        if (isVipService) {
            advanceAmount *= 1.2; // 20% additional for VIP service
        }
        
        return advanceAmount;
    }

    private void styleAdvancePaymentButton(JButton button) {
        button.setPreferredSize(new Dimension(200, 40));
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void clearForm() {
        pickupLocationField.setText("");
        deliveryLocationField.setText("");
        itemTypeComboBox.setSelectedIndex(0);
        quantitySpinner.setValue(1.0);
        vipServiceCheckBox.setSelected(false);
    }

    private void styleComponents() {
        // Style text fields with larger dimensions
        Dimension fieldSize = new Dimension(300, 40);
        styleTextField(pickupLocationField, fieldSize);
        styleTextField(deliveryLocationField, fieldSize);
        
        // Style combo box
        itemTypeComboBox.setPreferredSize(fieldSize);
        itemTypeComboBox.setBackground(FIELD_BG);
        itemTypeComboBox.setForeground(TEXT_COLOR);
        itemTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Style spinner
        quantitySpinner.setPreferredSize(fieldSize);
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField();
        spinnerTextField.setBackground(FIELD_BG);
        spinnerTextField.setForeground(TEXT_COLOR);
        spinnerTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Style checkbox
        vipServiceCheckBox.setForeground(LABEL_COLOR);
        vipServiceCheckBox.setBackground(PANEL_BG);
        vipServiceCheckBox.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Style button
        placeOrderButton.setPreferredSize(new Dimension(300, 45));
        placeOrderButton.setBackground(ACCENT_COLOR);
        placeOrderButton.setForeground(Color.WHITE);
        placeOrderButton.setBorderPainted(false);
        placeOrderButton.setFocusPainted(false);
        placeOrderButton.setFont(new Font("Arial", Font.BOLD, 16));
    }

    private void styleTextField(JTextField field, Dimension size) {
        field.setPreferredSize(size);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BG, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        addFormRow("Pickup Location:", pickupLocationField, gbc, 0);
        addFormRow("Delivery Location:", deliveryLocationField, gbc, 1);
        addFormRow("Item Type:", itemTypeComboBox, gbc, 2);
        addFormRow("Quantity:", quantitySpinner, gbc, 3);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 20, 10);
        add(vipServiceCheckBox, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(placeOrderButton, gbc);
    }

    private void addFormRow(String labelText, JComponent component, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setForeground(LABEL_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        add(label, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(component, gbc);
    }
}