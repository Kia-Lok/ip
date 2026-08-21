package walter.command;

import walter.DukeException;
import walter.storage.Storage;
import walter.task.Task;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Deletes one task and persists the changed task list.
 */
public class DeleteCommand extends Command {
    private final int taskIndex;

    /**
     * Creates a delete command for a zero-based task index.
     */
    public DeleteCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        Task task = tasks.delete(taskIndex);
        ui.showDeletedTask(task, tasks.size());
        storage.save(tasks.getTasks());
    }
}
