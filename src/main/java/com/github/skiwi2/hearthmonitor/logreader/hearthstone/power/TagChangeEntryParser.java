package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Frank van Heeswijk
 */
public class TagChangeEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() - TAG_CHANGE Entity=skiwi tag=TIMEOUT value=75
     */

    @Override
    public boolean isParsable(final String input) {
        return input.startsWith("[Power] GameState.DebugPrintPower() - TAG_CHANGE Entity=");
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - TAG_CHANGE Entity='
     *  - followed by zero or more characters, captured as the 1st group
     *  - followed by literal text ' tag='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' value='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - ending with zero or more characters
     */
    private static final Pattern EXTRACT_TAG_CHANGE_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - TAG_CHANGE Entity=") + "(.*)"
             + Pattern.quote(" tag=") + "(.*)" + Pattern.quote(" value=") + "(.*).*");

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        TagChangeLogEntry.Builder builder = new TagChangeLogEntry.Builder();

        Matcher tagChangeMatcher = EXTRACT_TAG_CHANGE_PATTERN.matcher(input);
        if (!tagChangeMatcher.find()) {
            throw new NotParsableException();
        }
        String entity = tagChangeMatcher.group(1);
        String tag = tagChangeMatcher.group(2);
        String value = tagChangeMatcher.group(3);

        builder.entity(entity);
        builder.tag(tag);
        builder.value(value);

        return builder.build();
    }
}
