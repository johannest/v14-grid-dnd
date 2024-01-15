package org.vaadin.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@SuppressWarnings("serial")
@DomEvent("grid-drop")
public class CustomGridDropEvent<T> extends ComponentEvent<Grid<T>> {

    private final T dropTargetItem;
    private final GridDropLocation dropLocation;
    private final Map<String, String> data;

    public CustomGridDropEvent(Grid<T> source, boolean fromClient,
                               @EventData("event.detail.dropTargetItem") JsonObject item,
                               @EventData("event.detail.dropLocation") String dropLocation,
                               @EventData("event.detail.dragData") JsonArray dragData) {
        super(source, fromClient);

        data = new HashMap<>();
        IntStream.range(0, dragData.length()).forEach(i -> {
            JsonObject jsonData = dragData.getObject(i);
            data.put(jsonData.getString("type"), jsonData.getString("data"));
        });

        if (item != null) {
            this.dropTargetItem = source.getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
        } else {
            this.dropTargetItem = null;
        }

        // CUSTOMIZATION STARTS
        Optional<GridDropLocation> first = Arrays.asList(GridDropLocation.values()).stream()
                .filter(dl -> dl.getClientName().equals(dropLocation))
                .findFirst();

        if (first.isEmpty()) {
            System.out.println("Did not find correct drop location");
            System.out.println("event.detail.dropTargetItem: "+item);
            System.out.println("event.detail.dropLocation: " +dropLocation);
            System.out.println("event.detail.dragData: "+getDetails(dragData));
            // fallback drop location
            this.dropLocation = GridDropLocation.ON_TOP;
        } else {
            this.dropLocation = first.get();
        }
        // CUSTOMIZATION ENDS
    }

    private String getDetails(JsonArray dragData) {
        if (dragData == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<dragData.length(); i++) {
                JsonValue jsonValue = dragData.get(i);
                if (jsonValue instanceof JsonObject) {
                    sb.append(((JsonObject)jsonValue).get("type").asString()).append(" ");
                    sb.append(((JsonObject)jsonValue).get("data").asString()).append(" ");
                } else {
                    sb.append(jsonValue.asString()).append(",");
                }
            }
           return sb.toString();
        }
    }

    /**
     * Get the row the drop happened on.
     * <p>
     * If the drop was not on top of a row (see {@link #getDropLocation()}) or
     * {@link GridDropMode#ON_GRID} is used, then returns an empty optional.
     *
     * @return The item of the row the drop happened on, or an empty optional if
     *         drop was not on a row
     */
    public Optional<T> getDropTargetItem() {
        return Optional.ofNullable(dropTargetItem);
    }

    /**
     * Get the location of the drop within the row.
     * <p>
     * <em>NOTE: the location will be {@link GridDropLocation#EMPTY} if:
     * <ul>
     * <li>dropped on an empty grid</li>
     * <li>dropping on rows was not possible because of
     * {@link GridDropMode#ON_GRID } was used</li>
     * <li>{@link GridDropMode#ON_TOP} is used and the drop happened on empty
     * space after last row or on top of the header / footer</li>
     * </ul>
     * </em>
     *
     * @return location of the drop in relative to the
     *         {@link #getDropTargetItem()} or {@link GridDropLocation#EMPTY} if
     *         no target row present
     * @see Grid#setDropMode(GridDropMode)
     */
    public GridDropLocation getDropLocation() {
        return dropLocation;
    }

    /**
     * Get data from the {@code DataTransfer} object.
     *
     * @param type
     *            Data format, e.g. {@code text/plain} or {@code text/uri-list}.
     * @return Optional data for the given format if exists in the {@code
     * DataTransfer}, otherwise {@code Optional.empty()}.
     */
    public Optional<String> getDataTransferData(String type) {
        return Optional.ofNullable(data.get(type));
    }

    /**
     * Get data of any of the types {@code "text"}, {@code "Text"} or {@code
     * "text/plain"}.
     * <p>
     * IE 11 transfers data dropped from the desktop as {@code "Text"} while
     * most other browsers transfer textual data as {@code "text/plain"}.
     *
     * @return First existing data of types in order {@code "text"}, {@code
     * "Text"} or {@code "text/plain"}, or {@code null} if none of them exist.
     */
    public String getDataTransferText() {
        // Read data type "text"
        String text = data.get("text");

        // IE stores data dragged from the desktop as "Text"
        if (text == null) {
            text = data.get("Text");
        }

        // Browsers may store the key as "text/plain"
        if (text == null) {
            text = data.get("text/plain");
        }

        return text;
    }

    /**
     * Get all of the transfer data from the {@code DataTransfer} object. The
     * data can be iterated to find the most relevant data as it preserves the
     * order in which the data was set to the drag source element.
     *
     * @return Map of type/data pairs, containing all the data from the {@code
     * DataTransfer} object.
     */
    public Map<String, String> getDataTransferData() {
        return Collections.unmodifiableMap(data);
    }

}
