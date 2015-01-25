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
 * Can be extended to read lines from a log source.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractLogReader implements LogReader {
    private final Set<EntryParser> entryParsers;
    private final MatchingIterator<String> matchingIterator;

    private final List<String> linesInMemory = new ArrayList<>();

    /**
     * Initializes an AbstractLogReader instance.
     *
     * @param entryParsers  The supplier of a set of entry parsers
     * @param readIterator  The iterator used to read lines from the log source
     * @param filterPredicate   The predicate to use to filter the lines read from the log source
     * @throws  java.lang.NullPointerException  If entryParsers.get() returns null or if readIterator or filterPredicate is null.
     */
    protected AbstractLogReader(final EntryParsers entryParsers, final Iterator<String> readIterator, final Predicate<? super String> filterPredicate) {
        Objects.requireNonNull(filterPredicate, "filterPredicate");
        Objects.requireNonNull(readIterator, "readIterator");
        this.entryParsers = Objects.requireNonNull(entryParsers.get(), "entryParsers.get()");
        Iterator<String> filteredIterator = IteratorUtils.filteredIterator(readIterator, filterPredicate);
        this.matchingIterator = MatchingIterator.fromIterator(filteredIterator);
    }

    /**
     * Initializes an AbstractLogReader instance without a filter predicate defined.
     *
     * @param entryParsers  The supplier of a set of entry parsers
     * @param readIterator  The iterator used to read lines from the log source
     * @throws  java.lang.NullPointerException  If entryParsers.get() returns null or if readIterator is null.
     */
    protected AbstractLogReader(final EntryParsers entryParsers, final Iterator<String> readIterator) {
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
