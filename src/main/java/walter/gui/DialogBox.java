package walter.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import walter.command.CommandCategory;

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
        return getWalterDialog(text, CommandCategory.NORMAL);
    }

    /**
     * Creates a left-aligned Walter response with a category-specific visual accent.
     *
     * @param text Walter output to display.
     * @param category Command category that produced the output.
     * @return Styled Walter dialog.
     */
    public static DialogBox getWalterDialog(String text, CommandCategory category) {
        DialogBox dialogBox = new DialogBox(text);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        dialogBox.dialog.getStyleClass().add("reply-label");
        dialogBox.applyCategoryStyle(category);
        return dialogBox;
    }

    /**
     * Adds the CSS class corresponding to a response category.
     *
     * @param category Category associated with the response.
     */
    private void applyCategoryStyle(CommandCategory category) {
        switch (category) {
            case ADD:
                dialog.getStyleClass().add("add-label");
                break;
            case STATE_CHANGE:
                dialog.getStyleClass().add("state-change-label");
                break;
            case DELETE:
                dialog.getStyleClass().add("delete-label");
                break;
            case ERROR:
                dialog.getStyleClass().add("error-label");
                break;
            case NORMAL:
                break;
            default:
                break;
        }
    }
}
