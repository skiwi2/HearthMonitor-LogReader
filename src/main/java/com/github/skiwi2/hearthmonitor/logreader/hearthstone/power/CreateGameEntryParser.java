package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.GameEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReaderUtils;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.Arrays;
import java.util.HashSet;
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

    private boolean restrictIndentation;
    private final int indentation;

    private CreateGameEntryParser() {
        this.indentation = 0;
    }

    private CreateGameEntryParser(final int indentation) {
        this.restrictIndentation = true;
        this.indentation = indentation;
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - ending with literal text 'CREATE_GAME'
     */
    private static final Pattern EXTRACT_CREATE_GAME_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("CREATE_GAME") + "$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_CREATE_GAME_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException();
        }

        //construct a log reader from the line reader
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader(
            lineReader.readNextLine(),
            lineReader,
            line -> (LogLineUtils.isFromNamedLogger(line) && LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > 0),
            new HashSet<>(Arrays.asList(
                (restrictIndentation) ? GameEntityEntryParser.createForIndentation(indentation + 4) : GameEntityEntryParser.create(),
                (restrictIndentation) ? PlayerEntryParser.createForIndentation(indentation + 4) : PlayerEntryParser.create()
            ))
        );

        try {
            CreateGameLogEntry.Builder builder = new CreateGameLogEntry.Builder();

            Matcher createGameLogEntryMatcher = EXTRACT_CREATE_GAME_PATTERN.matcher(input);
            if (!createGameLogEntryMatcher.find()) {
                throw new NotParsableException();
            }
            int localIndentation = createGameLogEntryMatcher.group(1).length();
            builder.indentation(localIndentation);

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

    private boolean isValidIndentation(final String input) {
        return (!restrictIndentation || LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static CreateGameEntryParser create() {
        return new CreateGameEntryParser();
    }

    public static CreateGameEntryParser createForIndentation(final int indentation) {
        return new CreateGameEntryParser(indentation);
    }
}
