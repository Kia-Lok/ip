package walter.ui;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import walter.task.Deadline;
import walter.task.Task;

/**
 * Handles Walter's terminal input and presents all user-facing output.
 */
public class Ui {
    private static final String SEPARATOR =
            "____________________________________________________________";
    private static final DateTimeFormatter DATE_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
    private static final String BANNER = """
            ██╗    ██╗ █████╗ ██╗  ████████╗███████╗██████╗
            ██║    ██║██╔══██╗██║  ╚══██╔══╝██╔════╝██╔══██╗
            ██║ █╗ ██║███████║██║     ██║   █████╗  ██████╔╝
            ██║███╗██║██╔══██║██║     ██║   ██╔══╝  ██╔══██╗
            ╚███╔███╔╝██║  ██║███████╗██║   ███████╗██║  ██║
             ╚══╝╚══╝ ╚═╝  ╚═╝╚══════╝╚═╝   ╚══════╝╚═╝  ╚═╝
            """;

    private final Scanner scanner;
    private final PrintStream output;
    private final boolean isTerminal;

    /**
     * Creates a terminal UI that reads standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
        output = System.out;
        isTerminal = true;
    }

    /**
     * Creates an output-only UI for presenting one command response.
     *
     * @param output Destination for user-facing output.
     */
    public Ui(PrintStream output) {
        scanner = null;
        this.output = output;
        isTerminal = false;
    }

    /**
     * Reports whether another command is available.
     *
     * @return {@code true} if standard input contains another line.
     */
    public boolean hasNextCommand() {
        return scanner != null && scanner.hasNextLine();
    }

    /**
     * Reads the next raw command line.
     *
     * @return Next line from standard input without parsing or normalization.
     */
    public String readCommand() {
        if (scanner == null) {
            throw new IllegalStateException("This UI does not read commands.");
        }
        return scanner.nextLine();
    }

    /**
     * Displays Walter's banner, greeting, and an optional loading warning.
     *
     * @param loadWarning Warning to display after the greeting, or {@code null} for none.
     */
    public void showWelcome(String loadWarning) {
        showSeparator();
        output.print(BANNER);
        output.println("Howdy! I'm Walter!");
        output.println("What can I do for you?");
        if (loadWarning != null) {
            output.println(loadWarning);
        }
        showSeparator();
    }

    /**
     * Displays one output separator.
     */
    public void showSeparator() {
        output.println(SEPARATOR);
    }

    /**
     * Displays Walter's exit message and closing separator.
     */
    public void showGoodbye() {
        output.println("Walter: Bye. Hope to see you again soon!");
        if (isTerminal) {
            showSeparator();
        }
    }

    /**
     * Displays an expected user-facing error.
     *
     * @param message Error message to display.
     */
    public void showError(String message) {
        output.println(message);
    }

    /**
     * Displays all tasks in their current order.
     *
     * @param tasks Tasks to display in numbered order.
     */
    public void showTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            output.println("Jesse, the board's clean. Nothing to do.");
            return;
        }

        output.println("Jesse, here's what we've got on the board:");
        showNumberedTasks(tasks);
    }

    /**
     * Displays Deadline tasks matching a date, or the existing no-match message.
     *
     * @param date Date used in the heading or no-match message.
     * @param deadlines Matching deadlines in display order.
     */
    public void showDeadlinesOn(LocalDate date, List<Deadline> deadlines) {
        String displayDate = date.format(DATE_DISPLAY_FORMATTER);
        if (deadlines.isEmpty()) {
            output.println("There are no deadlines on " + displayDate + ".");
            return;
        }

        output.println("Here are the deadlines on " + displayDate + ":");
        showNumberedTasks(deadlines);
    }

    /**
     * Displays tasks whose descriptions matched a find keyword.
     */
    public void showFindResults(List<Task> matches) {
        if (matches.isEmpty()) {
            output.println("There are no tasks matching that keyword.");
            return;
        }

        output.println("Here are the matching tasks in your list:");
        showNumberedTasks(matches);
    }

    /**
     * Displays tasks in their existing order using one-based numbering.
     *
     * @param tasks Tasks to display.
     */
    private void showNumberedTasks(List<? extends Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            output.println((i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Displays confirmation that a task was added.
     *
     * @param task Task that was added.
     * @param taskCount Number of tasks after the addition.
     */
    public void showAddedTask(Task task, int taskCount) {
        output.println("Walter has added this task:");
        output.println(task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was deleted.
     *
     * @param task Task that was deleted.
     * @param taskCount Number of tasks after the deletion.
     */
    public void showDeletedTask(Task task, int taskCount) {
        output.println("Walter has removed this task:");
        output.println(task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was marked done.
     *
     * @param task Task whose status was changed.
     */
    public void showMarkedTask(Task task) {
        output.println("Walter has marked this task as done:");
        output.println(task);
    }

    /**
     * Displays confirmation that a task was marked not done.
     *
     * @param task Task whose status was changed.
     */
    public void showUnmarkedTask(Task task) {
        output.println("Walter has marked this task as not done yet:");
        output.println(task);
    }

    /**
     * Displays the current task count with the correct singular or plural noun.
     *
     * @param taskCount Number of tasks to display.
     */
    private void showTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        output.println("Now you have " + taskCount + " " + taskWord + " in the list.");
    }
}
