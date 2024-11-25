package LogisticsManagementSystem;

public class User {
    private int userId;
    private String username;
    private String userType;
    private String email;
    private String phone;

    // Constructor
    public User(int userId, String username, String userType, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.email = email;
        this.phone = phone;
    }

    // Default constructor
    public User() {
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}