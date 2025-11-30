package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.SplitStrategy;
import com.evenly.models.User;
import com.evenly.services.MembershipService;
import com.evenly.services.SplitStrategyService;
import com.evenly.services.TransactionService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseController {

  @FXML
  private TextField descriptionField;

  @FXML
  private TextField amountField;

  @FXML
  private Button paidByButton;

  @FXML
  private Button splitButton;

  private final TransactionService transactionService = new TransactionService();
  private final MembershipService membershipService = new MembershipService();
  private final SplitStrategyService splitStrategyService = new SplitStrategyService();

  private static int currentGroupId;
  private User selectedPayer;
  private SplitStrategy selectedSplitStrategy;

  public static void setCurrentGroupId(int groupId) {
    currentGroupId = groupId;
  }

  @FXML
  private void initialize() {
    // Try to set default payer (first member)
    try {
      List<User> members = membershipService.getMembersOfGroup(currentGroupId);
      if (!members.isEmpty()) {
        selectedPayer = members.get(0);
        paidByButton.setText(selectedPayer.getName());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // Set default split strategy (equal)
    splitButton.setText("equally");
  }

  @FXML
  private void handlePaidByClick() {
    showMemberSelectionModal();
  }

  @FXML
  private void handleSplitClick() {
    showSplitStrategyModal();
  }

  private void showMemberSelectionModal() {
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Select Payer");

    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setAlignment(Pos.CENTER);

    Label title = new Label("Who paid for this expense?");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    VBox membersList = new VBox(10);

    try {
      List<User> members = membershipService.getMembersOfGroup(currentGroupId);
      for (User member : members) {
        Button memberButton = new Button(member.getName());
        memberButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-min-width: 200px;");
        memberButton.setOnAction(e -> {
          selectedPayer = member;
          paidByButton.setText(member.getName());
          modal.close();
        });
        membersList.getChildren().add(memberButton);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      showAlert("Error", "Failed to load group members: " + e.getMessage());
    }

    container.getChildren().addAll(title, membersList);

    Scene scene = new Scene(container, 300, 400);
    modal.setScene(scene);
    modal.showAndWait();
  }

  private void showSplitStrategyModal() {
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Choose Split Strategy");

    VBox container = new VBox(20);
    container.setPadding(new Insets(30));
    container.setAlignment(Pos.CENTER);

    Label title = new Label("How should this be split?");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    // Equal split button
    Button equalButton = new Button("Split Equally");
    equalButton.setStyle("-fx-font-size: 14px; -fx-padding: 15 30; -fx-min-width: 200px;");
    equalButton.setOnAction(e -> {
      try {
        selectedSplitStrategy = splitStrategyService.createSplitStrategy("EQUAL", null);
        splitButton.setText("equally");
        modal.close();
      } catch (SQLException ex) {
        ex.printStackTrace();
        showAlert("Error", "Failed to create split strategy: " + ex.getMessage());
      }
    });

    // Percentage split button
    Button percentageButton = new Button("Split by Percentage");
    percentageButton.setStyle("-fx-font-size: 14px; -fx-padding: 15 30; -fx-min-width: 200px;");
    percentageButton.setOnAction(e -> {
      modal.close();
      showPercentageSplitModal();
    });

    container.getChildren().addAll(title, equalButton, percentageButton);

    Scene scene = new Scene(container, 350, 250);
    modal.setScene(scene);
    modal.showAndWait();
  }

  private void showPercentageSplitModal() {
    Stage modal = new Stage();
    modal.initOwner(splitButton.getScene().getWindow()); // Set owner to main window
    modal.initModality(Modality.WINDOW_MODAL); // Use WINDOW_MODAL instead of APPLICATION_MODAL
    modal.setTitle("Percentage Split");

    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setPrefWidth(400); // Set preferred width

    Label title = new Label("Enter percentage for each member");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    Map<User, TextField> percentageFields = new HashMap<>();

    try {
      List<User> members = membershipService.getMembersOfGroup(currentGroupId);
      int row = 0;
      for (User member : members) {
        Label nameLabel = new Label(member.getName() + ":");
        TextField percentField = new TextField();
        percentField.setText("0");
        percentField.setPrefWidth(80);
        percentField.setEditable(true);
        percentField.setFocusTraversable(true);

        // Select text when focused so it's easy to replace
        percentField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
          if (isNowFocused) {
            Platform.runLater(percentField::selectAll);
          }
        });

        grid.add(nameLabel, 0, row);
        grid.add(percentField, 1, row);
        grid.add(new Label("%"), 2, row);

        percentageFields.put(member, percentField);
        row++;
      }

      Button saveButton = new Button("Save");
      saveButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
      saveButton.setOnAction(e -> {
        try {
          // Validate percentages sum to 100
          double total = 0;
          StringBuilder jsonParams = new StringBuilder("{");
          boolean first = true;

          for (Map.Entry<User, TextField> entry : percentageFields.entrySet()) {
            String text = entry.getValue().getText().trim();
            if (text.isEmpty()) {
              showErrorAlert(modal, "Please enter a percentage for " + entry.getKey().getName());
              return;
            }

            double percent = Double.parseDouble(text);
            total += percent;

            if (!first)
              jsonParams.append(",");
            jsonParams.append("\"").append(entry.getKey().getId()).append("\":").append(percent);
            first = false;
          }
          jsonParams.append("}");

          if (Math.abs(total - 100.0) > 0.01) {
            showErrorAlert(modal, "Percentages must sum to 100%. Current total: " + total + "%");
            return;
          }

          selectedSplitStrategy = splitStrategyService.createSplitStrategy("PERCENTAGE", jsonParams.toString());
          splitButton.setText("by percentage");
          modal.close();
        } catch (NumberFormatException ex) {
          showErrorAlert(modal, "Please enter valid numbers for all percentages.");
        } catch (SQLException ex) {
          ex.printStackTrace();
          String msg = ex.getMessage();
          showErrorAlert(modal, "Failed to save split strategy: " + (msg != null ? msg : "Unknown database error"));
        }
      });

      container.getChildren().addAll(title, grid, saveButton);
    } catch (SQLException e) {
      e.printStackTrace();
      String msg = e.getMessage();
      showErrorAlert(modal, "Failed to load group members: " + (msg != null ? msg : "Unknown database error"));
    }

    Scene scene = new Scene(container);
    modal.setScene(scene);
    // Ensure modal has a reasonable minimum size
    modal.setMinWidth(350);
    modal.setMinHeight(300);
    modal.showAndWait();
  }

  private void showErrorAlert(Stage owner, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.initOwner(owner);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    // Fix for empty alert dialogs on some platforms
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.showAndWait();
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

    if (selectedPayer == null) {
      showAlert("Error", "Please select who paid for this expense.");
      return;
    }

    try {
      double amount = Double.parseDouble(amountText);
      if (amount <= 0) {
        showAlert("Error", "Amount must be greater than 0.");
        return;
      }

      Integer splitStrategyId = selectedSplitStrategy != null ? selectedSplitStrategy.getId() : null;
      transactionService.createTransaction(currentGroupId, selectedPayer.getId(), description, amount, "CAD",
          splitStrategyId);

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
