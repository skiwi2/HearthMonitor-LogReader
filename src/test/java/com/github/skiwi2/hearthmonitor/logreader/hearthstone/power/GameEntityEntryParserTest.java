package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.GameEntityLogEntry;
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

public class GameEntityEntryParserTest {
    @Test
    public void testGameEntity() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("GameEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(GameEntityEntryParser.createParser(4))))) {
            GameEntityLogEntry gameEntityLogEntry = (GameEntityLogEntry)logReader.readNextEntry();

            assertEquals(4, gameEntityLogEntry.getIndentation());
            assertEquals("1", gameEntityLogEntry.getEntityId());

            assertEquals("85", gameEntityLogEntry.getTagValue("10"));
            assertEquals("1", gameEntityLogEntry.getTagValue("TURN"));
            assertEquals("PLAY", gameEntityLogEntry.getTagValue("ZONE"));
            assertEquals("1", gameEntityLogEntry.getTagValue("ENTITY_ID"));
            assertEquals("BEGIN_MULLIGAN", gameEntityLogEntry.getTagValue("NEXT_STEP"));
            assertEquals("GAME", gameEntityLogEntry.getTagValue("CARDTYPE"));
            assertEquals("RUNNING", gameEntityLogEntry.getTagValue("STATE"));
        }
    }

    @Test(expected = NotReadableException.class)
    public void testGameEntityWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("GameEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(GameEntityEntryParser.createParser(0))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}