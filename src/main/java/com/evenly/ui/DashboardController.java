package com.evenly.ui;

import com.evenly.EvenlyApp;
import com.evenly.models.Group;
import com.evenly.models.User;
import com.evenly.services.GroupService;
import com.evenly.services.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

  @FXML
  private ListView<Group> groupsListView;

  @FXML
  private Label welcomeLabel;

  private final GroupService groupService = new GroupService();
  private final ObservableList<Group> groups = FXCollections.observableArrayList();

  @FXML
  private void initialize() {
    groupsListView.setItems(groups);
    groupsListView.setCellFactory(param -> new GroupListCell());

    // Set welcome message with username
    User currentUser = SessionManager.getCurrentUser();
    if (currentUser != null && welcomeLabel != null) {
      welcomeLabel.setText("Welcome, " + currentUser.getName() + "!");
    }

    loadUserGroups();

    // Add selection listener to navigate to group dashboard on single click
    groupsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        try {
          GroupDashboardController.setSelectedGroupId(newValue.getId());
          EvenlyApp.setRoot("ui/group_dashboard");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void loadUserGroups() {
    try {
      User currentUser = SessionManager.getCurrentUser();
      if (currentUser != null) {
        List<Group> userGroups = groupService.getGroupsByUser(currentUser.getId());
        groups.setAll(userGroups);
      }
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
  private void handleCreateGroup() {
    try {
      EvenlyApp.setRoot("ui/create_group");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Custom ListCell for displaying groups
  private static class GroupListCell extends ListCell<Group> {
    @Override
    protected void updateItem(Group group, boolean empty) {
      super.updateItem(group, empty);

      if (empty || group == null) {
        setGraphic(null);
        setText(null);
      } else {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getStyleClass().add("group-item");

        Label iconLabel = new Label(group.getIcon());
        iconLabel.setStyle("-fx-font-size: 32px;");

        VBox textBox = new VBox();
        Label nameLabel = new Label(group.getName());
        nameLabel.getStyleClass().add("group-name");

        Label descLabel = new Label(group.getDescription() != null ? group.getDescription() : "No description");
        descLabel.getStyleClass().add("group-status-settled");

        textBox.getChildren().addAll(nameLabel, descLabel);
        hbox.getChildren().addAll(iconLabel, textBox);

        setGraphic(hbox);
      }
    }
  }
}
