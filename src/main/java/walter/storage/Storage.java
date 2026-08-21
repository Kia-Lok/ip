package walter.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import walter.DukeException;
import walter.task.Deadline;
import walter.task.Event;
import walter.task.Task;
import walter.task.Todo;

/**
 * Loads and saves Walter tasks using the existing Level-7 line-based format.
 */
public class Storage {
    private static final Path SAVE_FILE = Path.of("data", "walter.txt");
    private static final String FIELD_SEPARATOR = "\t";

    /**
     * Loads every persisted task, or returns an empty list when no save file exists.
     */
    public List<Task> load() throws IOException, DukeException {
        if (!Files.exists(SAVE_FILE)) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String line : Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8)) {
            tasks.add(parseStoredTask(line));
        }
        return tasks;
    }

    /**
     * Saves all tasks after creating the parent data directory when needed.
     */
    public void save(List<Task> tasks) throws DukeException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(toStoredTask(task));
        }

        try {
            Files.createDirectories(SAVE_FILE.getParent());
            Files.write(SAVE_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DukeException("Walter could not save your tasks.");
        }
    }

    /**
     * Reconstructs one task while rejecting malformed or unknown records.
     */
    private Task parseStoredTask(String line) throws DukeException {
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
                    requireStoredText(fields[2]), parseStoredDate(requireStoredText(fields[3])));
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
     * Converts one task to the existing deterministic storage representation.
     */
    private String toStoredTask(Task task) throws DukeException {
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
     * Parses a stored ISO date without leaking date parsing exceptions.
     */
    private LocalDate parseStoredDate(String dateText) throws DukeException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new DukeException("Malformed saved deadline date.");
        }
    }

    /**
     * Decodes one required text field and rejects empty persisted values.
     */
    private String requireStoredText(String field) throws DukeException {
        String text = unescapeField(field);
        if (text.isEmpty()) {
            throw new DukeException("Saved task text cannot be empty.");
        }
        return text;
    }

    /**
     * Escapes control characters that would otherwise interfere with the storage format.
     */
    private String escapeField(String field) {
        return field.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Restores a text field escaped by {@link #escapeField(String)}.
     */
    private String unescapeField(String field) throws DukeException {
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
