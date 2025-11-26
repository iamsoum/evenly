package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.Group;
import com.evenly.models.User;
import com.evenly.services.GroupService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class CreateGroupController {

  @FXML
  private TextField groupNameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private TextField emailField;

  @FXML
  private ListView<String> memberListView;

  private final GroupService groupService = new GroupService();
  private final ObservableList<String> members = FXCollections.observableArrayList();
  private Group currentGroup;

  @FXML
  private void initialize() {
    memberListView.setItems(members);
  }

  @FXML
  private void handleAddMember() {
    String email = emailField.getText().trim();

    if (email.isEmpty()) {
      showAlert("Error", "Please enter an email address.");
      return;
    }

    // First, ensure group is created
    if (currentGroup == null) {
      String groupName = groupNameField.getText().trim();
      String description = descriptionField.getText().trim();

      if (groupName.isEmpty()) {
        showAlert("Error", "Please enter a group name first.");
        return;
      }

      try {
        currentGroup = groupService.createGroup(groupName, description);
      } catch (SQLException e) {
        showAlert("Error", "Failed to create group: " + e.getMessage());
        return;
      }
    }

    try {
      User user = groupService.getUserByEmail(email);

      if (user == null) {
        showAlert("Error", "User doesn't exist");
        return;
      }

      // Add member to group
      groupService.addMemberToGroup(currentGroup.getId(), user.getId());
      members.add(user.getName() + " (" + user.getEmail() + ")");
      emailField.clear();

    } catch (SQLException e) {
      showAlert("Error", "Failed to add member: " + e.getMessage());
    }
  }

  @FXML
  private void handleSave() {
    if (currentGroup == null) {
      String groupName = groupNameField.getText().trim();
      String description = descriptionField.getText().trim();

      if (groupName.isEmpty()) {
        showAlert("Error", "Please enter a group name.");
        return;
      }

      try {
        currentGroup = groupService.createGroup(groupName, description);
      } catch (SQLException e) {
        showAlert("Error", "Failed to create group: " + e.getMessage());
        return;
      }
    }

    // Navigate back to dashboard
    try {
      EvenlyApp.setRoot("ui/dashboard");
    } catch (IOException e) {
      showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
    }
  }

  @FXML
  private void handleCancel() {
    try {
      EvenlyApp.setRoot("ui/dashboard");
    } catch (IOException e) {
      showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
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
