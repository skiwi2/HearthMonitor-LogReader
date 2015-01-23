package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
    private final Iterator<String> filteredIterator;

    private final List<String> linesInMemory = new ArrayList<>();
    private final List<String> peekedLines = new LinkedList<>();

    /**
     * Initializes an AbstractLogReader instance.
     *
     * @param entryParsers  The supplier of a set of entry parsers
     * @param readIterator  The iterator used to read lines from the log source
     * @param filterPredicate   The predicate to use to filter the lines read from the log source
     * @throws  java.lang.NullPointerException  If entryParsers.get() returns null or if readIterator or filterPredicate is null.
     */
    protected AbstractLogReader(final EntryParsers entryParsers, final Iterator<String> readIterator, final Predicate<String> filterPredicate) {
        Objects.requireNonNull(filterPredicate, "filterPredicate");
        Objects.requireNonNull(readIterator, "readIterator");
        this.entryParsers = Objects.requireNonNull(entryParsers.get(), "entryParsers.get()");
        this.filteredIterator = IteratorUtils.filteredIterator(readIterator, filterPredicate);
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

        String line = readLineFromLogAndSave();
        for (EntryParser entryParser : entryParsers) {
            if (!entryParser.isParsable(line)) {
                continue;
            }
            try {
                LogEntry result = entryParser.parse(line, new LineReader() {
                    @Override
                    public String readNextLine() {
                        return readLineFromLogAndSave();
                    }

                    @Override
                    public boolean hasNextLine() {
                        return hasNextEntry();
                    }

                    @Override
                    public boolean nextLineMatches(final Predicate<String> condition) {
                        if (!hasNextLine()) {
                            return false;
                        }
                        String line = readLineFromPeekedLinesOrLog();
                        Objects.requireNonNull(line, "line");
                        peekedLines.add(line);
                        return condition.test(line);
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
        return (!peekedLines.isEmpty() || filteredIterator.hasNext());
    }

    /**
     * Returns the next line from the lines that have been peeked, or if empty, from the log source.
     *
     * The returned line has to satisfy the filter predicate.
     *
     * @return  The next line from the lines that have been peeked, or if empty, from the log source.
     * @throws java.util.NoSuchElementException If no more lines are present.
     */
    private String readLineFromPeekedLinesOrLog() {
        if (!peekedLines.isEmpty()) {
            return peekedLines.remove(0);
        }
        return filteredIterator.next();
    }

    /**
     * Returns the next line from the log source and saves it.
     *
     * @return  The next line from the log source.
     * @throws java.util.NoSuchElementException If no more lines are present.
     * @throws java.lang.NullPointerException   If line is null.
     */
    private String readLineFromLogAndSave() {
        String line = readLineFromPeekedLinesOrLog();
        Objects.requireNonNull(line, "line");
        linesInMemory.add(line);
        return line;
    }
}
