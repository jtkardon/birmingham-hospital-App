package edu.wpi.teamname.controllers;

import java.sql.SQLException;
import javafx.fxml.FXML;

public class AboutPageController {
  @FXML
  public void initialize() throws SQLException {
    ParentController.titleString.set("About");
  }
}
