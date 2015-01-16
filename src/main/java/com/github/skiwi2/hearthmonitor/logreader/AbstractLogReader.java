package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Can be extended to read lines from a log source.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractLogReader implements LogReader {
    private final Set<EntryParser> entryParsers;

    private final List<String> linesInMemory = new ArrayList<>();
    private final List<String> peekedLines = new LinkedList<>();

    /**
     * Initializes an AbstractLogReader instance.
     *
     * @param entryParsers  The supplier of a set of entry parsers
     * @throws  java.lang.NullPointerException  If entryParsers.get() is null.
     */
    protected AbstractLogReader(final EntryParsers entryParsers) {
        this.entryParsers = Objects.requireNonNull(entryParsers.get(), "entryParsers.get()");
    }

    @Override
    public LogEntry readEntry() throws NotReadableException, NoMoreInputException {
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
                        try {
                            return readLineFromLogAndSave();
                        } catch (NoMoreInputException ex) {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public boolean hasNextLine() {
                        Optional<String> peekLine = peekLineFromLog();
                        return peekLine.isPresent();
                    }

                    @Override
                    public boolean nextLineMatches(final Predicate<String> condition) {
                        Objects.requireNonNull(condition, "condition");
                        Optional<String> peekLine = peekLineFromLog();
                        return (peekLine.isPresent() && condition.test(peekLine.get()));
                    }

                    /**
                     * Returns the next line from the log file and saves it to the peeked lines list.
                     *
                     * @return The next line from the log file.
                     * @throws java.lang.NullPointerException If line is null.
                     */
                    private Optional<String> peekLineFromLog() {
                        try {
                            String line = readLineFromLogOrPeekedLines();
                            Objects.requireNonNull(line, "line");
                            peekedLines.add(line);
                            return Optional.of(line);
                        } catch (NoMoreInputException ex) {
                            return Optional.empty();
                        }
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

    /**
     * Returns the next line from the log file.
     *
     * @return  The next line from the log file.
     * @throws NoMoreInputException If no more lines are present.
     */
    protected abstract String readLineFromLog() throws NoMoreInputException;

    /**
     * Returns the next line from the log file, or from the lines that have been peeked.
     *
     * @return  The next lien from the log file, or from the lines that have been peeked.
     * @throws NoMoreInputException If no more lines are present.
     */
    private String readLineFromLogOrPeekedLines() throws NoMoreInputException {
        if (!peekedLines.isEmpty()) {
            return peekedLines.remove(0);
        }
        return readLineFromLog();
    }

    /**
     * Returns the next line from the log file and saves it.
     *
     * @return  The next line from the log file.
     * @throws NoMoreInputException If no more lines are present.
     * @throws java.lang.NullPointerException   If line is null.
     */
    private String readLineFromLogAndSave() throws NoMoreInputException {
        String line = readLineFromLogOrPeekedLines();
        Objects.requireNonNull(line, "line");
        linesInMemory.add(line);
        return line;
    }
}
