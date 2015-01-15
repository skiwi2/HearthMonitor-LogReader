package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.AbstractLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Used to read log entries from a list.
 *
 * @author Frank van Heeswijk
 */
public class ListLogReader extends AbstractLogReader {
    private final Iterator<String> iterator;

    /**
     * Constructs a new ListLogReader instance.
     *
     * This method saves a snapshot of the list at this time, and uses that to iterate over.
     *
     * @param inputList The input list to read from
     * @param entryParsers  The supplier of a set of entry parsers
     * @throws  java.lang.NullPointerException  If inputList or entryParsers.get() is null.
     */
    public ListLogReader(final List<String> inputList, final EntryParsers entryParsers) {
        super(entryParsers);
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
