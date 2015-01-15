package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

/**
 * Used to parse lines from a log source.
 *
 * It has the option to read more lines from the log source via a line reader if deemed necessary.
 *
 * @author Frank van Heeswijk
 */
public interface EntryParser {
    /**
     * Returns whether this entry parser can parse the input.
     *
     * @param input The input check parsability for
     * @return  Whether this entry parser can parse the input.
     */
    boolean isParsable(final String input);

    /**
     * Parses the input String resulting in a LogEntry.
     *
     * If deemed necessary, extra lines may be obtained from the LineReader.
     *
     * @param input The input to parse
     * @param lineReader    The line reader from which extra lines can be obtained
     * @return  The LogEntry obtained after parsing the input.
     * @throws NotParsableException If this entry reader cannot parse the input to return a LogEntry.
     * @throws NoMoreInputException If the line reader has no more input.
     */
    LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException;
}
