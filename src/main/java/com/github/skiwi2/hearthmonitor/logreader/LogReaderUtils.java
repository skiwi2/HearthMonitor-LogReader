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
     * Creates a LogReader that can read log entries from an input string, a LineReader for the extra lines and a read condition.
     *
     * The LogReader will attempt to read extra lines from the LineReader as long as the read condition is met using the supplied entry readers.
     *
     * Note: The input will always be included and not checked against the extra read condition.
     *
     * @param input The input line
     * @param extraLineReader    The extra line reader
     * @param extraReadCondition The extra read condition
     * @param entryReaders  The entry readers
     * @return  A new LogReader that can read log entries from the input string, the LineReader for th extra lines and the read condition.
     */
    public static LogReader fromInputAndExtraLineReader(final String input, final LineReader extraLineReader, final Predicate<String> extraReadCondition, final Set<EntryReader> entryReaders) {
        Objects.requireNonNull(extraLineReader, "extraLineReader");
        Objects.requireNonNull(extraReadCondition, "extraReadCondition");
        Objects.requireNonNull(entryReaders, "entryReaders");
        return new AbstractLineLogReader() {
            private boolean inputRead = false;

            @Override
            protected Set<EntryReader> entryReaders() {
                return entryReaders;
            }

            @Override
            protected String readLineFromLog() throws NoMoreInputException {
                if (!inputRead) {
                    inputRead = true;
                    return input;
                }

                Optional<String> peekLine = extraLineReader.peekLine();
                if (peekLine.isPresent() && extraReadCondition.test(peekLine.get())) {
                    return extraLineReader.readLine();
                }
                throw new NoMoreInputException();
            }
        };
    }
}
