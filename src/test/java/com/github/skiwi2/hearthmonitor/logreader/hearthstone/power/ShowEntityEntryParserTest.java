package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
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

public class ShowEntityEntryParserTest {
    @Test
    public void testShowEntity() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ShowEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ShowEntityEntryParser.createParser(0))))) {
            ShowEntityLogEntry showEntityLogEntry = (ShowEntityLogEntry)logReader.readNextEntry();
            CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)showEntityLogEntry.getEntity();

            assertEquals(0, showEntityLogEntry.getIndentation());
            assertEquals("33", cardEntityLogObject.getId());
            assertEquals("", cardEntityLogObject.getCardId());
            assertEquals("INVALID", cardEntityLogObject.getType());
            assertEquals("DECK", cardEntityLogObject.getZone());
            assertEquals("0", cardEntityLogObject.getZonePos());
            assertEquals("1", cardEntityLogObject.getPlayer());
            assertEquals("CS2_062", showEntityLogEntry.getCardId());

            assertEquals("4", showEntityLogEntry.getTagValue("COST"));
            assertEquals("HAND", showEntityLogEntry.getTagValue("ZONE"));
            assertEquals("NEUTRAL", showEntityLogEntry.getTagValue("FACTION"));
            assertEquals("ABILITY", showEntityLogEntry.getTagValue("CARDTYPE"));
            assertEquals("FREE", showEntityLogEntry.getTagValue("RARITY"));
        }
    }

    @Test
    public void testShowEntityIndented() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ShowEntity-indented.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ShowEntityEntryParser.createParser(4))))) {
            ShowEntityLogEntry showEntityLogEntry = (ShowEntityLogEntry)logReader.readNextEntry();
            CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)showEntityLogEntry.getEntity();

            assertEquals(4, showEntityLogEntry.getIndentation());
            assertEquals("33", cardEntityLogObject.getId());
            assertEquals("", cardEntityLogObject.getCardId());
            assertEquals("INVALID", cardEntityLogObject.getType());
            assertEquals("DECK", cardEntityLogObject.getZone());
            assertEquals("0", cardEntityLogObject.getZonePos());
            assertEquals("1", cardEntityLogObject.getPlayer());
            assertEquals("CS2_062", showEntityLogEntry.getCardId());

            assertEquals("4", showEntityLogEntry.getTagValue("COST"));
            assertEquals("HAND", showEntityLogEntry.getTagValue("ZONE"));
            assertEquals("NEUTRAL", showEntityLogEntry.getTagValue("FACTION"));
            assertEquals("ABILITY", showEntityLogEntry.getTagValue("CARDTYPE"));
            assertEquals("FREE", showEntityLogEntry.getTagValue("RARITY"));
        }
    }

    @Test(expected = NotReadableException.class)
    public void testShowEntityWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("ShowEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(ShowEntityEntryParser.createParser(4))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}