package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Optional;

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
     * @throws NoMoreInputException If no more input could be obtained.
     */
    String readLine() throws NoMoreInputException;

    /**
     * Peeks into the next line if more input is present, meaning that the line is not being consumed from the input source.
     *
     * @return  The next line, if present.
     */
    Optional<String> peekLine();
}
