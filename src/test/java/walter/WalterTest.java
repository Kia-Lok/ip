package walter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import walter.command.CommandCategory;
import walter.storage.Storage;

/**
 * Tests the presentation-neutral command API used by Walter's GUI.
 */
public class WalterTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void getResponse_statefulCommands_responsesAndStatePreserved() {
        Walter walter = createWalter();

        assertTrue(walter.getResponse("todo read book").contains("[T][ ] read book"));
        assertEquals("Here are the tasks in your list:\n1. [T][ ] read book",
                walter.getResponse("list"));
        assertTrue(walter.getResponse("mark 1").contains("[T][X] read book"));
    }

    @Test
    public void getResponse_invalidCommand_errorReturned() {
        Walter walter = createWalter();

        assertEquals("Unknown command.", walter.getResponse("archive book"));
    }

    @Test
    public void getResponse_commandCategory_matchesCommandType() {
        Walter walter = createWalter();

        walter.getResponse("todo read book");
        assertEquals(CommandCategory.ADD, walter.getLastCommandCategory());

        walter.getResponse("deadline submit report /by 2026-09-04");
        assertEquals(CommandCategory.ADD, walter.getLastCommandCategory());

        walter.getResponse("event project meeting /at 3pm");
        assertEquals(CommandCategory.ADD, walter.getLastCommandCategory());

        walter.getResponse("mark 1");
        assertEquals(CommandCategory.STATE_CHANGE, walter.getLastCommandCategory());

        walter.getResponse("unmark 1");
        assertEquals(CommandCategory.STATE_CHANGE, walter.getLastCommandCategory());

        walter.getResponse("delete 1");
        assertEquals(CommandCategory.DELETE, walter.getLastCommandCategory());

        walter.getResponse("list");
        assertEquals(CommandCategory.NORMAL, walter.getLastCommandCategory());
    }

    @Test
    public void getResponse_invalidCommand_setsErrorCategory() {
        Walter walter = createWalter();
        walter.getResponse("todo read book");

        assertEquals("Unknown command.", walter.getResponse("archive book"));
        assertEquals(CommandCategory.ERROR, walter.getLastCommandCategory());
    }

    @Test
    public void getResponse_byeCommand_farewellReturnedWithoutCliSeparator() {
        Walter walter = createWalter();

        assertEquals("Walter: Bye. Hope to see you again soon!", walter.getResponse("bye"));
    }

    @Test
    public void getResponse_persistedTask_loadedByNewInstance() {
        Storage storage = new Storage(temporaryDirectory.resolve("walter.txt"));
        Walter firstWalter = new Walter(storage);
        firstWalter.getResponse("deadline submit report /by 2026-09-04");

        Walter secondWalter = new Walter(storage);

        assertTrue(secondWalter.getResponse("list").contains("submit report (by: Sep 4 2026)"));
    }

    /**
     * Creates Walter with an isolated save file for one test.
     */
    private Walter createWalter() {
        return new Walter(new Storage(temporaryDirectory.resolve("walter.txt")));
    }
}
