package LogisticsManagementSystem;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ForgotPasswordPanel extends JPanel {
    private static final String EMAIL_HOST = "smtp.gmail.com";
    private static final String EMAIL_PORT = "587";
    private static final String EMAIL_USERNAME = "randomidiot075@gmail.com";
    private static final String EMAIL_PASSWORD = "howcanthispasswordbesostrong";

    private JTextField emailField;
    private JButton sendResetLinkButton;

    public ForgotPasswordPanel() {
        initComponents();
        layoutComponents();
        addActionListeners();
    }

    private void initComponents() {
        setBackground(new Color(30, 30, 30)); // Set dark background color

        emailField = new JTextField(20);
        emailField.setBackground(new Color(40, 40, 40)); // Set input field background color
        emailField.setForeground(Color.WHITE); // Set input field text color
        emailField.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1)); // Set input field border color

        sendResetLinkButton = new JButton("Send Reset Link");
        sendResetLinkButton.setBackground(new Color(120, 60, 180)); // Set button background color
        sendResetLinkButton.setForeground(Color.WHITE); // Set button text color
        sendResetLinkButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Set button padding
    }

    private void layoutComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel("Enter your email:"));
        add(emailField);
        add(sendResetLinkButton);
    }

    private void addActionListeners() {
        sendResetLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                if (isValidEmail(email)) {
                    sendPasswordResetEmail(email);
                } else {
                    showError("Please enter a valid email address.");
                }
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        // Validate the email
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get the user's password
            String query = "SELECT password FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // Fetch the user's password
                        String password = rs.getString("password");

                        // Display the password in a dialog box
                        JOptionPane.showMessageDialog(this,
                            "Your password is: " + password,
                            "Password Retrieved",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // No user found with the given email
                        showError("No user found with the given email address.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error retrieving password: " + e.getMessage());
        }
    }

    private void storeResetToken(String email, String token) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, token);
                pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 3600000)); // Token expires in 1 hour
                pstmt.setString(3, email);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    showError("No user found with the given email.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showPasswordDirectly(email); // Fetch and display the password on error
        }
    }

    private void showPasswordDirectly(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String password = rs.getString("password");
                        JOptionPane.showMessageDialog(this,
                            "Password: " + password,
                            "Direct Access",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showError("No user found with the given email.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error retrieving password: " + e.getMessage());
        }
    }

    

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}