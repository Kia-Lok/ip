/**
 * Represents a task that occurs at a specified time or period.
 */
public class Event extends Task {
    private final String timeDetails;

    /**
     * Creates an event task with the given description and time text.
     *
     * @param description Description of the event task.
     * @param at Text describing when the event occurs.
     */
    public Event(String description, String at) {
        super(description);
        this.timeDetails = "at: " + at;
    }

    /**
     * Creates an event task that starts and ends at the given times.
     *
     * @param description Description of the event task.
     * @param from Text describing when the event starts.
     * @param to Text describing when the event ends.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.timeDetails = "from: " + from + " to: " + to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (" + this.timeDetails + ")";
    }
}
