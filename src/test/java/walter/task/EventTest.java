package walter.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests both supported Event display formats.
 */
public class EventTest {
    @Test
    public void toString_atEvent_atFormattingIncluded() {
        Event event = new Event("meeting", "3pm");

        assertEquals("[E][ ] meeting (at: 3pm)", event.toString());
    }

    @Test
    public void toString_fromToEvent_rangeFormattingIncluded() {
        Event event = new Event("workshop", "2pm", "4pm");

        assertEquals("[E][ ] workshop (from: 2pm to: 4pm)", event.toString());
    }
}
