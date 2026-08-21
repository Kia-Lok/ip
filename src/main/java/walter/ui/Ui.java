package walter.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import walter.task.Deadline;
import walter.task.Task;

/**
 * Handles all terminal input and output for Walter.
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

    /**
     * Creates a terminal UI that reads standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Reports whether another command is available.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next raw command line.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays Walter's banner, greeting, and an optional loading warning.
     */
    public void showWelcome(String loadWarning) {
        showSeparator();
        System.out.print(BANNER);
        System.out.println("Howdy! I'm Walter!");
        System.out.println("What can I do for you?");
        if (loadWarning != null) {
            System.out.println(loadWarning);
        }
        showSeparator();
    }

    /**
     * Displays one output separator.
     */
    public void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /**
     * Displays Walter's exit message and closing separator.
     */
    public void showGoodbye() {
        System.out.println("Walter: Bye. Hope to see you again soon!");
        showSeparator();
    }

    /**
     * Displays an expected user-facing error.
     */
    public void showError(String message) {
        System.out.println(message);
    }

    /**
     * Displays all tasks in their current order.
     */
    public void showTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("There are currently no items on your list.");
            return;
        }

        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Displays Deadline tasks matching a date, or the existing no-match message.
     */
    public void showDeadlinesOn(LocalDate date, List<Deadline> deadlines) {
        String displayDate = date.format(DATE_DISPLAY_FORMATTER);
        if (deadlines.isEmpty()) {
            System.out.println("There are no deadlines on " + displayDate + ".");
            return;
        }

        System.out.println("Here are the deadlines on " + displayDate + ":");
        for (int i = 0; i < deadlines.size(); i++) {
            System.out.println((i + 1) + ". " + deadlines.get(i));
        }
    }

    /**
     * Displays confirmation that a task was added.
     */
    public void showAddedTask(Task task, int taskCount) {
        System.out.println("Walter has added this task:");
        System.out.println(task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was deleted.
     */
    public void showDeletedTask(Task task, int taskCount) {
        System.out.println("Walter has removed this task:");
        System.out.println(task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was marked done.
     */
    public void showMarkedTask(Task task) {
        System.out.println("Walter has marked this task as done:");
        System.out.println(task);
    }

    /**
     * Displays confirmation that a task was marked not done.
     */
    public void showUnmarkedTask(Task task) {
        System.out.println("Walter has marked this task as not done yet:");
        System.out.println(task);
    }

    /**
     * Displays the current task count with the correct singular or plural noun.
     */
    private void showTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + taskCount + " " + taskWord + " in the list.");
    }
}
