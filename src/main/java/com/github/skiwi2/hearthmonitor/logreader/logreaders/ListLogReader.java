package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.DefaultLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Used to read log entries from a list.
 *
 * @author Frank van Heeswijk
 */
public class ListLogReader extends DefaultLogReader {
    /**
     * Constructs a new ListLogReader instance.
     *
     * This method saves a snapshot of the list at this time, and uses that to iterate over.
     *
     * @param inputList The input list to read from
     * @param entryParsers  The set of entry parsers
     * @throws  java.lang.NullPointerException  If inputList or entryParsers is null.
     */
    public ListLogReader(final List<String> inputList, final Set<? extends EntryParser> entryParsers) {
        super(entryParsers, new ArrayList<>(inputList).iterator());
    }

    /**
     * Constructs a new ListLogReader instance.
     *
     * This method saves a snapshot of the list at this time, and uses that to iterate over.
     * The filter predicate can be used to filter the lines you want to traverse.
     *
     * @param inputList The input list to read from
     * @param entryParsers  The set of entry parsers
     * @param filterPredicate   The predicate to filter the lines with
     * @throws  java.lang.NullPointerException  If inputList, filterPredicate or entryParsers is null.
     */
    public ListLogReader(final List<String> inputList, final Set<? extends EntryParser> entryParsers, final Predicate<? super String> filterPredicate) {
        super(entryParsers, new ArrayList<>(inputList).iterator(), filterPredicate);
    }
}
