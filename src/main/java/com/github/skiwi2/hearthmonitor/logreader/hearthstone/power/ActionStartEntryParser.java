package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.ActionStartLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.LogReaderUtils;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Used to parse [Power] ACTION_START entries.
 *
 * @author Frank van Heeswijk
 */
public class ActionStartEntryParser implements EntryParser {
    /*
     * [Power] GameState.DebugPrintPower() - ACTION_START Entity=skiwi SubType=TRIGGER Index=-1 Target=0
     * [Power] GameState.DebugPrintPower() -     SHOW_ENTITY - Updating Entity=[id=33 cardId= type=INVALID zone=DECK zonePos=0 player=1] CardID=CS2_062
     * [Power] GameState.DebugPrintPower() -         tag=COST value=4
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=HAND
     * [Power] GameState.DebugPrintPower() -         tag=FACTION value=NEUTRAL
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=ABILITY
     * [Power] GameState.DebugPrintPower() -         tag=RARITY value=FREE
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[id=33 cardId= type=INVALID zone=DECK zonePos=0 player=1] tag=ZONE_POSITION value=2
     * [Power] GameState.DebugPrintPower() -     HIDE_ENTITY - Entity=[name=Dark Iron Dwarf id=27 zone=HAND zonePos=2 cardId=EX1_046 player=1] tag=ZONE value=DECK
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[name=Dark Iron Dwarf id=27 zone=HAND zonePos=2 cardId=EX1_046 player=1] tag=ZONE value=DECK
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[name=Dark Iron Dwarf id=27 zone=HAND zonePos=2 cardId=EX1_046 player=1] tag=ZONE_POSITION value=0
     * [Power] GameState.DebugPrintPower() -     SHOW_ENTITY - Updating Entity=[id=14 cardId= type=INVALID zone=DECK zonePos=0 player=1] CardID=GVG_096
     * [Power] GameState.DebugPrintPower() -         tag=HEALTH value=3
     * [Power] GameState.DebugPrintPower() -         tag=ATK value=4
     * [Power] GameState.DebugPrintPower() -         tag=COST value=4
     * [Power] GameState.DebugPrintPower() -         tag=ZONE value=HAND
     * [Power] GameState.DebugPrintPower() -         tag=CARDTYPE value=MINION
     * [Power] GameState.DebugPrintPower() -         tag=RARITY value=COMMON
     * [Power] GameState.DebugPrintPower() -         tag=DEATHRATTLE value=1
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[id=14 cardId= type=INVALID zone=DECK zonePos=0 player=1] tag=ZONE_POSITION value=3
     * [Power] GameState.DebugPrintPower() -     HIDE_ENTITY - Entity=[name=Dread Infernal id=34 zone=HAND zonePos=3 cardId=CS2_064 player=1] tag=ZONE value=DECK
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[name=Dread Infernal id=34 zone=HAND zonePos=3 cardId=CS2_064 player=1] tag=ZONE value=DECK
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=[name=Dread Infernal id=34 zone=HAND zonePos=3 cardId=CS2_064 player=1] tag=ZONE_POSITION value=0
     * [Power] GameState.DebugPrintPower() -     TAG_CHANGE Entity=skiwi tag=MULLIGAN_STATE value=WAITING
     * [Power] GameState.DebugPrintPower() - ACTION_END
     */

    private final int indentation;
    private final Set<? extends EntryParser.Factory<? extends EntryParser>> otherEntryParserFactories;

