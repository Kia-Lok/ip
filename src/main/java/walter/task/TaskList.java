package walter.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import walter.DukeException;

/**
 * Owns Walter's task collection and all operations that inspect or modify it.
 */
public class TaskList {
    private static final int MAX_TASKS = 100;

    private final ArrayList<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates a task list from tasks loaded by storage.
     */
    public TaskList(List<Task> tasks) throws DukeException {
        if (tasks.size() > MAX_TASKS) {
            throw new DukeException("Saved task list exceeds the supported capacity.");
        }
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds one task while enforcing Walter's existing capacity.
     */
    public void add(Task task) throws DukeException {
        if (tasks.size() >= MAX_TASKS) {
            throw new DukeException("Task list is full.");
        }
        tasks.add(task);
    }

    /**
     * Deletes and returns the task at a validated zero-based index.
     */
    public Task delete(int taskIndex) throws DukeException {
        validateIndex(taskIndex);
        return tasks.remove(taskIndex);
    }

    /**
     * Marks and returns the task at a validated zero-based index.
     */
    public Task markAsDone(int taskIndex) throws DukeException {
        Task task = get(taskIndex);
        task.markAsDone();
        return task;
    }

    /**
     * Unmarks and returns the task at a validated zero-based index.
     */
    public Task markAsNotDone(int taskIndex) throws DukeException {
        Task task = get(taskIndex);
        task.markAsNotDone();
        return task;
    }

    /**
     * Returns the task at a validated zero-based index.
     */
    public Task get(int taskIndex) throws DukeException {
        validateIndex(taskIndex);
        return tasks.get(taskIndex);
    }

    /**
     * Returns the number of stored tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns a read-only snapshot for display or persistence.
     */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /**
     * Returns matching Deadlines in their original task-list order.
     */
    public List<Deadline> getDeadlinesOn(LocalDate date) {
        List<Deadline> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task instanceof Deadline deadline && deadline.getBy().equals(date)) {
                matches.add(deadline);
            }
        }
        return matches;
    }

    /**
     * Rejects an index that does not identify an existing task.
     */
    private void validateIndex(int taskIndex) throws DukeException {
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new DukeException("Task number is out of range.");
        }
    }
}
