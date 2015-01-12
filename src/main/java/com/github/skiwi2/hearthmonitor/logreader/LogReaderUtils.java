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
