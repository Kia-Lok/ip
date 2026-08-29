package walter.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Reusable FXML-backed message bubble for one side of a Walter conversation.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    /**
     * Loads one dialog bubble and assigns its message text.
     *
     * @param text Message displayed in the bubble.
     */
    private DialogBox(String text) {
        FXMLLoader loader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Walter could not load a dialog box.", exception);
        }
        dialog.setText(text);
    }

    /**
     * Creates a right-aligned user message.
     *
     * @param text User input to display.
     * @return Styled user dialog.
     */
    public static DialogBox getUserDialog(String text) {
        return new DialogBox(text);
    }

    /**
     * Creates a left-aligned Walter response.
     *
     * @param text Walter output to display.
     * @return Styled Walter dialog.
     */
    public static DialogBox getWalterDialog(String text) {
        DialogBox dialogBox = new DialogBox(text);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        dialogBox.dialog.getStyleClass().add("walter-bubble");
        return dialogBox;
    }
}
