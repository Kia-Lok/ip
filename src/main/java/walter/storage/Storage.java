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
import walter.place.Place;
import walter.task.Deadline;
import walter.task.Event;
import walter.task.Task;
import walter.task.Todo;

/**
 * Loads and saves Walter tasks using the Level-7 line-based format.
 * A default instance uses {@code data/walter.txt}; an alternate path can be supplied for
 * isolated environments such as tests.
 */
public class Storage {
    private static final Path DEFAULT_SAVE_FILE = Path.of("data", "walter.txt");
    private static final Path DEFAULT_PLACE_FILE = Path.of("data", "places.txt");
    private static final String FIELD_SEPARATOR = "\t";

    private final Path saveFile;
    private final Path placeFile;

    /**
     * Creates storage using Walter's production save-file location.
     */
    public Storage() {
        this(DEFAULT_SAVE_FILE, DEFAULT_PLACE_FILE);
    }

    /**
     * Creates storage using a specified save-file location.
     *
     * @param saveFile Location used to load and save tasks.
     */
    public Storage(Path saveFile) {
        this(saveFile, saveFile.resolveSibling("places.txt"));
    }

    /**
     * Creates storage using specified task and place save-file locations.
     *
     * @param saveFile Location used to load and save tasks.
     * @param placeFile Location used to load and save places.
     */
    public Storage(Path saveFile, Path placeFile) {
        this.saveFile = saveFile;
        this.placeFile = placeFile;
    }

    /**
     * Loads every persisted task, or returns an empty list when no save file exists.
     *
     * @return Tasks reconstructed from the save file in their stored order.
     * @throws IOException If the save file exists but cannot be read.
     * @throws DukeException If any stored task record is malformed or unsupported.
     */
    public List<Task> load() throws IOException, DukeException {
        if (!Files.exists(saveFile)) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (String line : Files.readAllLines(saveFile, StandardCharsets.UTF_8)) {
            tasks.add(parseStoredTask(line));
        }
        return tasks;
    }

    /**
     * Saves all tasks after creating the parent data directory when needed.
     *
     * @param tasks Tasks to persist in their current order.
     * @throws DukeException If a task type is unsupported or the save file cannot be written.
     */
    public void save(List<Task> tasks) throws DukeException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(toStoredTask(task));
        }

        try {
            Path parentDirectory = saveFile.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Files.write(saveFile, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DukeException("Walter could not save your tasks.");
        }
    }

    /**
     * Loads saved places in insertion order, or an empty list when no place file exists.
     *
     * @return Places reconstructed from the separate place save file.
     * @throws DukeException If the place file cannot be read or contains a malformed record.
     */
    public List<Place> loadPlaces() throws DukeException {
        if (!Files.exists(placeFile)) {
            return new ArrayList<>();
        }

        try {
            List<Place> places = new ArrayList<>();
            for (String line : Files.readAllLines(placeFile, StandardCharsets.UTF_8)) {
                places.add(parseStoredPlace(line));
            }
            return places;
        } catch (IOException exception) {
            throw new DukeException("Walter could not load saved places.");
        }
    }

    /**
     * Saves places to their separate save file in insertion order.
     *
     * @param places Places to persist.
     * @throws DukeException If the place file cannot be written.
     */
    public void savePlaces(List<Place> places) throws DukeException {
        List<String> lines = new ArrayList<>();
        for (Place place : places) {
            lines.add(String.join(
                    FIELD_SEPARATOR,
                    "P",
                    escapeField(place.getName()),
                    escapeField(place.getAddress())));
        }

        try {
            Path parentDirectory = placeFile.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Files.write(placeFile, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DukeException("Walter could not save your places.");
        }
    }

    /**
     * Reconstructs one place from its tab-separated storage record.
     */
    private Place parseStoredPlace(String line) throws DukeException {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (fields.length != 3 || !fields[0].equals("P")) {
            throw new DukeException("Malformed saved place record.");
        }
        return new Place(
                requireStoredPlaceText(fields[1]),
                requireStoredPlaceText(fields[2]));
    }

    /**
     * Reconstructs one task while rejecting malformed or unknown records.
     *
     * @param line Encoded save-file record.
     * @return Task reconstructed from the record.
     * @throws DukeException If the record has invalid fields, status, type, or escaped text.
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
     *
     * @param task Task to encode.
     * @return Tab-separated storage record for the task.
     * @throws DukeException If the task has an unsupported runtime type.
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
     *
     * @param dateText Stored ISO date text.
     * @return Parsed deadline date.
     * @throws DukeException If the stored date is invalid.
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
     *
     * @param field Encoded storage field.
     * @return Decoded non-empty text.
     * @throws DukeException If the field is empty or contains malformed escape syntax.
     */
    private String requireStoredText(String field) throws DukeException {
        String text = unescapeField(field, "Malformed saved task text.");
        if (text.isEmpty()) {
            throw new DukeException("Saved task text cannot be empty.");
        }
        return text;
    }

    /**
     * Decodes one required place field and rejects empty persisted values.
     */
    private String requireStoredPlaceText(String field) throws DukeException {
        String text = unescapeField(field, "Malformed saved place text.");
        if (text.isEmpty()) {
            throw new DukeException("Saved place text cannot be empty.");
        }
        return text;
    }

    /**
     * Escapes control characters that would otherwise interfere with the storage format.
     *
     * @param field Text to encode for storage.
     * @return Text with backslashes and supported control characters escaped.
     */
    private String escapeField(String field) {
        return field.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Restores a text field escaped by {@link #escapeField(String)}.
     *
     * @param field Encoded storage field.
     * @return Decoded text.
     * @throws DukeException If an escape sequence is incomplete or unsupported.
     */
    private String unescapeField(String field, String malformedTextMessage) throws DukeException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char current = field.charAt(i);
            if (current != '\\') {
                result.append(current);
                continue;
            }
            if (i + 1 >= field.length()) {
                throw new DukeException(malformedTextMessage);
            }

            char escaped = field.charAt(++i);
            switch (escaped) {
                case '\\' -> result.append('\\');
                case 't' -> result.append('\t');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                default -> throw new DukeException(malformedTextMessage);
            }
        }
        return result.toString();
    }
}
