package walter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    public void getWelcomeMessage_defaultMessage_breakingBadThemeGreetingReturned() {
        Walter walter = createWalter();

        assertEquals("Jesse, focus. We’ve got things to cook.\n"
                + "Tell me what needs to be done, and I’ll handle the list.\n"
                + "Start with `list`, `todo <task>`, or "
                + "`event <task> /from <start> /to <end>`.", walter.getWelcomeMessage());
    }

    @Test
    public void getResponse_statefulCommands_responsesAndStatePreserved() {
        Walter walter = createWalter();

        assertTrue(walter.getResponse("todo read book").contains("[T][ ] read book"));
        assertEquals("Here is the current operation:\n1. [T][ ] read book",
                walter.getResponse("list"));
        assertTrue(walter.getResponse("mark 1").contains("[T][X] read book"));
    }

    @Test
    public void getResponse_invalidCommand_errorReturned() {
        Walter walter = createWalter();

        assertEquals("Precision matters, Jesse. Unknown command.",
                walter.getResponse("archive book"));
    }

    @Test
    public void getResponse_knownErrorTypes_personalityPreservesSpecificDiagnostics() {
        Walter walter = createWalter();

        assertEquals("Precision matters, Jesse. Todo description cannot be empty.",
                walter.getResponse("todo"));
        assertEquals("Precision matters, Jesse. Task number is out of range.",
                walter.getResponse("mark 1"));
        assertEquals("Precision matters, Jesse. Deadline date must be in yyyy-MM-dd format.",
                walter.getResponse("deadline report /by tomorrow"));
        assertEquals("Precision matters, Jesse. Keyword is required for the find command.",
                walter.getResponse("find"));
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

        assertEquals("Precision matters, Jesse. Unknown command.",
                walter.getResponse("archive book"));
        assertEquals(CommandCategory.ERROR, walter.getLastCommandCategory());
    }

    @Test
    public void getResponse_byeCommand_farewellReturnedWithoutCliSeparator() {
        Walter walter = createWalter();

        assertEquals("All right. The operation is closed. Stay focused, Jesse.",
                walter.getResponse("bye"));
    }

    @Test
    public void getResponse_persistedTask_loadedByNewInstance() {
        Storage storage = new Storage(temporaryDirectory.resolve("walter.txt"));
        Walter firstWalter = new Walter(storage);
        firstWalter.getResponse("deadline submit report /by 2026-09-04");

        Walter secondWalter = new Walter(storage);

        assertTrue(secondWalter.getResponse("list").contains("submit report (by: Sep 4 2026)"));
    }

    @Test
    public void constructor_corruptedTaskFile_warningShownAndEmptyListUsed() throws IOException {
        Path saveFile = temporaryDirectory.resolve("walter.txt");
        Files.writeString(saveFile, "corrupted record", StandardCharsets.UTF_8);

        Walter walter = new Walter(new Storage(saveFile));

        assertTrue(walter.getWelcomeMessage().contains(
                "Malformed saved task record. Starting with an empty list."));
        assertEquals("The board is clean. Nothing needs doing.", walter.getResponse("list"));
    }

    @Test
    public void getResponse_placeLifecycle_addListDeleteAndPersist() {
        Storage storage = new Storage(temporaryDirectory.resolve("walter.txt"));
        Walter firstWalter = new Walter(storage);

        assertTrue(firstWalter.getResponse("place Alex's home /at 123 Clementi Ave 3")
                .contains("Alex's home — 123 Clementi Ave 3"));
        assertTrue(firstWalter.getResponse("place NUS Library /at 12 Computing Drive")
                .contains("The location list now contains 2 places."));
        assertEquals("Here are the recorded locations:\n"
                + "1. Alex's home — 123 Clementi Ave 3\n"
                + "2. NUS Library — 12 Computing Drive", firstWalter.getResponse("places"));

        assertTrue(firstWalter.getResponse("deleteplace 1")
                .contains("Alex's home — 123 Clementi Ave 3"));
        assertEquals("Here are the recorded locations:\n"
                + "1. NUS Library — 12 Computing Drive", firstWalter.getResponse("places"));

        Walter restartedWalter = new Walter(storage);
        assertEquals("Here are the recorded locations:\n"
                + "1. NUS Library — 12 Computing Drive", restartedWalter.getResponse("places"));
    }

    @Test
    public void getResponse_invalidPlaceCommands_errorsReturnedWithoutChangingPlaces() {
        Walter walter = createWalter();

        assertEquals("Precision matters, Jesse. Place name cannot be empty.",
                walter.getResponse("place"));
        assertEquals("Precision matters, Jesse. Place requires exactly one /at.",
                walter.getResponse("place Alex's home"));
        assertEquals("Precision matters, Jesse. Place name cannot be empty.",
                walter.getResponse("place /at 123 Clementi Ave 3"));
        assertEquals("Precision matters, Jesse. Place address cannot be empty.",
                walter.getResponse("place Alex's home /at"));
        assertEquals("Precision matters, Jesse. Place number must be an integer.",
                walter.getResponse("deleteplace abc"));
        assertEquals("Precision matters, Jesse. Place number is out of range.",
                walter.getResponse("deleteplace 999"));
        assertEquals("The location list is empty.",
                walter.getResponse("places"));
    }

    /**
     * Creates Walter with an isolated save file for one test.
     */
    private Walter createWalter() {
        return new Walter(new Storage(temporaryDirectory.resolve("walter.txt")));
    }
}
