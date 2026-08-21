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
     */
    public MarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        Task task = tasks.markAsDone(this.taskIndex);
        ui.showMarkedTask(task);
        storage.save(tasks.getTasks());
    }
}
