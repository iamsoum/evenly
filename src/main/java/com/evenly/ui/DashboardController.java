package com.evenly.ui;

import com.evenly.EvenlyApp;
import javafx.fxml.FXML;
import java.io.IOException;

public class DashboardController {

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
}
