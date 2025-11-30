package com.evenly;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class EvenlyApp extends Application {

  private static Scene scene;

  @Override
  public void start(Stage stage) throws IOException {
    scene = new Scene(loadFXML("ui/login"), 800, 600);
    stage.setScene(scene);
    stage.setTitle("Evenly");

    // Set app icon
    try {
      Image icon = new Image(EvenlyApp.class.getResourceAsStream("/com/evenly/images/logo.png"));
      stage.getIcons().add(icon);
    } catch (Exception e) {
      System.err.println("Could not load app icon: " + e.getMessage());
      e.printStackTrace();
    }

    // Set dock icon for macOS
    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
      try {
        java.net.URL iconUrl = EvenlyApp.class.getResource("/com/evenly/images/logo.png");
        java.awt.Image awtIcon = java.awt.Toolkit.getDefaultToolkit().getImage(iconUrl);
        java.awt.Taskbar.getTaskbar().setIconImage(awtIcon);
      } catch (Exception e) {
        System.err.println("Could not set dock icon: " + e.getMessage());
      }
    }
    stage.show();
  }

  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFXML(fxml));
  }

  private static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(EvenlyApp.class.getResource(fxml + ".fxml"));
    return fxmlLoader.load();
  }

  public static void main(String[] args) {
    launch();
  }
}
