package com.github.skiwi2.hearthmonitor.logreader;

import java.util.NoSuchElementException;
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
}
