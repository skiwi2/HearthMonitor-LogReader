package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.GameEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry.GameAccountId;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReaderUtils;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] CREATE_GAME entries.
 *
 * @author Frank van Heeswijk
 */
public class CreateGameEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() - CREATE_GAME
     * [Power] GameState.DebugPrintPower() -     GameEntity EntityID=1
     * [Power] GameState.DebugPrintPower() -         tag=10 value=85
     * [Power] GameState.DebugPrintPower() -         tag=TURN value=1
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=PLAY
     * [Power] GameState.DebugPrintPower() -         tag=ENTITY_ID value=1
     * [Power] GameState.DebugPrintPower() -         tag=NEXT_STEP value=BEGIN_MULLIGAN
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=GAME
     * [Power] GameState.DebugPrintPower() -         tag=STATE value=RUNNING
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
     * [Power] GameState.DebugPrintPower() -     Player EntityID=3 PlayerID=2 GameAccountId=[hi=144115198130930503 lo=37543301]
     * [Power] GameState.DebugPrintPower() -         tag=TIMEOUT value=75
     * [Power] GameState.DebugPrintPower() -         tag=PLAYSTATE value=PLAYING
     * [Power] GameState.DebugPrintPower() -         tag=HERO_ENTITY value=36
     * [Power] GameState.DebugPrintPower() -         tag=MAXHANDSIZE value=10
     * [Power] GameState.DebugPrintPower() -         tag=STARTHANDSIZE value=4
     * [Power] GameState.DebugPrintPower() -         tag=PLAYER_ID value=2
     * [Power] GameState.DebugPrintPower() -         tag=TEAM_ID value=2
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=PLAY
     * [Power] GameState.DebugPrintPower() -         tag=CONTROLLER value=2
     * [Power] GameState.DebugPrintPower() -         tag=ENTITY_ID value=3
     * [Power] GameState.DebugPrintPower() -         tag=MAXRESOURCES value=10
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=PLAYER
     * [Power] GameState.DebugPrintPower() -         tag=NUM_TURNS_LEFT value=1
     */

    @Override
    public boolean isParsable(final String input) {
        return input.equals("[Power] GameState.DebugPrintPower() - CREATE_GAME");
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        //construct a log reader from the line reader
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader(
            lineReader.readNextLine(),
            lineReader,
            line -> (LogLineUtils.isFromNamedLogger(line) && LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > 0),
            () -> new HashSet<>(Arrays.asList(
                new GameEntityEntryParser(),
                new PlayerEntryParser()
            ))
        );

        try {
            CreateGameLogEntry.Builder builder = new CreateGameLogEntry.Builder();
            GameEntityLogEntry gameEntityLogEntry = (GameEntityLogEntry)logReader.readNextEntry();
            builder.gameEntityLogEntry(gameEntityLogEntry);

            while (logReader.hasNextEntry()) {
                PlayerLogEntry playerLogEntry = (PlayerLogEntry)logReader.readNextEntry();
                builder.addPlayerLogEntry(playerLogEntry);
            }

            return builder.build();
        } catch (NotReadableException ex) {
            throw new NotParsableException(ex);
        }
    }

    /**
     * Used to parse [Power] CREATE_GAME - GameEntity entries.
     */
    public static class GameEntityEntryParser implements EntryParser {
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

        @Override
        public boolean isParsable(final String input) {
            return input.startsWith("[Power] GameState.DebugPrintPower() -     GameEntity");
        }

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -     GameEntity EntityID='
         *   - ending with zero or more characters, captured as the 1st group
         */
        private static final Pattern EXTRACT_GAME_ENTITY_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -     GameEntity EntityID=") + "(.*)$");

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -         tag='
         *   - followed by zero or more characters, captured as the 1st group
         *   - followed by literal text ' value='
         *   - ending with zero or more characters, captured as the 2nd group
         */
        private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -         tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
            GameEntityLogEntry.Builder builder = new GameEntityLogEntry.Builder();

            Matcher gameEntityMatcher = EXTRACT_GAME_ENTITY_PATTERN.matcher(input);
            if (!gameEntityMatcher.find()) {
                throw new NotParsableException();
            }
            String entityId = gameEntityMatcher.group(1);
            builder.entityId(entityId);

            Predicate<String> readCondition = line -> (LogLineUtils.isFromNamedLogger(line) &&
                (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > 4));

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

    /**
     * Used to parse [Power] CREATE_GAME - Player entries.
     */
    public static class PlayerEntryParser implements EntryParser {
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

        @Override
        public boolean isParsable(final String input) {
            return input.startsWith("[Power] GameState.DebugPrintPower() -     Player");
        }

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -     Player EntityID='
         *   - followed by zero or more characters, captured as the 1st group
         *   - followed by literal text ' PlayerID='
         *   - followed by zero or more characters, captured as the 2nd group
         *   - followed by literal text ' GameAccountId=[hi='
         *   - followed by zero or more characters, captured as the 3rd group
         *   - followed by literal text ' lo='
         *   - followed by zero or more characters, captured as the 4th group
         *   - followed by the literal text ']'
         *   - ending with zero or more characters
         */
        private static final Pattern EXTRACT_PLAYER_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -     Player EntityID=") + "(.*)"
                + Pattern.quote(" PlayerID=") + "(.*)" + Pattern.quote(" GameAccountId=[hi=") + "(.*)" + Pattern.quote(" lo=")
                + "(.*)" + Pattern.quote("]") + ".*$");

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -         tag='
         *   - followed by zero or more characters, captured as the 1st group
         *   - followed by literal text ' value='
         *   - ending with zero or more characters, captured as the 2nd group
         */
        private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -         tag=") + "(.*)" + Pattern.quote(" value=") + "(.*)$");

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
            PlayerLogEntry.Builder builder = new PlayerLogEntry.Builder();

            Matcher playerMatcher = EXTRACT_PLAYER_PATTERN.matcher(input);
            if (!playerMatcher.find()) {
                throw new NotParsableException();
            }
            String entityId = playerMatcher.group(1);
            String playerId = playerMatcher.group(2);
            String gameAccountIdHi = playerMatcher.group(3);
            String gameAccountIdLo = playerMatcher.group(4);

            GameAccountId gameAccountId = new GameAccountId.Builder()
                .hi(gameAccountIdHi)
                .lo(gameAccountIdLo)
                .build();

            builder.entityId(entityId);
            builder.playerId(playerId);
            builder.gameAccountId(gameAccountId);

            Predicate<String> readCondition = line -> (LogLineUtils.isFromNamedLogger(line) &&
                (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > 4));

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
}
