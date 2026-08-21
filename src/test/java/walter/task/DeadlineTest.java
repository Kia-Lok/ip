package walter.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests Deadline-specific display formatting.
 */
public class DeadlineTest {
    @Test
    public void toString_deadline_friendlyDateFormattingIncluded() {
        Deadline deadline = new Deadline("submit report", LocalDate.of(2026, 8, 30));

        assertEquals("[D][ ] submit report (by: Aug 30 2026)", deadline.toString());
    }
}
