import java.io.IOException;

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
     */
    public static void main(String[] args) {
        new Walter().run();
    }
}
