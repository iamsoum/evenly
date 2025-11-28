package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.User;
import com.evenly.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  private final UserService userService = new UserService();

  @FXML
  private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
      showAlert("Error", "Please enter both username and password.");
      return;
    }

    try {
      User user = userService.login(username, password);
      if (user == null) {
        showAlert("Login Failed", "Invalid username or password.");
        return;
      }
      EvenlyApp.setRoot("ui/dashboard");
    } catch (SQLException | java.io.IOException e) {
      e.printStackTrace();
      showAlert("Login Failed", "Could not login: " + e.getMessage());
    }
  }

  @FXML
  private void handleRegister() {
    try {
      EvenlyApp.setRoot("ui/register");
    } catch (java.io.IOException e) {
      e.printStackTrace();
      showAlert("Error", "Could not navigate to registration: " + e.getMessage());
    }
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
