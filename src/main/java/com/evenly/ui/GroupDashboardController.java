package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.Group;
import com.evenly.models.Transaction;
import com.evenly.models.User;
import com.evenly.services.GroupService;
import com.evenly.services.MembershipService;
import com.evenly.services.SessionManager;
import com.evenly.services.TransactionService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

  @FXML
  private ListView<String> membersListView;

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
    // Validate session
    if (!SessionManager.isSessionValid()) {
      handleLogout();
      return;
    }
    SessionManager.refreshSession();

    loadGroupData();
    loadTransactions();
    loadGroupMembers();
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

  private void loadGroupMembers() {
    try {
      MembershipService membershipService = new MembershipService();
      List<User> members = membershipService.getMembersOfGroup(selectedGroupId);
      membersListView.getItems().clear();

      if (members.isEmpty()) {
        membersListView.getItems().add("No members yet");
      } else {
        for (User member : members) {
          String displayText = member.getName() + " (" + member.getEmail() + ")";
          membersListView.getItems().add(displayText);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      membersListView.getItems().add("Error loading members");
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

  @FXML
  private void handleAddMembers() {
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.setTitle("Add Member to Group");

    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setAlignment(Pos.CENTER);

    Label title = new Label("Add Member by Email:");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    TextField emailField = new TextField();
    emailField.setPromptText("Enter email");
    emailField.setPrefWidth(300);

    Label membersLabel = new Label("Members:");
    membersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

    javafx.scene.control.ListView<String> membersList = new javafx.scene.control.ListView<>();
    membersList.setPrefHeight(200);

    // Load current members
    try {
      List<User> members = new MembershipService().getMembersOfGroup(selectedGroupId);
      for (User member : members) {
        membersList.getItems().add(member.getName() + " (" + member.getEmail() + ")");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Button addButton = new Button("Add");
    addButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
    addButton.setOnAction(e -> {
      String email = emailField.getText().trim();
      if (email.isEmpty()) {
        showAlert("Error", "Please enter an email address.");
        return;
      }

      try {
        MembershipService membershipService = new MembershipService();
        User user = membershipService.getUserByEmail(email);

        if (user == null) {
          showAlert("Error", "User doesn't exist");
          return;
        }

        // Add member to group
        membershipService.addMemberToGroup(selectedGroupId, user.getId());
        membersList.getItems().add(user.getName() + " (" + user.getEmail() + ")");
        emailField.clear();

      } catch (SQLException ex) {
        showAlert("Error", "Failed to add member: " + ex.getMessage());
      }
    });

    HBox inputRow = new HBox(10);
    inputRow.setAlignment(Pos.CENTER);
    inputRow.getChildren().addAll(emailField, addButton);

    container.getChildren().addAll(title, inputRow, membersLabel, membersList);

    Scene scene = new Scene(container, 400, 450);
    modal.setScene(scene);
    modal.showAndWait();
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
