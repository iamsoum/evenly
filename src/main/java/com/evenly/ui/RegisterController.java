package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class RegisterController {

  @FXML
  private TextField usernameField;

  @FXML
  private TextField emailField;

  @FXML
  private PasswordField passwordField;

  private final UserService userService = new UserService();

  @FXML
  private void handleRegister() {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = passwordField.getText();

    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
      showAlert("Error", "Please fill in all fields.");
      return;
    }

    try {
      userService.register(username, email, password);
      EvenlyApp.setRoot("ui/dashboard");
    } catch (SQLException e) {
      showAlert("Registration Failed", e.getMessage());
    } catch (java.io.IOException e) {
      e.printStackTrace();
      showAlert("Error", "Could not navigate to dashboard: " + e.getMessage());
    }
  }

  @FXML
  private void handleBackToLogin() {
    try {
      EvenlyApp.setRoot("ui/login");
    } catch (java.io.IOException e) {
      e.printStackTrace();
      showAlert("Error", "Could not navigate to login: " + e.getMessage());
    }
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
