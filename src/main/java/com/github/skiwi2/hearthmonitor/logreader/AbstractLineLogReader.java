package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Can be extended to read lines from a log source.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractLineLogReader implements LogReader {
    private final Set<EntryReader> entryReaders = entryReaders();
    private final List<String> linesInMemory = new ArrayList<>();

    @Override
    public LogEntry readEntry() throws NotReadableException, NoMoreInputException {
        String line = readLineFromLogAndSave();
        for (EntryReader entryReader : entryReaders) {
            if (!entryReader.isParsable(line)) {
                continue;
            }
            try {
                LogEntry result = entryReader.parse(line, this::readLineFromLogAndSave);
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
     * Returns the next line from the log file and saves it.
     *
     * @return  The next line from the log file.
     * @throws NoMoreInputException If no more lines are present.
     * @throws java.lang.NullPointerException   If line is null.
     */
    private String readLineFromLogAndSave() throws NoMoreInputException {
        String line = readLineFromLog();
        Objects.requireNonNull(line, "line");
        linesInMemory.add(line);
        return line;
    }
}
