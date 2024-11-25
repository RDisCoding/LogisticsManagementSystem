package LogisticsManagementSystem;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MainUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private LoginPanel3 loginPanel;
    
    public MainUI() {
        setTitle("Logistics Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        initializeComponents();
        
        // Add the main panel to the frame
        add(mainPanel);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Create login panel
        loginPanel = new LoginPanel3();
        
        // Add login panel to the card layout with a name
        mainPanel.add(loginPanel, "login");
        
        // Show the login panel initially
        cardLayout.show(mainPanel, "login");
        
        // Set minimum size
        setMinimumSize(new Dimension(400, 300));
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupUserInterface();
       
    }
    
    private void setupUserInterface() {
        if (currentUser == null) return;
        
        switch (currentUser.getUserType()) {
            case "client":
                mainPanel.add(new ClientPanel(currentUser), "client");
                showPanel("client");
                break;
            case "driver":
                mainPanel.add(new DriverPanel(currentUser), "driver");
                showPanel("driver");
                break;
            case "admin":
                mainPanel.add(new AdminPanel(currentUser), "admin");
                showPanel("admin");
                break;
        }
    }
}