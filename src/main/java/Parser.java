import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Parses and validates Walter command input without performing I/O or changing task state.
 */
public class Parser {
    /**
     * Removes leading and trailing whitespace from one input line.
     */
    public static String normalize(String input) {
        return input.strip();
    }

    /**
     * Reports whether input consists of exactly one command word.
     */
    public static boolean isExactCommand(String input, String commandWord) {
        return normalize(input).equals(commandWord);
    }

    /**
     * Returns the first command word after rejecting blank input.
     */
    public static String getCommandWord(String input) throws DukeException {
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
     */
    public static Todo parseTodo(String input) throws DukeException {
        String description = argumentAfter(input, "todo");
        if (description.isEmpty()) {
            throw new DukeException("Todo description cannot be empty.");
        }
        return new Todo(description);
    }

    /**
     * Parses a Deadline command with an ISO date.
     */
    public static Deadline parseDeadline(String input) throws DukeException {
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
     */
    public static Event parseEvent(String input) throws DukeException {
        String taskDetails = argumentAfter(input, "event");
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
     * Parses a one-based task number and returns its zero-based index.
     */
    public static int parseTaskIndex(String input) throws DukeException {
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
     */
    public static LocalDate parseOnDate(String input) throws DukeException {
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
     */
    private static String argumentAfter(String input, String commandWord) {
        return normalize(input).substring(commandWord.length()).strip();
    }

    /**
     * Parses a Deadline date and converts failures into the existing user-facing error.
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
