package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class to create a LogReader from other sources.
 *
 * @author Frank van Heeswijk
 */
public final class LogReaderUtils {
    private LogReaderUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a LogReader that can read log entries from a LineReader and a read condition.
     *
     * The LogReader will attempt to read from the LineReader as long as the read condition is met using the supplied entry readers.
     *
     * @param lineReader    The line reader
     * @param readCondition The read condition
     * @param entryReaders  The entry readers
     * @return  A new LogReader that can read log entries from the LineReader and the read condition.
     */
    public static LogReader fromLineReader(final LineReader lineReader, final Predicate<String> readCondition, final Set<EntryReader> entryReaders) {
        Objects.requireNonNull(lineReader, "lineReader");
        Objects.requireNonNull(readCondition, "readCondition");
        Objects.requireNonNull(entryReaders, "entryReaders");
        return new AbstractLineLogReader() {
            @Override
            protected Set<EntryReader> entryReaders() {
                return entryReaders;
            }

            @Override
            protected String readLineFromLog() throws NoMoreInputException {
                Optional<String> peekLine = lineReader.peekLine();
                if (peekLine.isPresent() && readCondition.test(peekLine.get())) {
                    return lineReader.readLine();
                }
                throw new NoMoreInputException();
            }
        };
    }
}
