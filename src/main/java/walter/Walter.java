package walter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import walter.command.Command;
import walter.command.CommandCategory;
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
    private CommandCategory lastCommandCategory = CommandCategory.NORMAL;

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
        String greeting = "Jesse, focus. We’ve got things to cook.\n"
                + "Tell me what needs to be done, and I’ll handle the list.\n"
                + "Start with `list`, `todo <task>`, or "
                + "`event <task> /from <start> /to <end>`.";
        return loadWarning == null ? greeting : greeting + "\n" + loadWarning;
    }

    /**
     * Processes one command through Walter's existing parser and command architecture.
     *
     * @param input Raw command supplied by a GUI or another presentation layer.
     * @return User-facing response produced by the command.
     */
    public String getResponse(String input) {
        lastCommandCategory = CommandCategory.NORMAL;
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
        try (PrintStream responseOutput = new PrintStream(
                responseBytes, true, StandardCharsets.UTF_8)) {
            Ui responseUi = new Ui(responseOutput);
            try {
                Command command = Parser.parse(input);
                command.execute(tasks, responseUi, storage);
                lastCommandCategory = command.getCategory();
            } catch (DukeException exception) {
                lastCommandCategory = CommandCategory.ERROR;
                responseUi.showError(getGuiErrorMessage(exception.getMessage()));
            }
        }
        return responseBytes.toString(StandardCharsets.UTF_8).stripTrailing();
    }

    /**
     * Gives known GUI errors themed wording without changing parser or command semantics.
     * Specific syntax diagnostics remain unchanged when collapsing them would lose useful detail.
     *
     * @param message Original error message produced by the application core.
     * @return Error text suitable for Walter's GUI response bubble.
     */
    private String getGuiErrorMessage(String message) {
        if (message.equals("Unknown command.")) {
            return "Jesse, stop giving me unknown commands.";
        }
        if (message.equals("Task number is required.")
                || message.equals("Task number must be an integer.")
                || message.equals("Task number is out of range.")) {
            return "Jesse, that task doesn't exist.";
        }
        if (message.equals("Todo description cannot be empty.")
                || message.equals("Deadline description cannot be empty.")
                || message.equals("Event description cannot be empty.")) {
            return "Jesse, I need a task. Give me something to work with.";
        }
        if (message.equals("Deadline date/time cannot be empty.")
                || message.equals("Deadline date must be in yyyy-MM-dd format.")
                || message.equals("Event date/time cannot be empty.")
                || message.equals("Event start cannot be empty.")
                || message.equals("Event end cannot be empty.")
                || message.equals("Date is required for the on command.")
                || message.equals("Date must be in yyyy-MM-dd format.")) {
            return "Jesse, that date makes no sense.";
        }
        if (message.startsWith("Walter could not save")) {
            return "Jesse, something went wrong.";
        }
        return message;
    }

    /**
     * Returns the category of the most recently processed GUI response.
     *
     * @return Last successful command category, or {@link CommandCategory#ERROR} when processing
     *         the latest input failed.
     */
    public CommandCategory getLastCommandCategory() {
        return lastCommandCategory;
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
