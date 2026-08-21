package walter.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import walter.DukeException;

/**
 * Owns Walter's ordered task collection and enforces its capacity and index rules.
 */
public class TaskList {
    private static final int MAX_TASKS = 100;

    private final ArrayList<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list from tasks loaded by storage.
     *
     * @param tasks Initial tasks in display order.
     * @throws DukeException If the supplied list exceeds Walter's supported capacity.
     */
    public TaskList(List<Task> tasks) throws DukeException {
        if (tasks.size() > MAX_TASKS) {
            throw new DukeException("Saved task list exceeds the supported capacity.");
        }
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds one task while enforcing Walter's existing capacity.
     *
     * @param task Task to append to the list.
     * @throws DukeException If the task list has reached its supported capacity.
     */
    public void add(Task task) throws DukeException {
        if (this.tasks.size() >= MAX_TASKS) {
            throw new DukeException("Task list is full.");
        }
        this.tasks.add(task);
    }

    /**
     * Deletes and returns the task at a validated zero-based index.
     *
     * @param taskIndex Zero-based index of the task to delete.
     * @return Deleted task.
     * @throws DukeException If the index does not identify an existing task.
     */
    public Task delete(int taskIndex) throws DukeException {
        validateIndex(taskIndex);
        return this.tasks.remove(taskIndex);
    }

    /**
     * Marks and returns the task at a validated zero-based index.
     *
     * @param taskIndex Zero-based index of the task to mark as done.
     * @return Task whose status was changed.
     * @throws DukeException If the index does not identify an existing task.
     */
    public Task markAsDone(int taskIndex) throws DukeException {
        Task task = get(taskIndex);
        task.markAsDone();
        return task;
    }

    /**
     * Unmarks and returns the task at a validated zero-based index.
     *
     * @param taskIndex Zero-based index of the task to mark as not done.
     * @return Task whose status was changed.
     * @throws DukeException If the index does not identify an existing task.
     */
    public Task markAsNotDone(int taskIndex) throws DukeException {
        Task task = get(taskIndex);
        task.markAsNotDone();
        return task;
    }

    /**
     * Returns the task at a validated zero-based index.
     *
     * @param taskIndex Zero-based index of the task to retrieve.
     * @return Task at the requested index.
     * @throws DukeException If the index does not identify an existing task.
     */
    public Task get(int taskIndex) throws DukeException {
        validateIndex(taskIndex);
        return this.tasks.get(taskIndex);
    }

    /**
     * Returns the number of stored tasks.
     *
     * @return Current task count.
     */
    public int size() {
        return this.tasks.size();
    }

    /**
     * Returns a read-only snapshot for display or persistence.
     *
     * @return Unmodifiable snapshot preserving the current task order.
     */
    public List<Task> getTasks() {
        return List.copyOf(this.tasks);
    }

    /**
     * Returns matching Deadlines in their original task-list order.
     *
     * @param date Date for which deadlines are requested.
     * @return Deadlines due on the given date, preserving task-list order.
     */
    public List<Deadline> getDeadlinesOn(LocalDate date) {
        List<Deadline> matches = new ArrayList<>();
        for (Task task : this.tasks) {
            if (task instanceof Deadline deadline && deadline.getBy().equals(date)) {
                matches.add(deadline);
            }
        }
        return matches;
    }

    /**
     * Rejects an index that does not identify an existing task.
     *
     * @param taskIndex Zero-based index to validate.
     * @throws DukeException If the index is negative or beyond the end of the task list.
     */
    private void validateIndex(int taskIndex) throws DukeException {
        if (taskIndex < 0 || taskIndex >= this.tasks.size()) {
            throw new DukeException("Task number is out of range.");
        }
    }
}
