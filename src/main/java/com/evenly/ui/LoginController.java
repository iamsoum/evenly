package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;

public class LoginController {

  @FXML
  private TextField usernameField;

  @FXML
  private TextField emailField;

  private final UserService userService = new UserService();

  @FXML
  private void handleLogin() {
    String username = usernameField.getText();
    String email = emailField.getText();

    if (username.isEmpty() || email.isEmpty()) {
      showAlert("Error", "Please enter both username and email.");
      return;
    }

    try {
      userService.loginOrRegister(username, email);
      EvenlyApp.setRoot("ui/dashboard");
    } catch (SQLException | java.io.IOException e) {
      e.printStackTrace();
      showAlert("Login Failed", "Could not login or register user: " + e.getMessage());
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
