package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.Group;
import com.evenly.models.Transaction;
import com.evenly.services.GroupService;
import com.evenly.services.TransactionService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GroupDashboardController {

  @FXML
  private Label groupIconLabel;

  @FXML
  private Label groupNameLabel;

  @FXML
  private ListView<String> transactionsListView;

  private final GroupService groupService = new GroupService();
  private final TransactionService transactionService = new TransactionService();
  private Group currentGroup;
  private static int selectedGroupId;

  // Static method to set the group ID before navigating
  public static void setSelectedGroupId(int groupId) {
    selectedGroupId = groupId;
  }

  @FXML
  private void initialize() {
    loadGroupData();
    loadTransactions();
  }

  private void loadGroupData() {
    try {
      currentGroup = groupService.getGroupById(selectedGroupId);
      if (currentGroup != null) {
        groupNameLabel.setText(currentGroup.getName());
        groupIconLabel.setText(currentGroup.getIcon());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void loadTransactions() {
    try {
      List<Transaction> transactions = transactionService.getTransactionsByGroup(selectedGroupId);
      transactionsListView.getItems().clear();

      if (transactions.isEmpty()) {
        transactionsListView.getItems().add("No expenses yet");
      } else {
        for (Transaction transaction : transactions) {
          String displayText = String.format("%s - $%.2f",
              transaction.getDescription(),
              transaction.getAmount());
          transactionsListView.getItems().add(displayText);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      transactionsListView.getItems().add("Error loading expenses");
    }
  }

  @FXML
  private void handleChooseIcon() {
    showIconSelectionModal();
  }

  private void showIconSelectionModal() {
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Choose Icon");

    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setAlignment(Pos.CENTER);

    Label title = new Label("Select an icon for your group");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    GridPane iconGrid = new GridPane();
    iconGrid.setHgap(10);
    iconGrid.setVgap(10);
    iconGrid.setAlignment(Pos.CENTER);

    // Popular emoji icons for groups
    String[] icons = {
        "📁", "🏠", "🎉", "🍕", "✈️", "🎓",
        "💼", "🏖️", "🎮", "🎬", "🍔", "☕",
        "🚗", "🏃", "🎵", "📚", "💰", "🎨",
        "🏋️", "🍺", "🎯", "⚽", "🎸", "🌍"
    };

    int row = 0;
    int col = 0;
    for (String icon : icons) {
      Button iconButton = new Button(icon);
      iconButton.setStyle("-fx-font-size: 24px; -fx-min-width: 50px; -fx-min-height: 50px; -fx-cursor: hand;");
      iconButton.setOnAction(e -> {
        selectIcon(icon);
        modal.close();
      });
      iconGrid.add(iconButton, col, row);
      col++;
      if (col >= 6) {
        col = 0;
        row++;
      }
    }

    container.getChildren().addAll(title, iconGrid);

    Scene scene = new Scene(container, 500, 400);
    modal.setScene(scene);
    modal.showAndWait();
  }

  private void selectIcon(String icon) {
    try {
      groupService.updateGroupIcon(currentGroup.getId(), icon);
      currentGroup.setIcon(icon);
      groupIconLabel.setText(icon);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleLogout() {
    try {
      EvenlyApp.setRoot("ui/login");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleHome() {
    try {
      EvenlyApp.setRoot("ui/dashboard");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleCreateGroup() {
    try {
      EvenlyApp.setRoot("ui/create_group");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void handleAddExpense() {
    try {
      AddExpenseController.setCurrentGroupId(currentGroup.getId());
      EvenlyApp.setRoot("ui/add_expense");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
