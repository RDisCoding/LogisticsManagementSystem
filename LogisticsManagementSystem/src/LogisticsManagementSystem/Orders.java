package LogisticsManagementSystem;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

public class Orders extends JPanel {
    private User currentUser;
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);

    
    public Orders(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Add your driver assignment-specific components here
    }
}
