package walter;

import java.io.IOException;

import walter.command.Command;
import walter.parser.Parser;
import walter.storage.Storage;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Coordinates the Walter chatbot's user interface, parser, task list, and storage.
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
        ui = new Ui();
        storage = new Storage();
        try {
            tasks = new TaskList(storage.load());
        } catch (DukeException exception) {
            tasks = new TaskList();
            loadWarning = "Walter could not load saved tasks. Starting with an empty list.";
        } catch (IOException exception) {
            tasks = new TaskList();
            loadWarning = "Walter could not access saved tasks. Starting with an empty list.";
        }
    }

    /**
     * Runs the command loop until the user exits or input ends.
     */
    public void run() {
        ui.showWelcome(loadWarning);
        while (ui.hasNextCommand()) {
            ui.showSeparator();
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
                    break;
                }
            } catch (DukeException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showSeparator();
        }
    }

    /**
     * Starts Walter.
     */
    public static void main(String[] args) {
        new Walter().run();
    }
}
