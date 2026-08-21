import java.time.LocalDate;

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
