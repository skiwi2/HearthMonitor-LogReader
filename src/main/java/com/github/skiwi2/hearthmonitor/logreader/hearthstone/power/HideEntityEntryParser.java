package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.HideEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] HIDE_ENTITY entries.
 *
 * @author Frank van Heeswijk
 */
public class HideEntityEntryParser implements EntryParser {
    /**
     * [Power] GameState.DebugPrintPower() -     HIDE_ENTITY - Entity=[name=Dread Infernal id=34 zone=HAND zonePos=3 cardId=CS2_064 player=1] tag=ZONE value=DECK
     */

    private boolean restrictIndentation;
    private final int indentation;

    private HideEntityEntryParser() {
        this.indentation = 0;
    }

    private HideEntityEntryParser(final int indentation) {
        this.restrictIndentation = true;
        this.indentation = indentation;
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'HIDE_ENTITY - Entity='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' tag='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' value='
     *  - ending with zero or more characters, captured as the 4th group
     */
    private static final Pattern EXTRACT_HIDE_ENTITY_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("HIDE_ENTITY - Entity=") + "(.*)"
            + Pattern.quote(" tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_HIDE_ENTITY_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException();
        }

        HideEntityLogEntry.Builder builder = new HideEntityLogEntry.Builder();

        Matcher hideEntityEntryMatcher = EXTRACT_HIDE_ENTITY_PATTERN.matcher(input);
        if (!hideEntityEntryMatcher.find()) {
            throw new NotParsableException();
        }
        int localIndentation = hideEntityEntryMatcher.group(1).length();
        String entity = hideEntityEntryMatcher.group(2);
        String tag = hideEntityEntryMatcher.group(3);
        String value = hideEntityEntryMatcher.group(4);

        EntityObjectParser entityObjectParser = new EntityObjectParser();
        if (!entityObjectParser.isParsable(entity)) {
            throw new NotParsableException();
        }
        EntityLogObject entityLogObject = (EntityLogObject)entityObjectParser.parse(entity);

        builder.indentation(localIndentation);
        builder.entity(entityLogObject);
        builder.tag(tag);
        builder.value(value);

        return builder.build();
    }

    private boolean isValidIndentation(final String input) {
        return (!restrictIndentation || LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static HideEntityEntryParser create() {
        return new HideEntityEntryParser();
    }

    public static HideEntityEntryParser createForIndentation(final int indentation) {
        return new HideEntityEntryParser(indentation);
    }
}
