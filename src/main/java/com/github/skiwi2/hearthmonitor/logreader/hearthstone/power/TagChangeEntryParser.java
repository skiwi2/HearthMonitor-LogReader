package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] TAG_CHANGE entries.
 *
 * @author Frank van Heeswijk
 */
public class TagChangeEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() - TAG_CHANGE Entity=skiwi tag=TIMEOUT value=75
     */

    private boolean restrictIndentation;
    private final int indentation;

    private TagChangeEntryParser() {
        this.indentation = 0;
    }

    private TagChangeEntryParser(final int indentation) {
        this.restrictIndentation = true;
        this.indentation = indentation;
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'TAG_CHANGE Entity='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' tag='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' value='
     *  - ending with zero or more characters, captured as the 4th group
     */
    private static final Pattern EXTRACT_TAG_CHANGE_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("TAG_CHANGE Entity=") + "(.*)"
            + Pattern.quote(" tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_TAG_CHANGE_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException();
        }

        TagChangeLogEntry.Builder builder = new TagChangeLogEntry.Builder();

        Matcher tagChangeMatcher = EXTRACT_TAG_CHANGE_PATTERN.matcher(input);
        if (!tagChangeMatcher.find()) {
            throw new NotParsableException();
        }
        int localIndentation = tagChangeMatcher.group(1).length();
        String entity = tagChangeMatcher.group(2);
        String tag = tagChangeMatcher.group(3);
        String value = tagChangeMatcher.group(4);

        builder.indentation(localIndentation);
        builder.entity(entity);
        builder.tag(tag);
        builder.value(value);

        return builder.build();
    }

    private boolean isValidIndentation(final String input) {
        return (!restrictIndentation || LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static TagChangeEntryParser create() {
        return new TagChangeEntryParser();
    }

    public static TagChangeEntryParser createForIndentation(final int indentation) {
        return new TagChangeEntryParser(indentation);
    }
}
