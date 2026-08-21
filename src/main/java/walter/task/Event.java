package walter.task;

/**
 * Represents a task that occurs at one specified time or across a start/end period.
 */
public class Event extends Task {
    private final String at;
    private final String from;
    private final String to;

    /**
     * Creates an event task with the given description and time text.
     *
     * @param description Description of the event task.
     * @param at Text describing when the event occurs.
     */
    public Event(String description, String at) {
        super(description);
        this.at = at;
        this.from = null;
        this.to = null;
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
        this.at = null;
        this.from = from;
        this.to = to;
    }

    /**
     * Reports whether this event uses the {@code /at} representation.
     *
     * @return {@code true} for an at-time event, or {@code false} for a time range.
     */
    public boolean isAtFormat() {
        return this.at != null;
    }

    /**
     * Returns the at-time text for persistence.
     *
     * @return At-time text, or {@code null} for a time-range event.
     */
    public String getAt() {
        return this.at;
    }

    /**
     * Returns the start text for persistence.
     *
     * @return Start text, or {@code null} for an at-time event.
     */
    public String getFrom() {
        return this.from;
    }

    /**
     * Returns the end text for persistence.
     *
     * @return End text, or {@code null} for an at-time event.
     */
    public String getTo() {
        return this.to;
    }

    /**
     * Returns the Event type marker, task status, description, and configured time details.
     *
     * @return Display form using either {@code at:} or {@code from: ... to: ...}.
     */
    @Override
    public String toString() {
        String timeDetails = isAtFormat()
                ? "at: " + this.at
                : "from: " + this.from + " to: " + this.to;
        return "[E]" + super.toString() + " (" + timeDetails + ")";
    }
}
