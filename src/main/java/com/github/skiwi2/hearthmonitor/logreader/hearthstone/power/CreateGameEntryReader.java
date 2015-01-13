package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.GameEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry.GameAccountId;
import com.github.skiwi2.hearthmonitor.logreader.EntryReader;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReaderUtils;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Power] CREATE_GAME entries.
 *
 * @author Frank van Heeswijk
 */
public class CreateGameEntryReader implements EntryReader {
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
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
        //construct a log reader from the line reader
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader(
            lineReader.readLine(),
            lineReader,
            line -> true,
            new HashSet<>(Arrays.asList(
                new GameEntityEntryReader(),
                new PlayerEntryReader()
            ))
        );

        try {
            CreateGameLogEntry.Builder builder = new CreateGameLogEntry.Builder();
            GameEntityLogEntry gameEntityLogEntry = (GameEntityLogEntry)logReader.readEntry();
            builder.gameEntityLogEntry(gameEntityLogEntry);

            while (true) {
                try {
                    PlayerLogEntry playerLogEntry = (PlayerLogEntry)logReader.readEntry();
                    builder.addPlayerLogEntry(playerLogEntry);
                } catch (NoMoreInputException ex) {
                    break;
                }
            }

            return builder.build();
        } catch (NotReadableException ex) {
            throw new NotParsableException(ex);
        }
    }

    /**
     * Used to parse [Power] CREATE_GAME - GameEntity entries.
     */
    public static class GameEntityEntryReader implements EntryReader {
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
         *   - followed by one or more digits, captured as the 1st group
         *   - ending with zero or more characters
         */
        private static final Pattern EXTRACT_GAME_ENTITY_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -     GameEntity EntityID=") + "(\\d+).*$");

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -         tag='
         *   - followed by one or more word characters, captured as the 1st group
         *   - followed by literal text ' value='
         *   - followed by one or more word characters, captured as the 2nd group
         *   - ending with zero or more characters
         */
        private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -         tag=") + "(\\w+)" + Pattern.quote(" value=") + "(\\w+).*$");

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
            GameEntityLogEntry.Builder builder = new GameEntityLogEntry.Builder();

            Matcher gameEntityMatcher = EXTRACT_GAME_ENTITY_PATTERN.matcher(input);
            if (!gameEntityMatcher.find()) {
                throw new NotParsableException();
            }
            String entityId = gameEntityMatcher.group(1);
            builder.entityId(entityId);

            Optional<String> nextLine = lineReader.peekLine();
            while (nextLine.isPresent()) {
                if (!LogLineUtils.isFromNamedLogger(nextLine.get()) || LogLineUtils.getNumberOfSpaces(nextLine.get()) <= 4) {
                    //if the next line does not look like a tag value line
                    break;
                }
                Matcher tagValueMatcher = EXTRACT_TAG_VALUE_PATTERN.matcher(lineReader.readLine());
                if (!tagValueMatcher.find()) {
                    throw new NotParsableException();
                }
                String tag = tagValueMatcher.group(1);
                String value = tagValueMatcher.group(2);
                builder.addTagValuePair(tag, value);
                nextLine = lineReader.peekLine();
            }

            return builder.build();
        }
    }

    /**
     * Used to parse [Power] CREATE_GAME - Player entries.
     */
    public static class PlayerEntryReader implements EntryReader {
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
         *   - followed by one or more digits, captured as the 1st group
         *   - followed by literal text ' PlayerID='
         *   - followed by one or more digits, captured as the 2nd group
         *   - followed by literal text ' GameAccountId=[hi='
         *   - followed by one or more digits, captured as the 3rd group
         *   - followed by literal text ' lo='
         *   - followed by one or more digits, captured as the 4th group
         *   - followed by the literal text ']'
         *   - ending with zero or more characters
         */
        private static final Pattern EXTRACT_PLAYER_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -     Player EntityID=") + "(\\d+)"
                + Pattern.quote(" PlayerID=") + "(\\d+)" + Pattern.quote(" GameAccountId=[hi=") + "(\\d+)" + Pattern.quote(" lo=")
                + "(\\d+)" + Pattern.quote("]") + ".*$");

        /**
         * Pattern that checks if a string matches the following:
         *   - starts with literal text '[Power] GameState.DebugPrintPower() -         tag='
         *   - followed by one or more word characters, captured as the 1st group
         *   - followed by literal text ' value='
         *   - followed by one or more word characters, captured as the 2nd group
         *   - ending with zero or more characters
         */
        private static final Pattern EXTRACT_TAG_VALUE_PATTERN =
            Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() -         tag=") + "(\\w+)" + Pattern.quote(" value=") + "(\\w+).*$");

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
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

            Optional<String> nextLine = lineReader.peekLine();
            while (nextLine.isPresent()) {
                if (!LogLineUtils.isFromNamedLogger(nextLine.get()) || LogLineUtils.getNumberOfSpaces(nextLine.get()) <= 4) {
                    //if the next line does not look like a tag value line
                    break;
                }
                Matcher tagValueMatcher = EXTRACT_TAG_VALUE_PATTERN.matcher(lineReader.readLine());
                if (!tagValueMatcher.find()) {
                    throw new NotParsableException();
                }
                String tag = tagValueMatcher.group(1);
                String value = tagValueMatcher.group(2);
                builder.addTagValuePair(tag, value);
                nextLine = lineReader.peekLine();
            }

            return builder.build();
        }
    }
}
