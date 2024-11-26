package LogisticsManagementSystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ClientPanel.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color SIDEBAR_COLOR = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR = new Color(136, 46, 224);
    private static final Color HOVER_COLOR = new Color(48, 54, 61);
    
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private NewOrderPanel newOrderPanel;
    private TrackOrderPanel trackOrdersPanel;
    private OrderHistoryPanel orderHistoryPanel;
    private ClientPortal clientPortal;
    
    public ClientPanel(User user) {
        this.currentUser = user;
        try {
            this.clientPortal = new ClientPortal();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            JOptionPane.showMessageDialog(
                this, 
                "Unable to connect to database. Please contact support.", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE
            );
            // Optionally, you might want to disable certain functionalities or 
            // provide a way to retry the connection
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error initializing ClientPanel", e);
            JOptionPane.showMessageDialog(
                this, 
                "An unexpected error occurred. Please restart the application.", 
                "Initialization Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
        
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
            newOrderPanel = new NewOrderPanel(currentUser, clientPortal);
            trackOrdersPanel = new TrackOrderPanel(currentUser);
            orderHistoryPanel = new OrderHistoryPanel(currentUser);
            
            // Add panels to card layout
            contentPanel.add(newOrderPanel, "New Order");
            contentPanel.add(trackOrdersPanel, "Track Orders");
            contentPanel.add(orderHistoryPanel, "Order History");
            
            mainContainer.add(contentPanel, BorderLayout.CENTER);
            add(mainContainer);
            
            // Show new order panel by default
            cardLayout.show(contentPanel, "New Order");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing components", e);
            JOptionPane.showMessageDialog(
                this, 
                "Failed to load user interface. Please restart the application.", 
                "UI Initialization Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private JPanel createSidebar() {
        try {
            JPanel sidebar = new JPanel();
            sidebar.setPreferredSize(new Dimension(250, getHeight()));
            sidebar.setBackground(SIDEBAR_COLOR);
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Navigation buttons
            String[] menuItems = {"New Order", "Track Orders", "Order History"};
            for (String item : menuItems) {
                JButton menuButton = createMenuButton(item);
                sidebar.add(menuButton);
                sidebar.add(Box.createVerticalStrut(10));
            }
            
            // Logout button at bottom
            sidebar.add(Box.createVerticalGlue());
            JButton logoutButton = createMenuButton("Logout");
            logoutButton.addActionListener(e -> handleLogout());
            sidebar.add(logoutButton);
            
            return sidebar;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating sidebar", e);
            JOptionPane.showMessageDialog(
                this, 
                "Unable to create navigation menu. Please restart the application.", 
                "Menu Creation Error", 
                JOptionPane.ERROR_MESSAGE
            );
            return new JPanel(); // Return an empty panel to prevent null pointer
        }
    }
    
    private JButton createMenuButton(String text) {
        try {
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
                try {
                    if (!text.equals("Logout")) {
                        cardLayout.show(contentPanel, text);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error navigating to panel: " + text, ex);
                    JOptionPane.showMessageDialog(
                        ClientPanel.this, 
                        "Unable to navigate to " + text + " panel.", 
                        "Navigation Error", 
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            });
            
            return button;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating menu button: " + text, e);
            JOptionPane.showMessageDialog(
                this, 
                "Failed to create menu button. Please restart the application.", 
                "Button Creation Error", 
                JOptionPane.ERROR_MESSAGE
            );
            return new JButton(text); // Return a basic button to prevent null pointer
        }
    }
    
    private void handleLogout() {
        try {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.getContentPane().removeAll();
                
                LoginPanel3 loginPanel = new LoginPanel3();
                frame.getContentPane().add(loginPanel);
                frame.revalidate();
                frame.repaint();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during logout process", e);
            JOptionPane.showMessageDialog(
                this, 
                "An error occurred while logging out. Please close the application manually.", 
                "Logout Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}