    private ActionStartEntryParser(final int indentation, final Set<? extends EntryParser.Factory<? extends EntryParser>> otherEntryParserFactories) {
        Objects.requireNonNull(otherEntryParserFactories, "otherEntryParserFactories");
        this.indentation = indentation;
        this.otherEntryParserFactories = new HashSet<>(otherEntryParserFactories);
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Power] GameState.DebugPrintPower() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'ACTION_START Entity='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' SubType='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' Index='
     *  - followed by zero or more characters, captured as the 4th group
     *  - followed by literal text ' Target='
     *  - ending with zero or more characters, captured as the 5th group
     */
    private static final Pattern EXTRACT_ACTION_START_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Power] GameState.DebugPrintPower() - ") + "(\\s*)" + Pattern.quote("ACTION_START Entity=") + "(.*)" + Pattern.quote(" SubType=")
            + "(.*)" + Pattern.quote(" Index=") + "(.*)" + Pattern.quote(" Target=") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_ACTION_START_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException(input);
        }

        Set<EntryParser> entryParsers = otherEntryParserFactories.stream()
            .map(factory -> factory.create(indentation + 4))
            .collect(Collectors.<EntryParser>toSet());

        //add itself
        entryParsers.add(new Factory(otherEntryParserFactories).create(indentation + 4));

        //construct a log reader from the line reader
        String nextInput = lineReader.readNextLine();
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader(
            nextInput,
            lineReader,
            line -> (LogLineUtils.isFromNamedLogger(line) && LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > indentation),
            entryParsers
        );

        try {
            ActionStartLogEntry.Builder builder = new ActionStartLogEntry.Builder();

            Matcher actionStartLogEntryMatcher = EXTRACT_ACTION_START_PATTERN.matcher(input);
            if (!actionStartLogEntryMatcher.find()) {
                throw new NotParsableException();
            }
            int localIndentation = actionStartLogEntryMatcher.group(1).length();
            String entity = actionStartLogEntryMatcher.group(2);
            String subtype = actionStartLogEntryMatcher.group(3);
            String index = actionStartLogEntryMatcher.group(4);
            String target = actionStartLogEntryMatcher.group(5);

            EntityObjectParser entityObjectParser = new EntityObjectParser();

            if (!entityObjectParser.isParsable(entity)) {
                throw new NotParsableException();
            }
            EntityLogObject entityLogObject = (EntityLogObject)entityObjectParser.parse(entity);

            if (!entityObjectParser.isParsable(index)) {
                throw new NotParsableException();
            }
            EntityLogObject indexLogObject = (EntityLogObject)entityObjectParser.parse(index);

            if (!entityObjectParser.isParsable(target)) {
                throw new NotParsableException();
            }
            EntityLogObject targetLogObject = (EntityLogObject)entityObjectParser.parse(target);

            builder.indentation(localIndentation);
            builder.entity(entityLogObject);
            builder.subtype(subtype);
            builder.index(indexLogObject);
            builder.target(targetLogObject);

            if (nextInput.equals("[Power] GameState.DebugPrintPower() - " + String.join("", Collections.nCopies(indentation, " ")) + "ACTION_END")) {
                //return early if no more sub log entries have been found
                return builder.build();
            }

            while (logReader.hasNextEntry()) {
                builder.addLogEntry(logReader.readNextEntry());
            }

            if (lineReader.nextLineMatches(line -> line.equals("[Power] GameState.DebugPrintPower() - " + String.join("", Collections.nCopies(indentation, " ")) + "ACTION_END"))) {
                lineReader.readNextLine();
            }

            return builder.build();
        } catch (NotReadableException ex) {
            throw new NotParsableException(ex);
        }
    }

    private boolean isValidIndentation(final String input) {
        return (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static EntryParser.Factory<ActionStartEntryParser> createFactory(final Set<? extends EntryParser.Factory<? extends EntryParser>> entryParserFactories) {
        return new Factory(entryParserFactories);
    }

    public static ActionStartEntryParser createParser(final int indentation, final Set<? extends EntryParser.Factory<? extends EntryParser>> entryParserFactories) {
        return createFactory(entryParserFactories).create(indentation);
    }

    public static class Factory implements EntryParser.Factory<ActionStartEntryParser> {
        private final Set<? extends EntryParser.Factory<? extends EntryParser>> otherEntryParserFactories;

        public Factory(final Set<? extends EntryParser.Factory<? extends EntryParser>> otherEntryParserFactories) {
            this.otherEntryParserFactories = otherEntryParserFactories;
        }

        @Override
        public ActionStartEntryParser create(final int indentation) {
            return new ActionStartEntryParser(indentation, otherEntryParserFactories);
        }
    }
}
