package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.GameEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] CREATE_GAME - GameEntity entries.
 *
 * @author Frank van Heeswijk
 */
public class GameEntityEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() -     GameEntity EntityID=1
     * [Power] GameState.DebugPrintPower() -         tag=10 value=85
     * [Power] GameState.DebugPrintPower() -         tag=TURN value=1
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=PLAY
     * [Power] GameState.DebugPrintPower() -         tag=ENTITY_ID value=1
     * [Power] GameState.DebugPrintPower() -         tag=NEXT_STEP value=BEGIN_MULLIGAN
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=GAME
     * [Power] GameState.DebugPrintPower() -         tag=STATE value=RUNNING
     */

    private boolean restrictIndentation;
    private final int indentation;

    private GameEntityEntryParser() {
        this.indentation = 0;
    }

    private GameEntityEntryParser(final int indentation) {
        this.restrictIndentation = true;
        this.indentation = indentation;
    }

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *   - followed by zero or more space characters, captured as the 1st group
     *   - followed by literal text 'GameEntity EntityID='
     *   - ending with zero or more characters, captured as the 2nd group
     */
    private static final Pattern EXTRACT_GAME_ENTITY_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("GameEntity EntityID=") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_GAME_ENTITY_PATTERN.matcher(input).matches() && isValidIndentation(input));
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

        GameEntityLogEntry.Builder builder = new GameEntityLogEntry.Builder();

        Matcher gameEntityMatcher = EXTRACT_GAME_ENTITY_PATTERN.matcher(input);
        if (!gameEntityMatcher.find()) {
            throw new NotParsableException();
        }
        int localIndentation = gameEntityMatcher.group(1).length();
        String entityId = gameEntityMatcher.group(2);
        builder.indentation(localIndentation);
        builder.entityId(entityId);

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

    public static GameEntityEntryParser create() {
        return new GameEntityEntryParser();
    }

    public static GameEntityEntryParser createForIndentation(final int indentation) {
        return new GameEntityEntryParser(indentation);
    }
}