package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogObject;

/**
 * Used to parse log objects from a string.
 *
 * @author Frank van Heeswijk
 */
public interface ObjectParser {
    /**
     * Returns whether this object parser can parse the input.
     *
     * @param input The input to check parsability for
     * @return  Whether this object parser can parse the input.
     */
    boolean isParsable(final String input);

    /**
     * Parses the input String resulting in a LogObject.
     *
     * @param input The input to parse
     * @return  The LogObject obtained after parsing the input.
     * @throws com.github.skiwi2.hearthmonitor.logreader.NotParsableException   If this entry reader cannot parse the input to return a LogObject.
     */
    LogObject parse(final String input) throws NotParsableException;
}
