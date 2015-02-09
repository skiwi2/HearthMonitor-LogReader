package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.HideEntityLogEntry;
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

public class HideEntityEntryParserTest {
    @Test
    public void testTagChange() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("HideEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(HideEntityEntryParser.createForIndentation(0))))) {
            HideEntityLogEntry hideEntityLogEntry = (HideEntityLogEntry)logReader.readNextEntry();
            CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)hideEntityLogEntry.getEntity();

            assertEquals(0, hideEntityLogEntry.getIndentation());
            assertEquals("Dread Infernal", cardEntityLogObject.getName());
            assertEquals("34", cardEntityLogObject.getId());
            assertEquals("HAND", cardEntityLogObject.getZone());
            assertEquals("3", cardEntityLogObject.getZonePos());
            assertEquals("CS2_064", cardEntityLogObject.getCardId());
            assertEquals("1", cardEntityLogObject.getPlayer());
            assertEquals("ZONE", hideEntityLogEntry.getTag());
            assertEquals("DECK", hideEntityLogEntry.getValue());
        }
    }

    @Test
    public void testTagChangeIndented() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("HideEntity-indented.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(HideEntityEntryParser.createForIndentation(4))))) {
            HideEntityLogEntry hideEntityLogEntry = (HideEntityLogEntry)logReader.readNextEntry();
            CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)hideEntityLogEntry.getEntity();

            assertEquals(4, hideEntityLogEntry.getIndentation());
            assertEquals("Dread Infernal", cardEntityLogObject.getName());
            assertEquals("34", cardEntityLogObject.getId());
            assertEquals("HAND", cardEntityLogObject.getZone());
            assertEquals("3", cardEntityLogObject.getZonePos());
            assertEquals("CS2_064", cardEntityLogObject.getCardId());
            assertEquals("1", cardEntityLogObject.getPlayer());
            assertEquals("ZONE", hideEntityLogEntry.getTag());
            assertEquals("DECK", hideEntityLogEntry.getValue());
        }
    }

    @Test(expected = NotReadableException.class)
    public void testTagChangeWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("HideEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(HideEntityEntryParser.createForIndentation(4))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}