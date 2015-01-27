package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

/**
 * @author Frank van Heeswijk
 */
public class BExtendedEntryParser implements ExtendedEntryParser {
    @Override
    public boolean isParsable(final String input) {
        return false;
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        throw new NotParsableException();
    }
}
