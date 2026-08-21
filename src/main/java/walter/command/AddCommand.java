package walter.command;

import walter.DukeException;
import walter.storage.Storage;
import walter.task.Task;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Adds one already-parsed task and persists the changed task list.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates an add command for a Todo, Deadline, or Event.
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        tasks.add(this.task);
        ui.showAddedTask(this.task, tasks.size());
        storage.save(tasks.getTasks());
    }
}
