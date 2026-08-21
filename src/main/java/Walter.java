import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * The entry point for the Walter chatbot.
 */
public class Walter {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final int MAX_TASKS = 100;
    private static final Path SAVE_FILE = Path.of("data", "walter.txt");
    private static final String FIELD_SEPARATOR = "\t";
    private static final DateTimeFormatter DATE_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);

    public static void main(String[] args) {
        String banner = """
                ██╗    ██╗ █████╗ ██╗  ████████╗███████╗██████╗
                ██║    ██║██╔══██╗██║  ╚══██╔══╝██╔════╝██╔══██╗
                ██║ █╗ ██║███████║██║     ██║   █████╗  ██████╔╝
                ██║███╗██║██╔══██║██║     ██║   ██╔══╝  ██╔══██╗
                ╚███╔███╔╝██║  ██║███████╗██║   ███████╗██║  ██║
                 ╚══╝╚══╝ ╚═╝  ╚═╝╚══════╝╚═╝   ╚══════╝╚═╝  ╚═╝
                """;
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;
        String loadWarning = null;
        try {
            taskCount = loadTasks(tasks);
        } catch (DukeException exception) {
            loadWarning = "Walter could not load saved tasks. Starting with an empty list.";
        } catch (IOException exception) {
            loadWarning = "Walter could not access saved tasks. Starting with an empty list.";
        }

        System.out.println(SEPARATOR);
        System.out.print(banner);
        System.out.println("Howdy! I'm Walter!");
        System.out.println("What can I do for you?");
        if (loadWarning != null) {
            System.out.println(loadWarning);
        }
        System.out.println(SEPARATOR);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().strip();
            System.out.println(SEPARATOR);

            if (command.equals("bye")) {
                System.out.println("Walter: Bye. Hope to see you again soon!");
                System.out.println(SEPARATOR);
                break;
            }

            try {
                taskCount = executeCommand(command, tasks, taskCount);
                if (changesTaskState(command)) {
                    saveTasks(tasks, taskCount);
                }
            } catch (DukeException exception) {
                System.out.println(exception.getMessage());
            }
            System.out.println(SEPARATOR);
        }
    }

    /**
     * Executes one non-exit command after validating its arguments.
     *
     * @param command User command with leading and trailing whitespace removed.
     * @param tasks Array storing all tasks.
     * @param taskCount Current number of stored tasks.
     * @return Updated number of stored tasks.
     * @throws DukeException If the command or any argument is invalid.
     */
    private static int executeCommand(String command, Task[] tasks, int taskCount)
            throws DukeException {
        if (command.isBlank()) {
            throw new DukeException("Command cannot be blank.");
        }

        if (command.equals("list")) {
            printTaskList(tasks, taskCount);
            return taskCount;
        }
        if (isCommand(command, "on")) {
            printDeadlinesOn(command, tasks, taskCount);
            return taskCount;
        }
        if (isCommand(command, "done") || isCommand(command, "mark")) {
            Task task = getTask(command, tasks, taskCount);
            task.markAsDone();
            System.out.println("Walter has marked this task as done:");
            System.out.println(task);
            return taskCount;
        }
        if (isCommand(command, "unmark")) {
            Task task = getTask(command, tasks, taskCount);
            task.markAsNotDone();
            System.out.println("Walter has marked this task as not done yet:");
            System.out.println(task);
            return taskCount;
        }
        if (isCommand(command, "delete")) {
            return deleteTask(command, tasks, taskCount);
        }
        if (isCommand(command, "todo")) {
            return addTask(tasks, taskCount, parseTodo(command));
        }
        if (isCommand(command, "deadline")) {
            return addTask(tasks, taskCount, parseDeadline(command));
        }
        if (isCommand(command, "event")) {
            return addTask(tasks, taskCount, parseEvent(command));
        }

        throw new DukeException("Unknown command.");
    }

    /**
     * Checks whether input is exactly a command word or is followed by whitespace and arguments.
     */
    private static boolean isCommand(String input, String commandWord) {
        return input.equals(commandWord)
                || input.startsWith(commandWord)
                && input.length() > commandWord.length()
                && Character.isWhitespace(input.charAt(commandWord.length()));
    }

    /**
     * Reports whether a successfully executed command changes persisted task state.
     */
    private static boolean changesTaskState(String command) {
        return isCommand(command, "todo")
                || isCommand(command, "deadline")
                || isCommand(command, "event")
                || isCommand(command, "done")
                || isCommand(command, "mark")
                || isCommand(command, "unmark")
                || isCommand(command, "delete");
    }

    /**
     * Finds a delimiter that appears as a complete whitespace-separated token.
     */
    private static int findDelimiter(String text, String delimiter, int fromIndex) {
        int delimiterIndex = text.indexOf(delimiter, fromIndex);
        while (delimiterIndex >= 0) {
            int delimiterEnd = delimiterIndex + delimiter.length();
            boolean hasValidStart = delimiterIndex == 0
                    || Character.isWhitespace(text.charAt(delimiterIndex - 1));
            boolean hasValidEnd = delimiterEnd == text.length()
                    || Character.isWhitespace(text.charAt(delimiterEnd));
            if (hasValidStart && hasValidEnd) {
                return delimiterIndex;
            }
            delimiterIndex = text.indexOf(delimiter, delimiterEnd);
        }
        return -1;
    }

    /**
     * Prints all stored tasks in their user-facing order.
     */
    private static void printTaskList(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            System.out.println("There are currently no items on your list.");
            return;
        }

        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + ". " + tasks[i]);
        }
    }

    /**
     * Prints Deadline tasks matching the ISO date supplied to the {@code on} command.
     */
    private static void printDeadlinesOn(String command, Task[] tasks, int taskCount)
            throws DukeException {
        String dateText = command.substring("on".length()).strip();
        if (dateText.isEmpty()) {
            throw new DukeException("Date is required for the on command.");
        }

        LocalDate queryDate;
        try {
            queryDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new DukeException("Date must be in yyyy-MM-dd format.");
        }

        int matchCount = 0;
        for (int i = 0; i < taskCount; i++) {
            if (tasks[i] instanceof Deadline deadline && deadline.getBy().equals(queryDate)) {
                if (matchCount == 0) {
                    System.out.println(
                            "Here are the deadlines on "
                                    + queryDate.format(DATE_DISPLAY_FORMATTER) + ":");
                }
                matchCount++;
                System.out.println(matchCount + ". " + deadline);
            }
        }

        if (matchCount == 0) {
            System.out.println(
                    "There are no deadlines on "
                            + queryDate.format(DATE_DISPLAY_FORMATTER) + ".");
        }
    }

    /**
     * Parses and validates a Todo command without changing task state.
     */
    private static Todo parseTodo(String command) throws DukeException {
        String description = command.substring("todo".length()).strip();
        if (description.isEmpty()) {
            throw new DukeException("Todo description cannot be empty.");
        }
        return new Todo(description);
    }

    /**
     * Parses and validates a Deadline command without changing task state.
     */
    private static Deadline parseDeadline(String command) throws DukeException {
        String taskDetails = command.substring("deadline".length()).strip();
        if (taskDetails.isEmpty()) {
            throw new DukeException("Deadline description cannot be empty.");
        }

        int delimiterIndex = findDelimiter(taskDetails, "/by", 0);
        if (delimiterIndex < 0) {
            throw new DukeException("Deadline requires /by.");
        }

        String description = taskDetails.substring(0, delimiterIndex).strip();
        String byText = taskDetails.substring(delimiterIndex + "/by".length()).strip();
        if (description.isEmpty()) {
            throw new DukeException("Deadline description cannot be empty.");
        }
        if (byText.isEmpty()) {
            throw new DukeException("Deadline date/time cannot be empty.");
        }
        return new Deadline(description, parseDeadlineDate(byText));
    }

    /**
     * Parses an ISO deadline date and converts parse failures into a user-facing error.
     */
    private static LocalDate parseDeadlineDate(String dateText) throws DukeException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new DukeException("Deadline date must be in yyyy-MM-dd format.");
        }
    }

    /**
     * Parses and validates either supported Event command format.
     */
    private static Event parseEvent(String command) throws DukeException {
        String taskDetails = command.substring("event".length()).strip();
        if (taskDetails.isEmpty()) {
            throw new DukeException("Event description cannot be empty.");
        }

        int atDelimiterIndex = findDelimiter(taskDetails, "/at", 0);
        if (atDelimiterIndex >= 0) {
            String description = taskDetails.substring(0, atDelimiterIndex).strip();
            String at = taskDetails.substring(atDelimiterIndex + "/at".length()).strip();
            if (description.isEmpty()) {
                throw new DukeException("Event description cannot be empty.");
            }
            if (at.isEmpty()) {
                throw new DukeException("Event date/time cannot be empty.");
            }
            return new Event(description, at);
        }

        int fromDelimiterIndex = findDelimiter(taskDetails, "/from", 0);
        if (fromDelimiterIndex < 0) {
            if (findDelimiter(taskDetails, "/to", 0) >= 0) {
                throw new DukeException("Event requires /from command when given /to command.");
            }
            throw new DukeException("Event requires /at or /from and /to.");
        }
        int toDelimiterIndex = findDelimiter(
                taskDetails, "/to", fromDelimiterIndex + "/from".length());
        if (toDelimiterIndex < 0) {
            throw new DukeException("Event requires /to command when given /from command.");
        }

        String description = taskDetails.substring(0, fromDelimiterIndex).strip();
        String from = taskDetails.substring(
                fromDelimiterIndex + "/from".length(), toDelimiterIndex).strip();
        String to = taskDetails.substring(toDelimiterIndex + "/to".length()).strip();
        if (description.isEmpty()) {
            throw new DukeException("Event description cannot be empty.");
        }
        if (from.isEmpty()) {
            throw new DukeException("Event start cannot be empty.");
        }
        if (to.isEmpty()) {
            throw new DukeException("Event end cannot be empty.");
        }
        return new Event(description, from, to);
    }

    /**
     * Returns the task selected by a validated 1-based task number.
     */
    private static Task getTask(String command, Task[] tasks, int taskCount) throws DukeException {
        return tasks[getTaskIndex(command, taskCount)];
    }

    /**
     * Parses and validates a 1-based task number, returning its zero-based array index.
     */
    private static int getTaskIndex(String command, int taskCount) throws DukeException {
        int commandWordEnd = 0;
        while (commandWordEnd < command.length()
                && !Character.isWhitespace(command.charAt(commandWordEnd))) {
            commandWordEnd++;
        }
        if (commandWordEnd == command.length()
                || command.substring(commandWordEnd).isBlank()) {
            throw new DukeException("Task number is required.");
        }

        String taskNumberText = command.substring(commandWordEnd).strip();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(taskNumberText);
        } catch (NumberFormatException exception) {
            throw new DukeException("Task number must be an integer.");
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new DukeException("Task number is out of range.");
        }
        return taskNumber - 1;
    }

    /**
     * Removes one validated task and shifts later tasks to keep the list consecutive.
     */
    private static int deleteTask(String command, Task[] tasks, int taskCount)
            throws DukeException {
        int taskIndex = getTaskIndex(command, taskCount);
        Task removedTask = tasks[taskIndex];
        int tasksToShift = taskCount - taskIndex - 1;
        if (tasksToShift > 0) {
            System.arraycopy(tasks, taskIndex + 1, tasks, taskIndex, tasksToShift);
        }

        int updatedTaskCount = taskCount - 1;
        tasks[updatedTaskCount] = null;
        System.out.println("Walter has removed this task:");
        System.out.println(removedTask);
        String taskWord = updatedTaskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + updatedTaskCount + " " + taskWord + " in the list.");
        return updatedTaskCount;
    }

    /**
     * Adds a validated task and displays its representation and the updated task count.
     */
    private static int addTask(Task[] tasks, int taskCount, Task task) throws DukeException {
        if (taskCount >= tasks.length) {
            throw new DukeException("Task list is full.");
        }

        tasks[taskCount] = task;
        int updatedTaskCount = taskCount + 1;
        System.out.println("Walter has added this task:");
        System.out.println(task);
        String taskWord = updatedTaskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + updatedTaskCount + " " + taskWord + " in the list.");
        return updatedTaskCount;
    }

    /**
     * Loads all tasks from disk without modifying the supplied array unless every record is valid.
     */
    private static int loadTasks(Task[] tasks) throws IOException, DukeException {
        if (!Files.exists(SAVE_FILE)) {
            return 0;
        }

        List<String> lines = Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);
        if (lines.size() > tasks.length) {
            throw new DukeException("Saved task list exceeds the supported capacity.");
        }

        Task[] loadedTasks = new Task[tasks.length];
        for (int i = 0; i < lines.size(); i++) {
            loadedTasks[i] = parseStoredTask(lines.get(i));
        }
        System.arraycopy(loadedTasks, 0, tasks, 0, lines.size());
        return lines.size();
    }

    /**
     * Reconstructs one task from the Level-7 storage representation.
     */
    private static Task parseStoredTask(String line) throws DukeException {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (fields.length < 3) {
            throw new DukeException("Malformed saved task record.");
        }

        boolean isDone;
        if (fields[1].equals("1")) {
            isDone = true;
        } else if (fields[1].equals("0")) {
            isDone = false;
        } else {
            throw new DukeException("Malformed saved task status.");
        }

        Task task;
        if (fields[0].equals("T") && fields.length == 3) {
            task = new Todo(requireStoredText(fields[2]));
        } else if (fields[0].equals("D") && fields.length == 4) {
            task = new Deadline(
                    requireStoredText(fields[2]),
                    parseDeadlineDate(requireStoredText(fields[3])));
        } else if (fields[0].equals("E") && fields.length == 5 && fields[2].equals("AT")) {
            task = new Event(requireStoredText(fields[3]), requireStoredText(fields[4]));
        } else if (fields[0].equals("E") && fields.length == 6
                && fields[2].equals("FROM_TO")) {
            task = new Event(
                    requireStoredText(fields[3]),
                    requireStoredText(fields[4]),
                    requireStoredText(fields[5]));
        } else {
            throw new DukeException("Unknown saved task record.");
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Decodes one required text field and rejects empty persisted values.
     */
    private static String requireStoredText(String field) throws DukeException {
        String text = unescapeField(field);
        if (text.isEmpty()) {
            throw new DukeException("Saved task text cannot be empty.");
        }
        return text;
    }

    /**
     * Writes the current task list to the relative Level-7 save file.
     */
    private static void saveTasks(Task[] tasks, int taskCount) throws DukeException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            lines.add(toStoredTask(tasks[i]));
        }

        try {
            Files.createDirectories(SAVE_FILE.getParent());
            Files.write(SAVE_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DukeException("Walter could not save your tasks.");
        }
    }

    /**
     * Converts one task to the deterministic Level-7 storage representation.
     */
    private static String toStoredTask(Task task) throws DukeException {
        String status = task.isDone() ? "1" : "0";
        String description = escapeField(task.getDescription());
        if (task instanceof Todo) {
            return String.join(FIELD_SEPARATOR, "T", status, description);
        }
        if (task instanceof Deadline deadline) {
            return String.join(
                    FIELD_SEPARATOR, "D", status, description, deadline.getBy().toString());
        }
        if (task instanceof Event event && event.isAtFormat()) {
            return String.join(
                    FIELD_SEPARATOR, "E", status, "AT", description, escapeField(event.getAt()));
        }
        if (task instanceof Event event) {
            return String.join(
                    FIELD_SEPARATOR,
                    "E",
                    status,
                    "FROM_TO",
                    description,
                    escapeField(event.getFrom()),
                    escapeField(event.getTo()));
        }
        throw new DukeException("Walter could not save an unknown task type.");
    }

    /**
     * Escapes control characters that would otherwise interfere with the line-based format.
     */
    private static String escapeField(String field) {
        return field.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Restores a text field escaped by {@link #escapeField(String)}.
     */
    private static String unescapeField(String field) throws DukeException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char current = field.charAt(i);
            if (current != '\\') {
                result.append(current);
                continue;
            }
            if (i + 1 >= field.length()) {
                throw new DukeException("Malformed saved task text.");
            }

            char escaped = field.charAt(++i);
            switch (escaped) {
            case '\\' -> result.append('\\');
            case 't' -> result.append('\t');
            case 'n' -> result.append('\n');
            case 'r' -> result.append('\r');
            default -> throw new DukeException("Malformed saved task text.");
            }
        }
        return result.toString();
    }
}
