package walter.command;

import walter.DukeException;
import walter.storage.Storage;
import walter.task.Task;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Marks one task as done and persists the changed state.
 */
public class MarkCommand extends Command {
    private final int taskIndex;

    /**
     * Creates a mark command for a zero-based task index.
     *
     * @param taskIndex Zero-based index of the task to mark as done.
     */
    public MarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    /**
     * Marks the selected task as done, displays it, and persists the changed status.
     *
     * @param tasks Task list containing the task to mark.
     * @param ui User interface used to display the marked task.
     * @param storage Storage used to persist the changed task status.
     * @throws DukeException If the task index is invalid or the changed list cannot be saved.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        Task task = tasks.markAsDone(taskIndex);
        ui.showMarkedTask(task);
        storage.save(tasks.getTasks());
    }
}
