package walter.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import walter.DukeException;
import walter.command.AddCommand;
import walter.command.DeleteCommand;
import walter.command.ExitCommand;
import walter.command.ListCommand;
import walter.command.MarkCommand;
import walter.command.OnCommand;
import walter.command.UnmarkCommand;

/**
 * Tests command recognition and validation performed by {@link Parser}.
 */
public class ParserTest {
    @Test
    public void parse_validTodo_addCommandReturned() throws DukeException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
    }

    @Test
    public void parse_emptyTodo_exceptionThrown() {
        assertThrows(DukeException.class, () -> Parser.parse("todo"));
    }

    @Test
    public void parse_validDeadline_addCommandReturned() throws DukeException {
        assertInstanceOf(
                AddCommand.class,
                Parser.parse("deadline submit report /by 2026-08-30"));
    }

    @Test
    public void parse_validLeapDate_addCommandReturned() throws DukeException {
        assertInstanceOf(
                AddCommand.class,
                Parser.parse("deadline submit report /by 2028-02-29"));
    }

    @Test
    public void parse_invalidDeadlineDates_exceptionThrown() {
        assertAll(
                () -> assertThrows(
                        DukeException.class,
                        () -> Parser.parse("deadline report /by 2027-02-29")),
                () -> assertThrows(
                        DukeException.class,
                        () -> Parser.parse("deadline report /by 2026-02-30")));
    }

    @Test
    public void parse_validAtEvent_addCommandReturned() throws DukeException {
        assertInstanceOf(AddCommand.class, Parser.parse("event meeting /at 3pm"));
    }

    @Test
    public void parse_validFromToEvent_addCommandReturned() throws DukeException {
        assertInstanceOf(
                AddCommand.class,
                Parser.parse("event meeting /from 2pm /to 4pm"));
    }

    @Test
    public void parse_malformedEvents_exceptionThrown() {
        assertAll(
                () -> assertThrows(DukeException.class, () -> Parser.parse("event meeting")),
                () -> assertThrows(
                        DukeException.class,
                        () -> Parser.parse("event meeting /from 2pm")),
                () -> assertThrows(
                        DukeException.class,
                        () -> Parser.parse("event meeting /to 4pm")),
                () -> assertThrows(
                        DukeException.class,
                        () -> Parser.parse("event meeting /at")));
    }

    @Test
    public void parse_listCommand_listCommandReturned() throws DukeException {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parse_byeCommand_exitCommandReturned() throws DukeException {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parse_markAndDoneCommands_markCommandReturned() {
        assertAll(
                () -> assertInstanceOf(MarkCommand.class, Parser.parse("mark 1")),
                () -> assertInstanceOf(MarkCommand.class, Parser.parse("done 2")));
    }

    @Test
    public void parse_unmarkCommand_unmarkCommandReturned() throws DukeException {
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 1"));
    }

    @Test
    public void parse_deleteCommand_deleteCommandReturned() throws DukeException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
    }

    @Test
    public void parse_validOnDate_onCommandReturned() throws DukeException {
        assertInstanceOf(OnCommand.class, Parser.parse("on 2026-08-30"));
    }

    @Test
    public void parse_invalidOnDate_exceptionThrown() {
        assertThrows(DukeException.class, () -> Parser.parse("on 2026-02-30"));
    }

    @Test
    public void parse_missingTaskNumbers_exceptionThrown() {
        assertAll(
                () -> assertThrows(DukeException.class, () -> Parser.parse("mark")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("done")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("unmark")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("delete")));
    }

    @Test
    public void parse_nonIntegerTaskNumbers_exceptionThrown() {
        assertAll(
                () -> assertThrows(DukeException.class, () -> Parser.parse("mark first")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("unmark 1.5")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("delete two")));
    }

    @Test
    public void parse_blankInput_exceptionThrown() {
        assertAll(
                () -> assertThrows(DukeException.class, () -> Parser.parse("")),
                () -> assertThrows(DukeException.class, () -> Parser.parse("   ")));
    }

    @Test
    public void parse_unknownCommand_exceptionThrown() {
        assertThrows(DukeException.class, () -> Parser.parse("find book"));
    }
}
