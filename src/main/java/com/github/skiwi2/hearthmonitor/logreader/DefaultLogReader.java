package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Used to read log entries from a log source via an iterator.
 *
 * It is encouraged to extend this class and pass the iterator via the subclass.
 *
 * @author Frank van Heeswijk
 */
public class DefaultLogReader implements LogReader {
    private final Set<? extends EntryParser> entryParsers;
    private final MatchingIterator<String> matchingIterator;

    private final List<String> linesInMemory = new ArrayList<>();

    /**
     * Constructs a new DefaultLogReader instance.
     *
     * @param entryParsers  The set of entry parsers
     * @param readIterator  The iterator used to read lines from the log source
     * @param filterPredicate   The predicate to use to filter the lines read from the log source
     * @throws  java.lang.NullPointerException  If entryParsers, readIterator or filterPredicate is null.
     */
    protected DefaultLogReader(final Set<? extends EntryParser> entryParsers, final Iterator<String> readIterator, final Predicate<? super String> filterPredicate) {
        Objects.requireNonNull(filterPredicate, "filterPredicate");
        Objects.requireNonNull(readIterator, "readIterator");
        this.entryParsers = Objects.requireNonNull(entryParsers, "entryParsers");
        Iterator<String> filteredIterator = IteratorUtils.filteredIterator(readIterator, filterPredicate);
        this.matchingIterator = MatchingIterator.fromIterator(filteredIterator);
    }

    /**
     * Constructs a new DefaultLogReader instance.
     *
     * The filter predicate can be used to filter the lines you want to traverse.
     *
     * @param entryParsers  The set of entry parsers
     * @param readIterator  The iterator used to read lines from the log source
     * @throws  java.lang.NullPointerException  If entryParsers or readIterator is null.
     */
    protected DefaultLogReader(final Set<? extends EntryParser> entryParsers, final Iterator<String> readIterator) {
        this(entryParsers, readIterator, string -> true);
    }

    @Override
    public LogEntry readNextEntry() throws NotReadableException {
        List<Exception> occurredExceptions = new ArrayList<>();

        String line = matchingIterator.next();
        linesInMemory.add(line);
        for (EntryParser entryParser : entryParsers) {
            if (!entryParser.isParsable(line)) {
                continue;
            }
            try {
                LogEntry result = entryParser.parse(line, new LineReader() {
                    @Override
                    public String readNextLine() {
                        String nextLine = matchingIterator.next();
                        linesInMemory.add(nextLine);
                        return nextLine;
                    }

                    @Override
                    public boolean hasNextLine() {
                        return matchingIterator.hasNext();
                    }

                    @Override
                    public boolean nextLineMatches(final Predicate<? super String> condition) {
                        return matchingIterator.nextMatches(condition);
                    }
                });
                linesInMemory.clear();
                return result;
            } catch (NotParsableException | NoSuchElementException ex) {
                occurredExceptions.add(ex);
                //try next entry parser
            }
        }
        List<String> notParsableLines = new ArrayList<>(linesInMemory);
        linesInMemory.clear();
        throw new NotReadableException(notParsableLines, occurredExceptions);
    }

    @Override
    public boolean hasNextEntry() {
        return matchingIterator.hasNext();
    }
}
