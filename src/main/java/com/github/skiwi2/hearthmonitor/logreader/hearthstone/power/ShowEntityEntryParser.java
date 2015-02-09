package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] FULL_ENTITY entries.
 *
 * @author Frank van Heeswijk
 */
public class ShowEntityEntryParser implements EntryParser {
    /**
     * [Power] GameState.DebugPrintPower() -     SHOW_ENTITY - Updating Entity=[id=33 cardId= type=INVALID zone=DECK zonePos=0 player=1] CardID=CS2_062
     * [Power] GameState.DebugPrintPower() -         tag=COST value=4
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=HAND
     * [Power] GameState.DebugPrintPower() -         tag=FACTION value=NEUTRAL
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=ABILITY
     * [Power] GameState.DebugPrintPower() -         tag=RARITY value=FREE
     */

    private boolean restrictIndentation;
    private final int indentation;

    private ShowEntityEntryParser() {
        this.indentation = 0;
    }

    private ShowEntityEntryParser(final int indentation) {
        this.restrictIndentation = true;
        this.indentation = indentation;
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'SHOW_ENTITY - Updating Entity='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' CardID='
     *  - ending with zero or more characters, captured as the 3rd group
     */
    private static final Pattern EXTRACT_SHOW_ENTITY_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("SHOW_ENTITY - Updating Entity=") + "(.*)"
            + Pattern.quote(" CardID=") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_SHOW_ENTITY_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *   - followed by four or more space characters, captured as the 1st group
     *   - followed by literal text 'tag='
     *   - followed by zero or more characters, captured as the 2nd group
     *   - followed by literal text ' value='
     *   - ending with zero or more characters, captured as the 3rd group
     */
    private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s{4,})" + Pattern.quote("tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException();
        }

        ShowEntityLogEntry.Builder builder = new ShowEntityLogEntry.Builder();

        Matcher showEntityMatcher = EXTRACT_SHOW_ENTITY_PATTERN.matcher(input);
        if (!showEntityMatcher.find()) {
            throw new NotParsableException();
        }
        int localIndentation = showEntityMatcher.group(1).length();
        String entity = showEntityMatcher.group(2);
        String cardId = showEntityMatcher.group(3);

        EntityObjectParser entityObjectParser = new EntityObjectParser();
        if (!entityObjectParser.isParsable(entity)) {
            throw new NotParsableException();
        }
        EntityLogObject entityLogObject = (EntityLogObject)entityObjectParser.parse(entity);

        builder.indentation(localIndentation);
        builder.entity(entityLogObject);
        builder.cardId(cardId);

        Predicate<String> readCondition = line -> (LogLineUtils.isFromNamedLogger(line) &&
            (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > localIndentation));

        while (lineReader.nextLineMatches(readCondition)) {
            Matcher tagValueMatcher = EXTRACT_TAG_VALUE_PATTERN.matcher(lineReader.readNextLine());
            if (!tagValueMatcher.find()) {
                throw new NotParsableException();
            }
            int tagValueIndentation = tagValueMatcher.group(1).length();
            if (tagValueIndentation < localIndentation + 4) {
                //expect tag value pairs to be indented by 4 more than the parent game entity
                throw new NotParsableException();
            }
            String tag = tagValueMatcher.group(2);
            String value = tagValueMatcher.group(3);
            builder.addTagValuePair(tag, value);
        }

        return builder.build();
    }

    private boolean isValidIndentation(final String input) {
        return (!restrictIndentation || LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static ShowEntityEntryParser create() {
        return new ShowEntityEntryParser();
    }

    public static ShowEntityEntryParser createForIndentation(final int indentation) {
        return new ShowEntityEntryParser(indentation);
    }
}
