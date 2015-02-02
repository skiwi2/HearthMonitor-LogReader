package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerLogEntry.GameAccountId;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] CREATE_GAME - Player entries.
 *
 * @author Frank van Heeswijk
 */
public class PlayerEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() -     Player EntityID=2 PlayerID=1 GameAccountId=[hi=144115198130930503 lo=27162067]
     * [Power] GameState.DebugPrintPower() -         tag=TIMEOUT value=75
     * [Power] GameState.DebugPrintPower() -         tag=PLAYSTATE value=PLAYING
     * [Power] GameState.DebugPrintPower() -         tag=CURRENT_PLAYER value=1
     * [Power] GameState.DebugPrintPower() -         tag=FIRST_PLAYER value=1
     * [Power] GameState.DebugPrintPower() -         tag=HERO_ENTITY value=4
     * [Power] GameState.DebugPrintPower() -         tag=MAXHANDSIZE value=10
     * [Power] GameState.DebugPrintPower() -         tag=STARTHANDSIZE value=4
     * [Power] GameState.DebugPrintPower() -         tag=PLAYER_ID value=1
     * [Power] GameState.DebugPrintPower() -         tag=TEAM_ID value=1
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=PLAY
     * [Power] GameState.DebugPrintPower() -         tag=CONTROLLER value=1
     * [Power] GameState.DebugPrintPower() -         tag=ENTITY_ID value=2
     * [Power] GameState.DebugPrintPower() -         tag=MAXRESOURCES value=10
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=PLAYER
     * [Power] GameState.DebugPrintPower() -         tag=NUM_TURNS_LEFT value=1
     */

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *   - followed by zero or more space characters, captured as the 1st group
     *   - followed by literal text 'Player EntityID='
     *   - followed by zero or more characters, captured as the 2nd group
     *   - followed by literal text ' PlayerID='
     *   - followed by zero or more characters, captured as the 3rd group
     *   - followed by literal text ' GameAccountId=[hi='
     *   - followed by zero or more characters, captured as the 4th group
     *   - followed by literal text ' lo='
     *   - followed by zero or more characters, captured as the 5th group
     *   - followed by the literal text ']'
     *   - ending with zero or more characters
     */
    private static final Pattern EXTRACT_PLAYER_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.compile("Player EntityID=") + "(.*)"
            + Pattern.quote(" PlayerID=") + "(.*)" + Pattern.quote(" GameAccountId=[hi=") + "(.*)" + Pattern.quote(" lo=")
            + "(.*)" + Pattern.quote("]") + ".*$");

    @Override
    public boolean isParsable(final String input) {
        return EXTRACT_PLAYER_PATTERN.matcher(input).matches();
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
        PlayerLogEntry.Builder builder = new PlayerLogEntry.Builder();

        Matcher playerMatcher = EXTRACT_PLAYER_PATTERN.matcher(input);
        if (!playerMatcher.find()) {
            throw new NotParsableException();
        }
        int indentation = playerMatcher.group(1).length();
        String entityId = playerMatcher.group(2);
        String playerId = playerMatcher.group(3);
        String gameAccountIdHi = playerMatcher.group(4);
        String gameAccountIdLo = playerMatcher.group(5);

        GameAccountId gameAccountId = new GameAccountId.Builder()
            .hi(gameAccountIdHi)
            .lo(gameAccountIdLo)
            .build();

        builder.indentation(indentation);
        builder.entityId(entityId);
        builder.playerId(playerId);
        builder.gameAccountId(gameAccountId);

        Predicate<String> readCondition = line -> (LogLineUtils.isFromNamedLogger(line) &&
            (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > indentation));

        while (lineReader.nextLineMatches(readCondition)) {
            Matcher tagValueMatcher = EXTRACT_TAG_VALUE_PATTERN.matcher(lineReader.readNextLine());
            if (!tagValueMatcher.find()) {
                throw new NotParsableException();
            }
            int tagValueIndentation = tagValueMatcher.group(1).length();
            if (tagValueIndentation < indentation + 4) {
                //expect tag value pairs to be indented by 4 more than the parent player
                throw new NotParsableException();
            }
            String tag = tagValueMatcher.group(2);
            String value = tagValueMatcher.group(3);
            builder.addTagValuePair(tag, value);
        }

        return builder.build();
    }
}
