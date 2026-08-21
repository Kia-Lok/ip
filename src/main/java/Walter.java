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
            String command = Parser.normalize(this.ui.readCommand());
            this.ui.showSeparator();

            if (Parser.isExactCommand(command, "bye")) {
                this.ui.showGoodbye();
                break;
            }

            try {
                boolean taskStateChanged = executeCommand(command);
                if (taskStateChanged) {
                    this.storage.save(this.tasks.getTasks());
                }
            } catch (DukeException exception) {
                this.ui.showError(exception.getMessage());
            }
            this.ui.showSeparator();
        }
    }

    /**
     * Dispatches one non-exit command and reports whether persisted task state changed.
     */
    private boolean executeCommand(String command) throws DukeException {
        String commandWord = Parser.getCommandWord(command);
        if (commandWord.equals("list") && Parser.isExactCommand(command, "list")) {
            this.ui.showTaskList(this.tasks.getTasks());
            return false;
        }
        if (commandWord.equals("on")) {
            var queryDate = Parser.parseOnDate(command);
            this.ui.showDeadlinesOn(queryDate, this.tasks.getDeadlinesOn(queryDate));
            return false;
        }
        if (commandWord.equals("done") || commandWord.equals("mark")) {
            Task task = this.tasks.markAsDone(Parser.parseTaskIndex(command));
            this.ui.showMarkedTask(task);
            return true;
        }
        if (commandWord.equals("unmark")) {
            Task task = this.tasks.markAsNotDone(Parser.parseTaskIndex(command));
            this.ui.showUnmarkedTask(task);
            return true;
        }
        if (commandWord.equals("delete")) {
            Task task = this.tasks.delete(Parser.parseTaskIndex(command));
            this.ui.showDeletedTask(task, this.tasks.size());
            return true;
        }
        if (commandWord.equals("todo")) {
            return addTask(Parser.parseTodo(command));
        }
        if (commandWord.equals("deadline")) {
            return addTask(Parser.parseDeadline(command));
        }
        if (commandWord.equals("event")) {
            return addTask(Parser.parseEvent(command));
        }

        throw new DukeException("Unknown command.");
    }

    /**
     * Adds one parsed task and displays the unchanged Walter confirmation.
     */
    private boolean addTask(Task task) throws DukeException {
        this.tasks.add(task);
        this.ui.showAddedTask(task, this.tasks.size());
        return true;
    }

    /**
     * Starts Walter.
     */
    public static void main(String[] args) {
        new Walter().run();
    }
}
