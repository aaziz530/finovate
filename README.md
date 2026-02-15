# Finovate - User Management & Password Reset

This project implements a user management system with JavaFX, featuring a secure "Forgot Password" mechanism using email verification.

## Getting Started

Follow these steps to set up and run the application:

### 1. Configure Email Settings
Before running the application, you must configure your SMTP credentials to enable password reset emails:
1. Open `src/main/java/org/esprit/finovate/services/EmailService.java`.
2. Update the `username` and `password` fields with your email and App Password.

### 2. Database Configuration
Ensure your database settings are correct:
1. Open `src/main/java/org/esprit/finovate/utils/MyDataBase.java`.
2. Verify the `USER` and `PSR` (password) match your local MySQL configuration.

### 3. Run the Application
Open your terminal in the project root directory and execute:

```bash
mvn javafx:run
```

