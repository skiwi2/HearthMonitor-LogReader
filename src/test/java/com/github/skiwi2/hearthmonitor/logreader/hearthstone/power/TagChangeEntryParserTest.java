package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TagChangeEntryParserTest {
    @Test
    public void testTagChange() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("TagChange.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(TagChangeEntryParser.createParser(0))))) {
            TagChangeLogEntry tagChangeLogEntry = (TagChangeLogEntry)logReader.readNextEntry();

            assertEquals(0, tagChangeLogEntry.getIndentation());
            assertEquals("skiwi", ((PlayerEntityLogObject)tagChangeLogEntry.getEntity()).getName());
            assertEquals("TIMEOUT", tagChangeLogEntry.getTag());
            assertEquals("75", tagChangeLogEntry.getValue());
        }
    }

    @Test
    public void testTagChangeIndented() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("TagChange-indented.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(TagChangeEntryParser.createParser(4))))) {
            TagChangeLogEntry tagChangeLogEntry = (TagChangeLogEntry)logReader.readNextEntry();

            assertEquals(4, tagChangeLogEntry.getIndentation());
            assertEquals("skiwi", ((PlayerEntityLogObject)tagChangeLogEntry.getEntity()).getName());
            assertEquals("TIMEOUT", tagChangeLogEntry.getTag());
            assertEquals("75", tagChangeLogEntry.getValue());
        }
    }

    @Test(expected = NotReadableException.class)
    public void testTagChangeWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("TagChange.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(TagChangeEntryParser.createParser(4))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}