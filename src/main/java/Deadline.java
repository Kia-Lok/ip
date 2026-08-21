/**
 * Represents a task that needs to be completed by a specified time.
 */
public class Deadline extends Task {
    private final String by;

    /**
     * Creates a deadline task with the given description and deadline text.
     *
     * @param description Description of the deadline task.
     * @param by Text describing when the task is due.
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline text for persistence.
     *
     * @return Text describing when this task is due.
     */
    public String getBy() {
        return this.by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + this.by + ")";
    }
}
