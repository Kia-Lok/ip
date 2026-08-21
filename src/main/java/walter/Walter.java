package walter;

import java.io.IOException;

import walter.command.Command;
import walter.parser.Parser;
import walter.storage.Storage;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Coordinates Walter's user interface, command parser, task list, and persistent storage.
 * This class owns the application lifecycle and delegates command-specific behavior to
 * {@link Command} objects.
 */
public class Walter {
    private final Ui ui;
    private final Storage storage;
    private TaskList tasks;
    private String loadWarning;

    /**
     * Creates Walter and loads any tasks saved by an earlier run.
     */
    public Walter() {
        this.ui = new Ui();
        this.storage = new Storage();
        try {
            this.tasks = new TaskList(this.storage.load());
        } catch (DukeException exception) {
            this.tasks = new TaskList();
            this.loadWarning = "Walter could not load saved tasks. Starting with an empty list.";
        } catch (IOException exception) {
            this.tasks = new TaskList();
            this.loadWarning = "Walter could not access saved tasks. Starting with an empty list.";
        }
    }

    /**
     * Runs the command loop until the user exits or input ends.
     */
    public void run() {
        this.ui.showWelcome(this.loadWarning);
        while (this.ui.hasNextCommand()) {
            this.ui.showSeparator();
            try {
                Command command = Parser.parse(this.ui.readCommand());
                command.execute(this.tasks, this.ui, this.storage);
                if (command.isExit()) {
                    break;
                }
            } catch (DukeException exception) {
                this.ui.showError(exception.getMessage());
            }
            this.ui.showSeparator();
        }
    }

    /**
     * Starts Walter.
     *
     * @param args Command-line arguments; Walter does not currently use them.
     */
    public static void main(String[] args) {
        new Walter().run();
    }
}
