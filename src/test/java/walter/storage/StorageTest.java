package walter.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import walter.DukeException;
import walter.task.Deadline;
import walter.task.Event;
import walter.task.Task;
import walter.task.Todo;

/**
 * Tests persistence using an isolated temporary save file.
 */
public class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void load_missingFile_emptyListReturned() throws IOException, DukeException {
        Storage storage = createStorage();

        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void saveLoad_todo_roundTripPreserved() throws IOException, DukeException {
        Storage storage = createStorage();
        storage.save(List.of(new Todo("read book")));

        List<Task> loaded = storage.load();

        assertEquals(1, loaded.size());
        assertInstanceOf(Todo.class, loaded.get(0));
        assertEquals("read book", loaded.get(0).getDescription());
    }

    @Test
    public void saveLoad_deadline_dateRoundTripPreserved() throws IOException, DukeException {
        Storage storage = createStorage();
        LocalDate date = LocalDate.of(2026, 8, 30);
        storage.save(List.of(new Deadline("submit report", date)));

        Deadline loaded = assertInstanceOf(Deadline.class, storage.load().get(0));

        assertEquals("submit report", loaded.getDescription());
        assertEquals(date, loaded.getBy());
    }

    @Test
    public void saveLoad_atEvent_roundTripPreserved() throws IOException, DukeException {
        Storage storage = createStorage();
        storage.save(List.of(new Event("meeting", "3pm")));

        Event loaded = assertInstanceOf(Event.class, storage.load().get(0));

        assertTrue(loaded.isAtFormat());
        assertEquals("meeting", loaded.getDescription());
        assertEquals("3pm", loaded.getAt());
    }

    @Test
    public void saveLoad_fromToEvent_roundTripPreserved() throws IOException, DukeException {
        Storage storage = createStorage();
        storage.save(List.of(new Event("workshop", "2pm", "4pm")));

        Event loaded = assertInstanceOf(Event.class, storage.load().get(0));

        assertFalse(loaded.isAtFormat());
        assertEquals("workshop", loaded.getDescription());
        assertEquals("2pm", loaded.getFrom());
        assertEquals("4pm", loaded.getTo());
    }

    @Test
    public void saveLoad_markedAndUnmarkedTasks_statusPreserved()
            throws IOException, DukeException {
        Storage storage = createStorage();
        Task marked = new Todo("marked");
        marked.markAsDone();
        Task unmarked = new Todo("unmarked");
        storage.save(List.of(marked, unmarked));

        List<Task> loaded = storage.load();

        assertTrue(loaded.get(0).isDone());
        assertFalse(loaded.get(1).isDone());
    }

    @Test
    public void saveLoad_multipleTasks_originalOrderPreserved() throws IOException, DukeException {
        Storage storage = createStorage();
        storage.save(List.of(
                new Todo("first"),
                new Deadline("second", LocalDate.of(2026, 8, 30)),
                new Event("third", "3pm")));

        List<Task> loaded = storage.load();

        assertEquals(List.of("first", "second", "third"), loaded.stream()
                .map(Task::getDescription)
                .toList());
    }

    @Test
    public void saveLoad_escapedControlCharacters_roundTripPreserved()
            throws IOException, DukeException {
        Storage storage = createStorage();
        Todo todo = new Todo("tab\tnewline\nbackslash\\carriage\rreturn");
        Event event = new Event("event\\description", "from\tline", "to\nline");
        storage.save(List.of(todo, event));

        List<Task> loaded = storage.load();
        Event loadedEvent = assertInstanceOf(Event.class, loaded.get(1));

        assertEquals(todo.getDescription(), loaded.get(0).getDescription());
        assertEquals(event.getDescription(), loadedEvent.getDescription());
        assertEquals(event.getFrom(), loadedEvent.getFrom());
        assertEquals(event.getTo(), loadedEvent.getTo());
    }

    @Test
    public void load_malformedRecord_exceptionThrown() throws IOException {
        writeSaveFile("not-a-task");

        assertThrows(DukeException.class, () -> createStorage().load());
    }

    @Test
    public void load_malformedCompletionStatus_exceptionThrown() throws IOException {
        writeSaveFile("T\t2\tread book");

        assertThrows(DukeException.class, () -> createStorage().load());
    }

    @Test
    public void load_malformedStoredDate_exceptionThrown() throws IOException {
        writeSaveFile("D\t0\tsubmit report\t2026-02-30");

        assertThrows(DukeException.class, () -> createStorage().load());
    }

    @Test
    public void load_malformedEscapedText_exceptionThrown() throws IOException {
        writeSaveFile("T\t0\tbad\\q");

        assertThrows(DukeException.class, () -> createStorage().load());
    }

    /**
     * Returns storage bound to this test's temporary directory.
     */
    private Storage createStorage() {
        return new Storage(temporaryDirectory.resolve("data").resolve("walter.txt"));
    }

    /**
     * Writes one raw record for malformed-file tests.
     */
    private void writeSaveFile(String record) throws IOException {
        Path saveFile = temporaryDirectory.resolve("data").resolve("walter.txt");
        Files.createDirectories(saveFile.getParent());
        Files.writeString(saveFile, record, StandardCharsets.UTF_8);
    }
}
