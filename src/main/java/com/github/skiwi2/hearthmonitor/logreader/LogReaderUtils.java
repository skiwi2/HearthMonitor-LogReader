package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public static LogReader fromInputAndExtraLineReader(final String input, final LineReader extraLineReader, final Predicate<? super String> extraReadCondition, final EntryParsers entryParsers) {
        Objects.requireNonNull(extraLineReader, "extraLineReader");
        Objects.requireNonNull(extraReadCondition, "extraReadCondition");
        Objects.requireNonNull(entryParsers, "entryParsers");
        return new AbstractLogReader(entryParsers, createReadIteratorForFromInputAndExtraLineReader(input, extraLineReader, extraReadCondition)) { };
    }

    /**
     * Returns an iterator for the given input and extra line reader.
     *
     * @param input The given input
     * @param extraLineReader   The given extra line reader
     * @param extraReadCondition    The given extra read condition
     * @return  The iterator for the given input and extra line reader.
     */
    private static Iterator<String> createReadIteratorForFromInputAndExtraLineReader(final String input, final LineReader extraLineReader, final Predicate<? super String> extraReadCondition) {
        LineReader conditionalLineReader = LineReader.readWhile(extraLineReader, extraReadCondition);
        Iterator<String> lineReaderIterator = new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return conditionalLineReader.hasNextLine();
            }

            @Override
            public String next() {
                return conditionalLineReader.readNextLine();
            }
        };
        return Stream.concat(
            Stream.of(input),
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(lineReaderIterator, Spliterator.NONNULL), false)
        ).iterator();
    }
}
