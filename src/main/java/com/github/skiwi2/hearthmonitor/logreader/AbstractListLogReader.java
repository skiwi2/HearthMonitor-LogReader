package com.github.skiwi2.hearthmonitor.logreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Can be extended to read log entries from a list.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractListLogReader extends AbstractLineLogReader {
    private final Iterator<String> iterator;

    /**
     * Initializes an AbstractListLogReader instance.
     *
     * This method saves a snapshot of the list at this time, and uses that to iterate over.
     *
     * @param inputList The input list to read from
     * @throws  java.lang.NullPointerException  If inputList is null.
     */
    protected AbstractListLogReader(final List<String> inputList) {
        Objects.requireNonNull(inputList, "inputList");
        this.iterator = new ArrayList<>(inputList).iterator();
    }

    @Override
    protected String readLineFromLog() throws NoMoreInputException {
        if (!iterator.hasNext()) {
            throw new NoMoreInputException();
        }
        return iterator.next();
    }
}
