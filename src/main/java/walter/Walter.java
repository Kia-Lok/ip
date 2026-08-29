package walter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
        ui = new Ui();
        storage = new Storage();
        loadTasks();
    }

    /**
     * Creates Walter with a specified storage component for isolated environments.
     *
     * @param storage Storage used to load and save tasks.
     */
    public Walter(Storage storage) {
        ui = new Ui();
        this.storage = storage;
        loadTasks();
    }

    /**
     * Loads saved tasks, falling back to an empty list with a user-facing warning.
     */
    private void loadTasks() {
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
     * Returns Walter's GUI greeting and any warning raised while loading saved tasks.
     *
     * @return Greeting suitable for the first Walter dialog.
     */
    public String getWelcomeMessage() {
        String greeting = "Howdy! I'm Walter!\nWhat can I do for you?";
        return loadWarning == null ? greeting : greeting + "\n" + loadWarning;
    }

    /**
     * Processes one command through Walter's existing parser and command architecture.
     *
     * @param input Raw command supplied by a GUI or another presentation layer.
     * @return User-facing response produced by the command.
     */
    public String getResponse(String input) {
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
        try (PrintStream responseOutput = new PrintStream(
                responseBytes, true, StandardCharsets.UTF_8)) {
            Ui responseUi = new Ui(responseOutput);
            try {
                Command command = Parser.parse(input);
                command.execute(tasks, responseUi, storage);
            } catch (DukeException exception) {
                responseUi.showError(exception.getMessage());
            }
        }
        return responseBytes.toString(StandardCharsets.UTF_8).stripTrailing();
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
     *
     * @param args Command-line arguments; Walter does not currently use them.
     */
    public static void main(String[] args) {
        new Walter().run();
    }
}
