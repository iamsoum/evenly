package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.services.TransactionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class AddExpenseController {

  @FXML
  private TextField descriptionField;

  @FXML
  private TextField amountField;

  private final TransactionService transactionService = new TransactionService();
  private static int currentGroupId;

  public static void setCurrentGroupId(int groupId) {
    currentGroupId = groupId;
  }

  @FXML
  private void handleBack() {
    try {
      GroupDashboardController.setSelectedGroupId(currentGroupId);
      EvenlyApp.setRoot("ui/group_dashboard");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleSave() {
    String description = descriptionField.getText().trim();
    String amountText = amountField.getText().trim();

    if (description.isEmpty() || amountText.isEmpty()) {
      showAlert("Error", "Please fill in all fields.");
      return;
    }

    try {
      double amount = Double.parseDouble(amountText);
      if (amount <= 0) {
        showAlert("Error", "Amount must be greater than 0.");
        return;
      }

      // Using payer_id = 1 as placeholder - in real app, would use current user
      transactionService.createTransaction(currentGroupId, 1, description, amount, "CAD");

      // Navigate back to group dashboard
      GroupDashboardController.setSelectedGroupId(currentGroupId);
      EvenlyApp.setRoot("ui/group_dashboard");
    } catch (NumberFormatException e) {
      showAlert("Error", "Please enter a valid amount.");
    } catch (SQLException e) {
      e.printStackTrace();
      showAlert("Error", "Failed to save expense: " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      showAlert("Error", "Failed to navigate: " + e.getMessage());
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
