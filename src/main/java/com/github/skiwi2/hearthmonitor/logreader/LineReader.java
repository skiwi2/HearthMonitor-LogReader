package com.github.skiwi2.hearthmonitor.logreader;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Used to read lines from an input source.
 *
 * @author Frank van Heeswijk
 */
public interface LineReader {
    /**
     * Reads the next line.
     *
     * @return  The next line.
     * @throws java.util.NoSuchElementException If there are no lines left anymore
     */
    String readNextLine() throws NoSuchElementException;

    /**
     * Returns whether there is a next line to read.
     *
     * @return  Whether there is a next line to read.
     */
    boolean hasNextLine();

    /**
     * Returns whether the next line matches the given condition.
     *
     * @param condition The condition that the next line should match
     * @return  Whether the next line matches the given condition.
     */
    boolean nextLineMatches(final Predicate<String> condition);

    /**
     * Returns a LineReader that reads from another LineReader while the read condition is true.
     *
     * @param lineReader The LineReader to be read from
     * @param readCondition The read condition
     * @return A LineReader that reads from another LineReader while the read condition is true.
     * @throws java.lang.NullPointerException If lineReader or readCondition is null.
     */
    static LineReader readWhile(final LineReader lineReader, final Predicate<String> readCondition) {
        Objects.requireNonNull(lineReader, "lineReader");
        Objects.requireNonNull(readCondition, "readCondition");
        return new LineReader() {
            @Override
            public String readNextLine() throws NoSuchElementException {
                if (!lineReader.nextLineMatches(readCondition)) {
                    throw new NoSuchElementException();
                }
                return lineReader.readNextLine();
            }

            @Override
            public boolean hasNextLine() {
                return lineReader.nextLineMatches(readCondition);
            }

            @Override
            public boolean nextLineMatches(final Predicate<String> condition) {
                return lineReader.nextLineMatches(readCondition.and(condition));
            }
        };
    }
}
