package LogisticsManagementSystem;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class RegisterPanel extends JPanel {
    // UI Components
    private JTextField[] fields;
    private JLabel[] errorLabels;
    private JComboBox<String> userTypeDropdown;
    private JButton submitButton;
    private Map<String, String> userTypeMap;

    // UI Colors
    private static final Color DARK_THEME = new Color(18, 18, 18);
    private static final Color ACCENT_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color CARD_BACKGROUND = new Color(22, 27, 34);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color ACCENT_COLOR2 = new Color(136, 46, 224);

    // Constants
    private static final String[] LABELS = {
        "Username:", "Password:", "Confirm Password:",
        "Email:", "Phone:", "User Type:"
    };

    public RegisterPanel() {
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
    	setPreferredSize(new Dimension(600, 600));
        setBackground(CARD_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setLayout(new GridBagLayout());

        fields = new JTextField[5];
        errorLabels = new JLabel[5];

        userTypeMap = new LinkedHashMap<>();
        userTypeMap.put("Client", "client");
        userTypeMap.put("Warehouse Manager", "warehouse");
        userTypeMap.put("Driver", "driver");

        userTypeDropdown = new JComboBox<>(userTypeMap.keySet().toArray(new String[0]));
        submitButton = createStyledButton("Register");
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1; // Allow components to resize horizontally

        for (int i = 0; i < LABELS.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i * 2;

            JLabel label = createStyledLabel(LABELS[i]);
            gbc.weightx = 0; // Labels should not expand
            add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1; // Input fields should expand
            if (i == 1 || i == 2) {
                fields[i] = new JPasswordField(20);
            } else if (i == 5) {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                userTypeDropdown.setBackground(BACKGROUND_COLOR);
                userTypeDropdown.setForeground(TEXT_COLOR);
                add(userTypeDropdown, gbc);
                continue;
            } else {
                fields[i] = new JTextField(20);
            }

            styleTextField(fields[i]);
            add(fields[i], gbc);

            gbc.gridy = i * 2 + 1;
            errorLabels[i] = new JLabel();
            errorLabels[i].setForeground(Color.RED);
            gbc.weightx = 0; // Error labels do not need resizing
            add(errorLabels[i], gbc);
        }

        // Add submit button
        gbc.gridx = 0;
        gbc.gridy = LABELS.length * 2;
        gbc.gridwidth = 2; // Span across both columns
        gbc.weightx = 1; // Button should expand horizontally
        gbc.insets = new Insets(15, 5, 5, 5);
        add(submitButton, gbc);
    }

    private void setupListeners() {
        submitButton.addActionListener(e -> handleRegistration());
    }

    private void handleRegistration() {
        clearErrorMessages();

        try {
            String username = fields[0].getText();
            String password = new String(((JPasswordField) fields[1]).getPassword());
            String confirmPassword = new String(((JPasswordField) fields[2]).getPassword());
            String email = fields[3].getText();
            String phone = fields[4].getText();
            String userType = userTypeMap.get(userTypeDropdown.getSelectedItem());

            // Validate all fields
            validateUsername(username, errorLabels[0]);
            validatePasswordStrength(password, errorLabels[1]);
            validateConfirmPassword(password, confirmPassword, errorLabels[2]);
            validateEmailFormat(email, errorLabels[3]);
            validatePhoneNumber(phone, errorLabels[4]);

            // If all validations pass, proceed with registration
            registerUser(username, password, email, phone, userType);

        } catch (Exception ex) {
            showError("Registration Error: " + ex.getMessage());
        }
    }

    private void registerUser(String username, String password, String email, 
                            String phone, String userType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, email, phone, user_type) " +
                          "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.setString(5, userType);

                pstmt.executeUpdate();
                showSuccess("Registration successful!");
                clearFields();
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Username or email already exists!");
            } else {
                showError("Registration failed: " + e.getMessage());
            }
        }
    }

    // Validation methods
    private void validateUsername(String username, JLabel errorLabel) throws InvalidUsernameException {
        if (username.isEmpty()) {
            errorLabel.setText("Username cannot be empty");
            throw new InvalidUsernameException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 20) {
            errorLabel.setText("Username must be 3-20 characters long");
            throw new InvalidUsernameException("Username must be 3-20 characters long");
        }
    }

    private void validatePasswordStrength(String password, JLabel errorLabel) 
            throws InvalidPasswordException {
        StringBuilder errorMessage = new StringBuilder("Password must: ");
        boolean isValid = true;

        if (password.length() < 8 || password.length() > 20) {
            errorMessage.append("\n- Be 8-20 characters long");
            isValid = false;
        }

        boolean hasLowerCase = false, hasUpperCase = false;
        boolean hasDigit = false, hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLowerCase = true;
            if (Character.isUpperCase(c)) hasUpperCase = true;
            if (Character.isDigit(c)) hasDigit = true;
            if ("!@#$%^&*()-_=+[]{}|;:,.<>?".indexOf(c) != -1) hasSpecialChar = true;
        }

        if (!hasLowerCase) {
            errorMessage.append("\n- Contain at least one lowercase letter");
            isValid = false;
        }
        if (!hasUpperCase) {
            errorMessage.append("\n- Contain at least one uppercase letter");
            isValid = false;
        }
        if (!hasDigit) {
            errorMessage.append("\n- Contain at least one digit");
            isValid = false;
        }
        if (!hasSpecialChar) {
            errorMessage.append("\n- Contain at least one special character");
            isValid = false;
        }

        if (!isValid) {
            errorLabel.setText("<html>" + errorMessage.toString().replace("\n", "<br/>") + "</html>");
            throw new InvalidPasswordException(errorMessage.toString());
        }
    }

    private void validateConfirmPassword(String password, String confirmPassword, 
            JLabel errorLabel) throws PasswordMismatchException {
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            throw new PasswordMismatchException("Passwords do not match");
        }
    }

    private void validateEmailFormat(String email, JLabel errorLabel) 
            throws InvalidEmailException {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (email.isEmpty()) {
            errorLabel.setText("Email cannot be empty");
            throw new InvalidEmailException("Email cannot be empty");
        }
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            errorLabel.setText("Invalid email format");
            throw new InvalidEmailException("Invalid email format");
        }
    }

    private void validatePhoneNumber(String phone, JLabel errorLabel) 
            throws InvalidPhoneException {
        if (phone.isEmpty()) {
            errorLabel.setText("Phone number cannot be empty");
            throw new InvalidPhoneException("Phone number cannot be empty");
        }
        if (!phone.matches("^[0-9]{10}$")) {
            errorLabel.setText("Phone number must be 10 digits");
            throw new InvalidPhoneException("Phone number must be 10 digits");
        }
    }

    // UI Helper methods
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT_COLOR2);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BACKGROUND_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR2));
        field.setCaretColor(TEXT_COLOR);
    }

    private void clearErrorMessages() {
        for (JLabel errorLabel : errorLabels) {
            if (errorLabel != null) {
                errorLabel.setText("");
            }
        }
    }

    private void clearFields() {
        for (JTextField field : fields) {
            if (field != null) {
                field.setText("");
            }
        }
        userTypeDropdown.setSelectedIndex(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom Exceptions
    private static class InvalidUsernameException extends Exception {
        public InvalidUsernameException(String message) {
            super(message);
        }
    }

    private static class InvalidPasswordException extends Exception {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    private static class PasswordMismatchException extends Exception {
        public PasswordMismatchException(String message) {
            super(message);
        }
    }

    private static class InvalidEmailException extends Exception {
        public InvalidEmailException(String message) {
            super(message);
        }
    }

    private static class InvalidPhoneException extends Exception {
        public InvalidPhoneException(String message) {
            super(message);
        }
    }
}