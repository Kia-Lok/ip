package walter.command;

import walter.DukeException;
import walter.place.PlaceList;
import walter.storage.Storage;
import walter.task.TaskList;
import walter.ui.Ui;

/**
 * Displays saved places without changing Walter's task list or place storage.
 */
public class ListPlacesCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        PlaceList places = new PlaceList(storage.loadPlaces());
        ui.showPlaces(places.getPlaces());
    }
}
