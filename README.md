# Logistics Management System

## 🚚 Overview

Logistics Management System is a comprehensive Java-based application designed to streamline logistics operations, providing distinct interfaces for administrators, drivers, and clients. Built with robust database connectivity and a modern, responsive Swing-based UI, this system offers end-to-end management of logistics processes.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-007396?style=for-the-badge&logo=java&logoColor=white)

## ✨ Key Features

### 1. Multi-Role Access System
- **Admin Panel**: Comprehensive dashboard for system management
- **Driver Panel**: Real-time delivery tracking and management
- **Client Panel**: Order tracking and interaction

### 2. Admin Dashboard Highlights
- Interactive charts displaying:
  - Hourly revenue
  - Delivery statistics
  - Warehouse capacity
- Real-time performance metrics
- Order management (new, current, completed)

### 3. Database Integration
- Seamless MySQL connectivity
- Comprehensive data management
- Secure connection handling
- Real-time data updates

### 4. User Authentication
- Secure login system
- Role-based access control
- User-specific interfaces

### 5. Exception Handling
- Robust error management
- Graceful error reporting
- Logging mechanisms

## 🛠 Technologies Used

- **Language**: Java
- **UI Framework**: Java Swing
- **Database**: MySQL
- **Charting Library**: JFreeChart
- **Logging**: java.util.logging

## 📦 System Architecture

### Main Components
- `MainUI`: Central application frame
- `AdminPanel`: Administrative dashboard
- `DashboardPanel`: Dynamic statistics and visualization
- `LoginPanel3`: Authentication interface
- `DatabaseConnection`: Database utility

### Design Patterns
- Model-View-Controller (MVC)
- Singleton (Database Connection)
- Dependency Injection

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 11+
- MySQL Server
- JFreeChart Library
- JDBC MySQL Connector

### Installation Steps
1. Clone the repository
```bash
git clone https://github.com/yourusername/logistics-management-system.git
```

2. Set up MySQL database
```sql
CREATE DATABASE logistics_management;
USE logistics_management;

-- Create necessary tables
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_type ENUM('admin', 'driver', 'client') NOT NULL
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

3. Configure database connection in `DatabaseConnection.java`
4. Add required libraries
5. Compile and run `MainUI`

## 📊 Dashboard Features

### Warehouse Management
- Real-time capacity tracking
- Utilization percentage visualization
- Inventory status monitoring

### Revenue Tracking
- Hourly revenue charts
- Daily income summaries
- Performance trend analysis

### Delivery Metrics
- Active delivery tracking
- Delivery completion rates
- Driver performance indicators

## 🔒 Security Features
- Secure user authentication
- Role-based access control
- Encrypted database connections
- Comprehensive exception handling

## 🤝 Contributing
1. Fork the repository
2. Create your feature branch
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. Commit your changes
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. Push to the branch
   ```bash
   git push origin feature/AmazingFeature
   ```
5. Open a Pull Request

## 📝 License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details.

## 🛠️ Troubleshooting
- Ensure all libraries are in the build path
- Check MySQL connection settings
- Verify JDK compatibility

## 🌟 Project Status
Active development and maintenance

---

**Created with ❤️ by The Analyzer**

## 📧 Contact
- Your Name: rudraydave@gmail.com
- Project Link: https://github.com/RDisCoding/logistics-management-system
