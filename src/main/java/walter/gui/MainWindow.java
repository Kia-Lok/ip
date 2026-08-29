package walter.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import walter.Walter;
import walter.command.CommandCategory;

/**
 * Controls Walter's main conversation window.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Walter walter;

    /**
     * Configures automatic scrolling after the FXML controls are loaded.
     */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty().addListener(
                observable -> scrollPane.setVvalue(1.0));
    }

    /**
     * Supplies the Walter instance used to process commands and shows its greeting.
     *
     * @param walter Walter application facade shared throughout this window.
     */
    public void setWalter(Walter walter) {
        this.walter = walter;
        dialogContainer.getChildren().add(DialogBox.getWalterDialog(walter.getWelcomeMessage()));
        userInput.requestFocus();
    }

    /**
     * Sends the text-field content to Walter and appends both sides of the exchange.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = walter.getResponse(input);
        CommandCategory commandCategory = walter.getLastCommandCategory();
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getWalterDialog(response, commandCategory));
        userInput.clear();
    }
}
