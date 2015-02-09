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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private boolean restrictIndentation;
    private final int indentation;
    private final Set<EntryParser> entryParsers;

    private ActionStartEntryParser(final Set<EntryParser> entryParsers) {
        Objects.requireNonNull(entryParsers, "entryParsers");
        this.indentation = 0;
        this.entryParsers = new HashSet<>(entryParsers);
    }

    private ActionStartEntryParser(final int indentation, final Set<EntryParser> entryParsers) {
        Objects.requireNonNull(entryParsers, "entryParsers");
        this.restrictIndentation = true;
        this.indentation = indentation;
        this.entryParsers = new HashSet<>(entryParsers);
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
            throw new NotParsableException();
        }

        //TODO clean this up
        Set<EntryParser> innerEntryParsers;
        try {
            innerEntryParsers = entryParsers.stream()
                .map(Object::getClass)
                .map(clazz -> {
                    try {
                        if (restrictIndentation) {
                            if (clazz.equals(ActionStartEntryParser.class)) {
                                Method method = clazz.getDeclaredMethod("createForIndentation", int.class, Set.class);
                                return (EntryParser)method.invoke(null, indentation + 4, entryParsers);
                            }
                            Method method = clazz.getDeclaredMethod("createForIndentation", int.class);
                            return (EntryParser)method.invoke(null, indentation + 4);
                        }
                        if (clazz.equals(ActionStartEntryParser.class)) {
                            Method method = clazz.getDeclaredMethod("create", Set.class);
                            return (EntryParser)method.invoke(null, entryParsers);
                        }
                        Method method = clazz.getDeclaredMethod("create");
                        return (EntryParser)method.invoke(null);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toSet());
        } catch (RuntimeException ex) {
            throw new NotParsableException(ex);
        }

        //construct a log reader from the line reader
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader(
            lineReader.readNextLine(),
            lineReader,
            line -> (LogLineUtils.isFromNamedLogger(line) && LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(line)) > indentation),
            innerEntryParsers
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

            while (logReader.hasNextEntry()) {
                builder.addLogEntry(logReader.readNextEntry());
            }

            return builder.build();
        } catch (NotReadableException ex) {
            throw new NotParsableException(ex);
        }
    }

    private boolean isValidIndentation(final String input) {
        return (!restrictIndentation || LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static ActionStartEntryParser create(final Set<EntryParser> entryParsers) {
        return new ActionStartEntryParser(entryParsers);
    }

    public static ActionStartEntryParser createForIndentation(final int indentation, final Set<EntryParser> entryParsers) {
        return new ActionStartEntryParser(indentation, entryParsers);
    }
}
