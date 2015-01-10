package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Can be extended to read lines from a log source.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractLineLogReader implements LogReader {
    private final Set<EntryReader> entryReaders = entryReaders();
    private final List<String> linesInMemory = new ArrayList<>();
    private final List<String> peekedLines = new LinkedList<>();

    @Override
    public LogEntry readEntry() throws NotReadableException, NoMoreInputException {
        String line = readLineFromLogAndSave();
        for (EntryReader entryReader : entryReaders) {
            if (!entryReader.isParsable(line)) {
                continue;
            }
            try {
                LogEntry result = entryReader.parse(line, new LineReader() {
                    @Override
                    public String readLine() throws NoMoreInputException {
                        return readLineFromLogAndSave();
                    }

                    @Override
                    public Optional<String> peekLine() {
                        return peekLineFromLog();
                    }

                    /**
                     * Returns the next line from the log file and saves it to the peeked lines list.
                     *
                     * @return  The next line from the log file.
                     * @throws  java.lang.NullPointerException  If line is null.
                     */
                    private Optional<String> peekLineFromLog() {
                        try {
                            String line = readLineFromLog();
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
            } catch (NotParsableException | NoMoreInputException ex) {
                //try next entry reader
            }
        }
        List<String> notReadableLines = new ArrayList<>(linesInMemory);
        linesInMemory.clear();
        throw new NotReadableException(notReadableLines);
    }

    /**
     * Returns the set of entry readers used to read the log files.
     *
     * @return  The set of entry readers used to read the log files.
     */
    protected abstract Set<EntryReader> entryReaders();

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
