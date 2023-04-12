package edu.wpi.teamname.controllers.JFXitems;

import edu.wpi.teamname.controllers.ServiceRequestController;
import edu.wpi.teamname.servicerequest.ServiceRequest;
import edu.wpi.teamname.servicerequest.requestitem.RequestItem;
import java.text.DecimalFormat;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class ReqMenuItems extends GridPane {
  private String name;
  private int id;
  private boolean add;
  private String folder;
  ServiceRequest request;
  RequestItem item;

  ServiceRequestController controller;

  @FXML VBox vBox;
  @FXML Label label;
  @FXML Label labelP;
  @FXML Button button;
  @FXML ImageView imageView;
  @FXML TextField quantity;

  public ReqMenuItems(
      RequestItem item,
      String folder,
      ServiceRequest request,
      boolean add,
      ServiceRequestController controller) {
    this.name = item.getName();
    // System.out.println(item.getItemID());
    this.id = item.getItemID();
    this.item = item;
    this.request = request;
    this.add = add;
    this.controller = controller;
    this.folder = folder;

    initialize();
  }

  public ReqMenuItems(
      RequestItem item,
      String folder,
      ServiceRequest request,
      boolean add,
      ServiceRequestController controller,
      int q) {
    this.name = item.getName();
    this.id = item.getItemID();
    this.item = item;
    this.request = request;
    this.add = add;
    this.controller = controller;
    this.folder = folder;

    initialize();

    quantity.setText("Quantity: " + String.valueOf(q));
    quantity.setDisable(true);
  }

  private void initialize() {
    try {
      imageView =
          new ImageView(
              "edu/wpi/teamname/images/" + folder + "/" + name.replace(" ", "_") + ".png");
      add(imageView, 0, 0);
      setFillWidth(imageView, true);
      setHgrow(imageView, Priority.ALWAYS);
      System.out.println(
          "edu/wpi/teamname/images/" + folder + "/" + name.replace(" ", "_") + ".png");
    } catch (IllegalArgumentException i) {
      System.out.println("illegal image urls");
      System.out.println(
          "edu/wpi/teamname/images/" + folder + "/" + name.replace(" ", "_") + ".png");
    }
    // hBox = new HBox();
    DecimalFormat df = new DecimalFormat("0.00");
    label = new Label(name.replace("_", " "));
    label.setFont(Font.font("Roboto", 32));
    label.setMinWidth(200);
    label.setMaxWidth(200);
    labelP = new Label("Price: $" + df.format(item.getPrice()));
    labelP.setFont(Font.font("Roboto", 32));
    labelP.setMinWidth(200);
    labelP.setMaxWidth(200);
    button =
        new RequestMenuItemButton(name.replace("_", " "), this.id, this, this.request, this.add);
    quantity = new TextField("");
    quantity.setPromptText("Quantity");
    quantity.setFont(Font.font("Roboto", 32));

    quantity.setStyle(
        "-fx-background-color: #D9E3F8 ;-fx-font-fill: #121C2B; -fx-background-radius: 8;-fx-border-radius: 8; -fx-border-color: #6F797A; -fx-border-width: 1");

    add(label, 1, 0);
    add(labelP, 2, 0);
    add(quantity, 3, 0);

    vBox = new VBox();
    vBox.setAlignment(Pos.CENTER);
    vBox.setMinWidth(button.getWidth() + 15);
    vBox.getChildren().add(button);
    add(vBox, 4, 0);

    quantity.setText("1");
    button.setLayoutY(this.getHeight());
    button.setPadding(new Insets(this.getHeight() / 2 - button.getHeight() / 2, 25, 0, 25));

    setFillWidth(label, true);
    setHgrow(label, Priority.ALWAYS);
    setHalignment(label, HPos.LEFT);
    setHalignment(labelP, HPos.LEFT);
    setHalignment(quantity, HPos.LEFT);
    setFillWidth(vBox, true);
    setHgrow(vBox, Priority.ALWAYS);
    button.setAlignment(Pos.CENTER_RIGHT);
    setHalignment(vBox, HPos.RIGHT);
    setStyle("-fx-background-color: #D5E3FF;" + "-fx-background-radius: 8");
    button.setStyle(
        "-fx-background-color: #555F71; "
            + "-fx-text-fill:  #FFFFFF; "
            + "-fx-background-radius: 100;"
            + " -fx-font-family: Roboto;"
            + " -fx-font-size: 32 ");
  }

  public int getQuantity() {
    int val;
    try {
      val = Integer.parseInt(quantity.getText());
    } catch (NumberFormatException e) {
      val = 1;
    }
    return val;
  }

  void delete() {
    controller.getCartBox().getChildren().remove(this);
  }
}
