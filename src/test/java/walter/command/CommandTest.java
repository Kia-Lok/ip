package walter.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import walter.DukeException;
import walter.storage.Storage;
import walter.task.Deadline;
import walter.task.Task;
import walter.task.TaskList;
import walter.task.Todo;
import walter.ui.Ui;

/**
 * Tests command coordination where it adds coverage beyond TaskList and Storage tests.
 */
public class CommandTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void execute_addCommand_taskAddedAndPersisted() throws IOException, DukeException {
        TaskList tasks = new TaskList();
        Storage storage = createStorage();
        Task task = new Todo("read book");

        new AddCommand(task).execute(tasks, new Ui(), storage);

        assertSame(task, tasks.get(0));
        assertEquals("read book", storage.load().get(0).getDescription());
    }

    @Test
    public void execute_markAndUnmarkCommands_statusUpdatedAndPersisted()
            throws IOException, DukeException {
        Task task = new Todo("read book");
        TaskList tasks = new TaskList(List.of(task));
        Storage storage = createStorage();

        new MarkCommand(0).execute(tasks, new Ui(), storage);
        assertTrue(task.isDone());
        assertTrue(storage.load().get(0).isDone());

        new UnmarkCommand(0).execute(tasks, new Ui(), storage);
        assertFalse(task.isDone());
        assertFalse(storage.load().get(0).isDone());
    }

    @Test
    public void execute_deleteCommand_taskRemovedAndPersistenceUpdated()
            throws IOException, DukeException {
        Task first = new Todo("first");
        Task second = new Todo("second");
        TaskList tasks = new TaskList(List.of(first, second));
        Storage storage = createStorage();

        new DeleteCommand(0).execute(tasks, new Ui(), storage);

        assertEquals(1, tasks.size());
        assertSame(second, tasks.get(0));
        assertEquals("second", storage.load().get(0).getDescription());
    }

    @Test
    public void execute_mutatingCommandsWhenSaveFails_stateAndOutputRemainUnchanged()
            throws DukeException {
        Task first = new Todo("first");
        Task second = new Todo("second");
        TaskList tasks = new TaskList(List.of(first, second));
        FailingStorage storage = new FailingStorage(temporaryDirectory.resolve("walter.txt"));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        Ui ui = new Ui(new PrintStream(outputBytes, true, StandardCharsets.UTF_8));
        AddCommand addCommand = new AddCommand(new Todo("third"));

        assertThrows(DukeException.class, () -> addCommand.execute(tasks, ui, storage));
        assertThrows(DukeException.class, () -> new DeleteCommand(0).execute(tasks, ui, storage));
        assertThrows(DukeException.class, () -> new MarkCommand(0).execute(tasks, ui, storage));
        second.markAsDone();
        assertThrows(DukeException.class, () -> new UnmarkCommand(1).execute(tasks, ui, storage));

        assertEquals(List.of(first, second), tasks.getTasks());
        assertFalse(first.isDone());
        assertTrue(second.isDone());
        assertEquals("", outputBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void execute_onCommand_taskStateUnchangedAndStorageNotSaved() throws DukeException {
        LocalDate date = LocalDate.of(2026, 8, 30);
        Deadline deadline = new Deadline("submit report", date);
        TaskList tasks = new TaskList(List.of(deadline));
        RecordingStorage storage = new RecordingStorage(
                temporaryDirectory.resolve("walter.txt"));

        new OnCommand(date).execute(tasks, new Ui(), storage);

        assertSame(deadline, tasks.get(0));
        assertEquals(0, storage.getSaveCount());
    }

    @Test
    public void execute_findCommand_taskStateUnchangedAndStorageNotSaved() throws DukeException {
        Task matchingTask = new Todo("Read Book");
        matchingTask.markAsDone();
        Task otherTask = new Todo("buy milk");
        TaskList tasks = new TaskList(List.of(matchingTask, otherTask));
        List<Task> originalTasks = tasks.getTasks();
        RecordingStorage storage = new RecordingStorage(
                this.temporaryDirectory.resolve("walter.txt"));

        new FindCommand("book").execute(tasks, new Ui(), storage);

        assertEquals(originalTasks, tasks.getTasks());
        assertTrue(matchingTask.isDone());
        assertFalse(otherTask.isDone());
        assertEquals(0, storage.getSaveCount());
    }

    /**
     * Returns storage bound to this test's temporary directory.
     */
    private Storage createStorage() {
        return new Storage(temporaryDirectory.resolve("data").resolve("walter.txt"));
    }

    /**
     * Records save attempts so read-only command behavior can be verified without a mocking library.
     */
    private static class RecordingStorage extends Storage {
        private int saveCount;

        RecordingStorage(Path saveFile) {
            super(saveFile);
        }

        @Override
        public void save(List<Task> tasks) {
            saveCount++;
        }

        int getSaveCount() {
            return saveCount;
        }
    }

    /**
     * Simulates a persistence failure without relying on platform-specific file permissions.
     */
    private static class FailingStorage extends Storage {
        FailingStorage(Path saveFile) {
            super(saveFile);
        }

        @Override
        public void save(List<Task> tasks) throws DukeException {
            throw new DukeException("Walter could not save your tasks.");
        }
    }
}
