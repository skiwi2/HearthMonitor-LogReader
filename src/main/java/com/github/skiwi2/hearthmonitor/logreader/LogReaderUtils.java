package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Objects;
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
     * @param entryParsers  The supplier of a set of entry parsers
     * @return  A new LogReader that can read log entries from the input string, the LineReader for the extra lines and the read condition.
     */
    public static LogReader fromInputAndExtraLineReader(final String input, final LineReader extraLineReader, final Predicate<String> extraReadCondition, final EntryParsers entryParsers) {
        Objects.requireNonNull(extraLineReader, "extraLineReader");
        Objects.requireNonNull(extraReadCondition, "extraReadCondition");
        Objects.requireNonNull(entryParsers, "entryParsers");
        return new AbstractLogReader(entryParsers) {
            private boolean inputRead = false;

            @Override
            protected String readLineFromLog() throws NoMoreInputException {
                if (!inputRead) {
                    inputRead = true;
                    return input;
                }
                if (extraLineReader.nextLineMatches(extraReadCondition)) {
                    return extraLineReader.readNextLine();
                }
                throw new NoMoreInputException();
            }
        };
    }
}
