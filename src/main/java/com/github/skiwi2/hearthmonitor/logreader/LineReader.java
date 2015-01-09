package com.github.skiwi2.hearthmonitor.logreader;

/**
 * Used to read lines from an input source.
 *
 * This functional interface differs from Supplier<String> in that it can throw a NoMoreInputException.
 *
 * @author Frank van Heeswijk
 */
@FunctionalInterface
public interface LineReader {
    /**
     * Reads a line.
     *
     * @return  The read line.
     * @throws NoMoreInputException If no more input could be obtained.
     */
    String readLine() throws NoMoreInputException;
}
