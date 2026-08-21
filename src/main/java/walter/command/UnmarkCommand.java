package walter.command;

import walter.DukeException;
import walter.storage.Storage;
import walter.task.Task;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Marks one task as not done and persists the changed state.
 */
public class UnmarkCommand extends Command {
    private final int taskIndex;

    /**
     * Creates an unmark command for a zero-based task index.
     *
     * @param taskIndex Zero-based index of the task to mark as not done.
     */
    public UnmarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    /**
     * Marks the selected task as not done, displays it, and persists the changed status.
     *
     * @param tasks Task list containing the task to unmark.
     * @param ui User interface used to display the unmarked task.
     * @param storage Storage used to persist the changed task status.
     * @throws DukeException If the task index is invalid or the changed list cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        Task task = tasks.markAsNotDone(this.taskIndex);
        ui.showUnmarkedTask(task);
        storage.save(tasks.getTasks());
    }
}
