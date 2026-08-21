/**
 * Represents a parsed Walter command that can be executed by the application loop.
 */
public abstract class Command {
    /**
     * Executes this command using Walter's existing application components.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException;

    /**
     * Reports whether this command should terminate the application loop.
     */
    public boolean isExit() {
        return false;
    }
}
