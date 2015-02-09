package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.PlayerLogEntry;
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

public class PlayerEntryParserTest {
    @Test
    public void testPlayer() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("Player.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(PlayerEntryParser.createForIndentation(4))))) {
            PlayerLogEntry playerLogEntry = (PlayerLogEntry)logReader.readNextEntry();

            assertEquals(4, playerLogEntry.getIndentation());
            assertEquals("2", playerLogEntry.getEntityId());
            assertEquals("1", playerLogEntry.getPlayerId());
            assertEquals("144115198130930503", playerLogEntry.getGameAccountId().getHi());
            assertEquals("27162067", playerLogEntry.getGameAccountId().getLo());

            assertEquals("75", playerLogEntry.getTagValue("TIMEOUT"));
            assertEquals("PLAYING", playerLogEntry.getTagValue("PLAYSTATE"));
            assertEquals("1", playerLogEntry.getTagValue("CURRENT_PLAYER"));
            assertEquals("1", playerLogEntry.getTagValue("FIRST_PLAYER"));
            assertEquals("4", playerLogEntry.getTagValue("HERO_ENTITY"));
            assertEquals("10", playerLogEntry.getTagValue("MAXHANDSIZE"));
            assertEquals("4", playerLogEntry.getTagValue("STARTHANDSIZE"));
            assertEquals("1", playerLogEntry.getTagValue("PLAYER_ID"));
            assertEquals("1", playerLogEntry.getTagValue("TEAM_ID"));
            assertEquals("PLAY", playerLogEntry.getTagValue("ZONE"));
            assertEquals("1", playerLogEntry.getTagValue("CONTROLLER"));
            assertEquals("2", playerLogEntry.getTagValue("ENTITY_ID"));
            assertEquals("10", playerLogEntry.getTagValue("MAXRESOURCES"));
            assertEquals("PLAYER", playerLogEntry.getTagValue("CARDTYPE"));
            assertEquals("1", playerLogEntry.getTagValue("NUM_TURNS_LEFT"));
        }
    }

    @Test(expected = NotReadableException.class)
    public void testPlayerWrongIndentationLevel() throws Exception{
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("Player.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(PlayerEntryParser.createForIndentation(0))))) {
            assertNotNull(logReader);
            logReader.readNextEntry();
        }
    }
}