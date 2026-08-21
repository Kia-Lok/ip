package walter.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import walter.DukeException;
import walter.command.AddCommand;
import walter.command.Command;
import walter.command.DeleteCommand;
import walter.command.ExitCommand;
import walter.command.ListCommand;
import walter.command.MarkCommand;
import walter.command.OnCommand;
import walter.command.UnmarkCommand;
import walter.task.Deadline;
import walter.task.Event;
import walter.task.Todo;

/**
 * Translates raw user input into validated, executable {@link Command} objects.
 * Parsing constructs command arguments such as tasks, indexes, and dates without performing
 * input/output, mutating the task list, or saving data.
 */
public class Parser {
    /**
     * Parses one input line into the corresponding executable command.
     *
     * @param input Raw command line entered by the user.
     * @return Command representing the validated input.
     * @throws DukeException If the input is blank, unknown, or has invalid command syntax.
     */
    public static Command parse(String input) throws DukeException {
        String command = normalize(input);
        String commandWord = getCommandWord(command);
        if (commandWord.equals("list") && isExactCommand(command, "list")) {
            return new ListCommand();
        }
        if (commandWord.equals("bye") && isExactCommand(command, "bye")) {
            return new ExitCommand();
        }
        if (commandWord.equals("on")) {
            return new OnCommand(parseOnDate(command));
        }
        if (commandWord.equals("done") || commandWord.equals("mark")) {
            return new MarkCommand(parseTaskIndex(command));
        }
        if (commandWord.equals("unmark")) {
            return new UnmarkCommand(parseTaskIndex(command));
        }
        if (commandWord.equals("delete")) {
            return new DeleteCommand(parseTaskIndex(command));
        }
        if (commandWord.equals("todo")) {
            return new AddCommand(parseTodo(command));
        }
        if (commandWord.equals("deadline")) {
            return new AddCommand(parseDeadline(command));
        }
        if (commandWord.equals("event")) {
            return new AddCommand(parseEvent(command));
        }
        throw new DukeException("Unknown command.");
    }

    /**
     * Removes leading and trailing whitespace from one input line.
     *
     * @param input Input text to normalize.
     * @return Input with leading and trailing whitespace removed.
     */
    private static String normalize(String input) {
        return input.strip();
    }

    /**
     * Reports whether input consists of exactly one command word.
     *
     * @param input Input text to compare.
     * @param commandWord Expected command word.
     * @return {@code true} if the normalized input equals the command word exactly.
     */
    private static boolean isExactCommand(String input, String commandWord) {
        return normalize(input).equals(commandWord);
    }

    /**
     * Returns the first command word after rejecting blank input.
     *
     * @param input Input from which to extract the command word.
     * @return First whitespace-delimited word in the normalized input.
     * @throws DukeException If the input is blank.
     */
    private static String getCommandWord(String input) throws DukeException {
        String command = normalize(input);
        if (command.isBlank()) {
            throw new DukeException("Command cannot be blank.");
        }

        int commandWordEnd = 0;
        while (commandWordEnd < command.length()
                && !Character.isWhitespace(command.charAt(commandWordEnd))) {
            commandWordEnd++;
        }
        return command.substring(0, commandWordEnd);
    }

    /**
     * Parses a Todo command.
     *
     * @param input Complete Todo command.
     * @return Todo containing the parsed description.
     * @throws DukeException If the Todo description is empty.
     */
    private static Todo parseTodo(String input) throws DukeException {
        String description = argumentAfter(input, "todo");
        if (description.isEmpty()) {
            throw new DukeException("Todo description cannot be empty.");
        }
        return new Todo(description);
    }

    /**
     * Parses a Deadline command with an ISO date.
     *
     * @param input Complete Deadline command.
     * @return Deadline containing the parsed description and date.
     * @throws DukeException If the description or date is missing, the {@code /by} delimiter is
     *         absent, or the date is not a valid ISO date.
     */
    private static Deadline parseDeadline(String input) throws DukeException {
        String taskDetails = argumentAfter(input, "deadline");
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
     * Parses either supported Event command representation.
     *
     * @param input Complete Event command using either {@code /at} or {@code /from} and
     *         {@code /to}.
     * @return Event containing the parsed description and time details.
     * @throws DukeException If required delimiters or event details are missing.
     */
    private static Event parseEvent(String input) throws DukeException {
        String taskDetails = argumentAfter(input, "event");
        if (taskDetails.isEmpty()) {
            throw new DukeException("Event description cannot be empty.");
        }

        int atDelimiterIndex = findDelimiter(taskDetails, "/at", 0);
        if (atDelimiterIndex >= 0) {
            String description = taskDetails.substring(0, atDelimiterIndex).strip();
            String atTime = taskDetails.substring(atDelimiterIndex + "/at".length()).strip();
            if (description.isEmpty()) {
                throw new DukeException("Event description cannot be empty.");
            }
            if (atTime.isEmpty()) {
                throw new DukeException("Event date/time cannot be empty.");
            }
            return new Event(description, atTime);
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
        String startTime = taskDetails.substring(
                fromDelimiterIndex + "/from".length(), toDelimiterIndex).strip();
        String endTime = taskDetails.substring(toDelimiterIndex + "/to".length()).strip();
        if (description.isEmpty()) {
            throw new DukeException("Event description cannot be empty.");
        }
        if (startTime.isEmpty()) {
            throw new DukeException("Event start cannot be empty.");
        }
        if (endTime.isEmpty()) {
            throw new DukeException("Event end cannot be empty.");
        }
        return new Event(description, startTime, endTime);
    }

    /**
     * Parses a one-based task number and returns its zero-based index.
     *
     * @param input Complete command containing a task number.
     * @return Zero-based task index corresponding to the entered number.
     * @throws DukeException If the task number is missing or is not an integer.
     */
    private static int parseTaskIndex(String input) throws DukeException {
        String command = normalize(input);
        int commandWordEnd = getCommandWord(command).length();
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
        return taskNumber - 1;
    }

    /**
     * Parses the ISO date supplied to an {@code on} command.
     *
     * @param input Complete {@code on} command.
     * @return Date requested by the command.
     * @throws DukeException If the date is missing or is not a valid ISO date.
     */
    private static LocalDate parseOnDate(String input) throws DukeException {
        String dateText = argumentAfter(input, "on");
        if (dateText.isEmpty()) {
            throw new DukeException("Date is required for the on command.");
        }
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new DukeException("Date must be in yyyy-MM-dd format.");
        }
    }

    /**
     * Returns the stripped text following a known command word.
     *
     * @param input Complete command input.
     * @param commandWord Command word at the start of the input.
     * @return Remaining input after removing the command word and surrounding whitespace.
     */
    private static String argumentAfter(String input, String commandWord) {
        return normalize(input).substring(commandWord.length()).strip();
    }

    /**
     * Parses a Deadline date and converts failures into the existing user-facing error.
     *
     * @param dateText Text expected to contain an ISO date.
     * @return Parsed date.
     * @throws DukeException If the text is not a valid ISO date.
     */
    private static LocalDate parseDeadlineDate(String dateText) throws DukeException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new DukeException("Deadline date must be in yyyy-MM-dd format.");
        }
    }

    /**
     * Finds a delimiter that appears as a complete whitespace-separated token.
     *
     * @param text Text to search.
     * @param delimiter Delimiter token to locate.
     * @param fromIndex Index at which to begin searching.
     * @return Index of the first complete delimiter token, or {@code -1} if none exists.
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
}
