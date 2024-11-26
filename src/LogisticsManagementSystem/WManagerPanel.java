
package LogisticsManagementSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class WManagerPanel extends JPanel {
    private User currentUser;
    
    public WManagerPanel() {
    	System.out.println("WManagerPanel loaded");
    }

    public WManagerPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Panel layout
        JTabbedPane tabbedPane = new JTabbedPane();

        // Inventory Management Tab
        tabbedPane.addTab("Inventory Management", createInventoryPanel());

        // Reports Tab
        tabbedPane.addTab("Reports", createReportsPanel());

        // Add tabs to the main panel
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table to display inventory
        String[] columns = {"Inventory ID", "Warehouse ID", "Item Name", "Quantity", "Unit", "Last Updated"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable inventoryTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        // Populate inventory table from the database
        loadInventoryData(model);

        // Add scrollPane to the panel
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add control buttons
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Item");
        JButton deleteButton = new JButton("Delete Item");

        controlPanel.add(addButton);
        controlPanel.add(updateButton);
        controlPanel.add(deleteButton);

        panel.add(controlPanel, BorderLayout.SOUTH);

        // Add button actions
        addButton.addActionListener(e -> handleAddItem(model));
        updateButton.addActionListener(e -> handleUpdateItem(model, inventoryTable));
        deleteButton.addActionListener(e -> handleDeleteItem(model, inventoryTable));

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Warehouse Reports Coming Soon!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void loadInventoryData(DefaultTableModel model) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("inventory_id"),   // Inventory ID
                    rs.getInt("warehouse_id"),  // Warehouse ID
                    rs.getString("item_name"),  // Item Name
                    rs.getInt("quantity"),      // Quantity
                    rs.getString("unit"),       // Unit
                    rs.getTimestamp("last_updated") // Last Updated
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load inventory data: " + e.getMessage());
        }
    }


    private void handleAddItem(DefaultTableModel model) {
        JTextField warehouseField = new JTextField();
        JTextField itemField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField unitField = new JTextField();

        Object[] message = {
            "Warehouse ID:", warehouseField,
            "Item Name:", itemField,
            "Quantity:", quantityField,
            "Unit:", unitField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO inventory (warehouse_id, item_name, quantity, unit, last_updated) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, Integer.parseInt(warehouseField.getText()));
                    pstmt.setString(2, itemField.getText());
                    pstmt.setInt(3, Integer.parseInt(quantityField.getText()));
                    pstmt.setString(4, unitField.getText());

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Item added successfully!");
                    model.setRowCount(0); // Clear the table
                    loadInventoryData(model); // Reload data
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to add item: " + e.getMessage());
            }
        }
    }


    private void handleUpdateItem(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update");
            return;
        }

        int inventoryId = (int) model.getValueAt(selectedRow, 0); // Inventory ID
        JTextField warehouseField = new JTextField(model.getValueAt(selectedRow, 1).toString());
        JTextField itemField = new JTextField(model.getValueAt(selectedRow, 2).toString());
        JTextField quantityField = new JTextField(model.getValueAt(selectedRow, 3).toString());
        JTextField unitField = new JTextField(model.getValueAt(selectedRow, 4).toString());

        Object[] message = {
            "Warehouse ID:", warehouseField,
            "Item Name:", itemField,
            "Quantity:", quantityField,
            "Unit:", unitField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE inventory SET warehouse_id = ?, item_name = ?, quantity = ?, unit = ?, last_updated = CURRENT_TIMESTAMP WHERE inventory_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, Integer.parseInt(warehouseField.getText()));
                    pstmt.setString(2, itemField.getText());
                    pstmt.setInt(3, Integer.parseInt(quantityField.getText()));
                    pstmt.setString(4, unitField.getText());
                    pstmt.setInt(5, inventoryId);

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Item updated successfully!");
                    model.setRowCount(0); // Clear the table
                    loadInventoryData(model); // Reload data
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update item: " + e.getMessage());
            }
        }
    }


    private void handleDeleteItem(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete");
            return;
        }

        int inventoryId = (int) model.getValueAt(selectedRow, 0); // Inventory ID

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Delete Item", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM inventory WHERE inventory_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, inventoryId);

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Item deleted successfully!");
                    model.setRowCount(0); // Clear the table
                    loadInventoryData(model); // Reload data
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to delete item: " + e.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Warehouse Manager Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Dummy User for testing
            User testUser = new User(3, "3", "warehouse", "c", "3");
            WManagerPanel panel = new WManagerPanel(testUser);

            frame.add(panel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
