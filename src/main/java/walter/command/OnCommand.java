package walter.command;

import java.time.LocalDate;

import walter.storage.Storage;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Displays Deadlines occurring on one date without changing persisted state.
 */
public class OnCommand extends Command {
    private final LocalDate date;

    /**
     * Creates a date lookup command.
     */
    public OnCommand(LocalDate date) {
        this.date = date;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showDeadlinesOn(this.date, tasks.getDeadlinesOn(this.date));
    }
}
