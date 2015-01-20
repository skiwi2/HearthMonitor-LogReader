package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
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
public class FullEntityEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() - FULL_ENTITY - Creating ID=4 CardID=HERO_07
     * [Power] GameState.DebugPrintPower() -     tag=HEALTH value=30
     * [Power] GameState.DebugPrintPower() -     tag=ZONE value=PLAY
     * [Power] GameState.DebugPrintPower() -     tag=CONTROLLER value=1
     * [Power] GameState.DebugPrintPower() -     tag=ENTITY_ID value=4
     * [Power] GameState.DebugPrintPower() -     tag=FACTION value=NEUTRAL
     * [Power] GameState.DebugPrintPower() -     tag=CARDTYPE value=HERO
     * [Power] GameState.DebugPrintPower() -     tag=RARITY value=FREE
     */

    @Override
    public boolean isParsable(final String input) {
        return input.startsWith("[Power] GameState.DebugPrintPower() - FULL_ENTITY - Creating ID=");
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - FULL_ENTITY - Creating ID='
     *  - followed by zero or more characters, captured as the 1st group
     *  - followed by literal text ' CardID='
     *  - ending with zero or more characters, captured as the 2nd group
     */
    private static final Pattern EXTRACT_FULL_ENTITY_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - FULL_ENTITY - Creating ID=") + "(.*)"
            + Pattern.quote(" CardID=") + "(.*)$");

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with literal text '[Power] GameState.DebugPrintPower() -     tag='
     *   - followed by zero or more characters, captured as the 1st group
     *   - followed by literal text ' value='
     *   - ending with zero or more characters, captured as the 2nd group
     */
    private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -     tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        FullEntityLogEntry.Builder builder = new FullEntityLogEntry.Builder();

        Matcher fullEntityMatcher = EXTRACT_FULL_ENTITY_PATTERN.matcher(input);
        if (!fullEntityMatcher.find()) {
            throw new NotParsableException();
        }
        String id = fullEntityMatcher.group(1);
        String cardId = fullEntityMatcher.group(2);

        builder.id(id);
        builder.cardId(cardId);

        Predicate<String> readCondition = line -> (LogLineUtils.isFromNamedLogger(line) &&
            (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > 0));

        while (lineReader.nextLineMatches(readCondition)) {
            Matcher tagValueMatcher = EXTRACT_TAG_VALUE_PATTERN.matcher(lineReader.readNextLine());
            if (!tagValueMatcher.find()) {
                throw new NotParsableException();
            }
            String tag = tagValueMatcher.group(1);
            String value = tagValueMatcher.group(2);
            builder.addTagValuePair(tag, value);
        }

        return builder.build();
    }
}
