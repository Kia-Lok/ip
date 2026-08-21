/**
 * Marks one task as not done and persists the changed state.
 */
public class UnmarkCommand extends Command {
    private final int taskIndex;

    /**
     * Creates an unmark command for a zero-based task index.
     */
    public UnmarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        Task task = tasks.markAsNotDone(this.taskIndex);
        ui.showUnmarkedTask(task);
        storage.save(tasks.getTasks());
    }
}
