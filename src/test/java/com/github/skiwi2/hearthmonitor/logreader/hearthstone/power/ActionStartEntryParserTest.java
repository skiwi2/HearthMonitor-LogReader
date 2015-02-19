package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.ActionStartLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.HideEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ActionStartEntryParserTest {
    private static final Set<EntryParser.Factory<? extends EntryParser>> INNER_ENTRY_PARSER_FACTORIES = new HashSet<>(Arrays.asList(
        ShowEntityEntryParser.createFactory(),
        TagChangeEntryParser.createFactory(),
        HideEntityEntryParser.createFactory()
    ));

    @Test
    public void testActionStart() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ActionStart.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ActionStartEntryParser.createParser(0, INNER_ENTRY_PARSER_FACTORIES))))) {
            ActionStartLogEntry actionStartLogEntry = (ActionStartLogEntry)logReader.readNextEntry();

            assertEquals(0, actionStartLogEntry.getIndentation());
            assertEquals("skiwi", ((PlayerEntityLogObject)actionStartLogEntry.getEntity()).getName());
            assertEquals("TRIGGER", actionStartLogEntry.getSubtype());
            assertEquals("-1", ((PlayerEntityLogObject)actionStartLogEntry.getIndex()).getName());
            assertEquals("0", ((PlayerEntityLogObject)actionStartLogEntry.getTarget()).getName());

            //all sub entries on indentation 4
            assertTrue(actionStartLogEntry.getLogEntries().stream().allMatch(logEntry -> {
                if (logEntry instanceof ShowEntityLogEntry) {
                    return (((ShowEntityLogEntry)logEntry).getIndentation() == 4);
                } else if (logEntry instanceof TagChangeLogEntry) {
                    return (((TagChangeLogEntry)logEntry).getIndentation() == 4);
                } else if (logEntry instanceof HideEntityLogEntry) {
                    return (((HideEntityLogEntry)logEntry).getIndentation() == 4);
                }
                return false;
            }));

            List<Class<?>> expectedClasses = Arrays.asList(
                ShowEntityLogEntry.class,
                TagChangeLogEntry.class,
                HideEntityLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class,
                ShowEntityLogEntry.class,
                TagChangeLogEntry.class,
                HideEntityLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class
            );
            List<Class<?>> actualClasses = actionStartLogEntry.getLogEntries().stream()
                .map(Object::getClass)
                .collect(Collectors.toList());
            assertEquals(expectedClasses, actualClasses);
        }
    }

    @Test
    public void testActionStartIndented() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ActionStart-indented.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ActionStartEntryParser.createParser(4, INNER_ENTRY_PARSER_FACTORIES))))) {
            ActionStartLogEntry actionStartLogEntry = (ActionStartLogEntry)logReader.readNextEntry();

            assertEquals(4, actionStartLogEntry.getIndentation());
            assertEquals("skiwi", ((PlayerEntityLogObject)actionStartLogEntry.getEntity()).getName());
            assertEquals("TRIGGER", actionStartLogEntry.getSubtype());
            assertEquals("-1", ((PlayerEntityLogObject)actionStartLogEntry.getIndex()).getName());
            assertEquals("0", ((PlayerEntityLogObject)actionStartLogEntry.getTarget()).getName());

            //all sub entries on indentation 8
            assertTrue(actionStartLogEntry.getLogEntries().stream().allMatch(logEntry -> {
                if (logEntry instanceof ShowEntityLogEntry) {
                    return (((ShowEntityLogEntry)logEntry).getIndentation() == 8);
                } else if (logEntry instanceof TagChangeLogEntry) {
                    return (((TagChangeLogEntry)logEntry).getIndentation() == 8);
                } else if (logEntry instanceof HideEntityLogEntry) {
                    return (((HideEntityLogEntry)logEntry).getIndentation() == 8);
                }
                return false;
            }));

            List<Class<?>> expectedClasses = Arrays.asList(
                ShowEntityLogEntry.class,
                TagChangeLogEntry.class,
                HideEntityLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class,
                ShowEntityLogEntry.class,
                TagChangeLogEntry.class,
                HideEntityLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class,
                TagChangeLogEntry.class
            );
            List<Class<?>> actualClasses = actionStartLogEntry.getLogEntries().stream()
                .map(Object::getClass)
                .collect(Collectors.toList());
            assertEquals(expectedClasses, actualClasses);
        }
    }

    @Test
    public void testActionStartTwice() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ActionStart-twice.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ActionStartEntryParser.createParser(0, INNER_ENTRY_PARSER_FACTORIES))))) {
            assertEquals(ActionStartLogEntry.class, logReader.readNextEntry().getClass());
            try {
                logReader.readNextEntry();
                fail();
            } catch (NotReadableException ex) {
                //ok - ACTION_END is not being parsed
            }
            assertEquals(ActionStartLogEntry.class, logReader.readNextEntry().getClass());
        }
    }

    @Test(expected = NotReadableException.class)
    public void testActionStartWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ActionStart.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ActionStartEntryParser.createParser(4, INNER_ENTRY_PARSER_FACTORIES))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}