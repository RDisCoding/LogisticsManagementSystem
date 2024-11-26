package LogisticsManagementSystem;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Random;
import java.awt.GradientPaint;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel3 extends JPanel {
    // UI Components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton forgotPasswordButton;
    private GradientPanel mainPanel;
    private JLabel logoLabel;
    private ForgotPasswordPanel forgotPasswordPanel;
    
    // UI Colors
    private static final Color DARK_THEME = new Color(18, 18, 18);
    private static final Color ACCENT_COLOR = new Color(255, 255, 255);
    private static final Color PURPLE_ACCENT = new Color(138, 43, 226);
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR2 = new Color(136, 46, 224);
    
    // Email configuration
    private static final String EMAIL_HOST = "smtp.gmail.com";
    private static final String EMAIL_PORT = "587";
    private static final String EMAIL_USERNAME = "your-email@gmail.com";
    private static final String EMAIL_PASSWORD = "your-email-password";

    // Logo configuration
    private ImageIcon logo;
    private static final int LOGO_WIDTH = 175;
    private static final int LOGO_HEIGHT = 85;

    public LoginPanel3() {
        initComponents();
        loadLogo("C:/Users/rudra/OneDrive/Desktop/Stuff/WebD Stuff/Logo/logo.png");
    }

    public void loadLogo(String path) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                LOGO_WIDTH, LOGO_HEIGHT, Image.SCALE_SMOOTH);
            logo = new ImageIcon(scaledImage);
            logoLabel.setIcon(logo);
        } catch (Exception e) {
            logoLabel.setText("Logo");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(DARK_THEME);
        
        forgotPasswordPanel = new ForgotPasswordPanel();

        mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());
        
        createModernComponents();
        layoutComponents();
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int w = getWidth();
            int h = getHeight();
            
            Color color1 = DARK_THEME;
            Color color2 = new Color(40, 40, 48);
            GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }
    private static class JRoundTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder;

        public JRoundTextField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            setOpaque(false);
            setText(placeholder);
            setForeground(Color.GRAY);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(ACCENT_COLOR);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            
            super.paintComponent(g);
            g2.dispose();
        }
    }
    private static class JRoundPasswordField extends JPasswordField {
        private String placeholder;
        private boolean showingPlaceholder;

        public JRoundPasswordField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            setOpaque(false);
            setEchoChar((char) 0);
            setText(placeholder);
            setForeground(Color.GRAY);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (showingPlaceholder) {
                        setText("");
                        setEchoChar('•');
                        setForeground(ACCENT_COLOR);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (String.valueOf(getPassword()).isEmpty()) {
                        setEchoChar((char) 0);
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            
            super.paintComponent(g);
            g2.dispose();
        }
    }
    private static class JRoundButton extends JButton {
        public JRoundButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            
            super.paintComponent(g);
            g2.dispose();
        }
    }
    private void createModernComponents() {
        // Logo
        logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 32));
        logoLabel.setForeground(ACCENT_COLOR);
        
        // Email field
        emailField = new JRoundTextField(20, "Enter your email");
        emailField.setBackground(new Color(30, 30, 30));
        emailField.setForeground(ACCENT_COLOR);
        emailField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        emailField.setCaretColor(ACCENT_COLOR);
        
        // Password field
        passwordField = new JRoundPasswordField(20, "Enter your password");
        passwordField.setBackground(new Color(30, 30, 30));
        passwordField.setForeground(ACCENT_COLOR);
        passwordField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        passwordField.setCaretColor(ACCENT_COLOR);
        
        // Buttons
        loginButton = new JRoundButton("LOG IN");
        loginButton.setBackground(PURPLE_ACCENT);
        loginButton.setForeground(ACCENT_COLOR);
        loginButton.setFocusPainted(false);
        
        registerButton = new JRoundButton("REGISTER");
        registerButton.setBackground(PURPLE_ACCENT);
        registerButton.setForeground(ACCENT_COLOR);
        registerButton.setFocusPainted(false);
        
        forgotPasswordButton = new JButton("FORGOT YOUR PASSWORD?");
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setForeground(PURPLE_ACCENT);
        forgotPasswordButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> showRegistrationDialog());
        forgotPasswordButton.addActionListener(e -> showForgotPasswordPanel());
    }
    
    private void showForgotPasswordPanel() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password", true);
        dialog.setContentPane(forgotPasswordPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);
        
        // Logo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(logoLabel, gbc);
        
        // Email field
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 30, 5, 30);
        mainPanel.add(emailField, gbc);
        
        // Password field
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 30, 5, 30);
        mainPanel.add(loginButton, gbc);
        
        // Register button
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 30, 5, 30);
        mainPanel.add(registerButton, gbc);
        
        // Forgot password button
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 30, 5, 30);
        mainPanel.add(forgotPasswordButton, gbc);
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("user_type"),
                        rs.getString("email"),
                        rs.getString("phone")
                    );
                    handleSuccessfulLogin(user);
                } else {
                    showError("Invalid email or password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    private void showRegistrationDialog() {
        RegisterPanel registrationPanel = new RegisterPanel();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New User", true);
        dialog.setContentPane(registrationPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField emailInput = new JRoundTextField(20, "Enter Email");
        JButton submitButton = new JRoundButton("Send Reset Link");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Enter your email:"), gbc);
        
        gbc.gridy = 1;
        panel.add(emailInput, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(submitButton, gbc);
        
        submitButton.addActionListener(e -> {
            String email = emailInput.getText();
            if (isValidEmail(email)) {
                sendPasswordResetEmail(email);
                dialog.dispose();
            } else {
                showError("Please enter a valid email address");
            }
        });
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void sendPasswordResetEmail(String email) {
        String resetToken = generateResetToken();
        storeResetToken(email, resetToken);
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", EMAIL_HOST);
        props.put("mail.smtp.port", EMAIL_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Reset Request");
            message.setText("Your password reset token is: " + resetToken);

            Transport.send(message);
            JOptionPane.showMessageDialog(this, 
                "Password reset instructions have been sent to your email.",
                "Reset Email Sent",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (MessagingException e) {
            e.printStackTrace();
            showError("Failed to send reset email: " + e.getMessage());
        }
    }

    private void handleSuccessfulLogin(User user) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        
        switch (user.getUserType().toLowerCase()) {
            case "admin":
                frame.add(new AdminPanel(user));
                break;
            case "client":
                frame.add(new ClientPanel(user));
                break;
            case "warehouse":
                frame.add(new WManagerPanel(user));
                break;
            case "driver":
                frame.add(new DriverPanel(user));
                break;
            default:
                showError("Unknown user type: " + user.getUserType());
                return;
        }
        
        frame.revalidate();
        frame.repaint();
    }

    // Utility methods
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String generateResetToken() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void storeResetToken(String email, String token) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, token);
                pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 3600000));
                pstmt.setString(3, email);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to process reset request: " + e.getMessage());
        }
    }
}