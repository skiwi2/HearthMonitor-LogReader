package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.GameEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.CreateGameLogEntry.PlayerLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.CreateGameEntryParser.GameEntityEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.CreateGameEntryParser.PlayerEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CreateGameEntryParserTest {
    @Test
    public void testGameEntity() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("CreateGameGameEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new GameEntityEntryParser())))) {
            GameEntityLogEntry gameEntityLogEntry = (GameEntityLogEntry)logReader.readNextEntry();

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

    @Test
    public void testPlayer() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("CreateGamePlayer.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new PlayerEntryParser())))) {
            PlayerLogEntry playerLogEntry = (PlayerLogEntry)logReader.readNextEntry();

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

    @Test
    public void testCreateGame() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("CreateGame.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new CreateGameEntryParser())))) {
            CreateGameLogEntry createGameLogEntry = (CreateGameLogEntry)logReader.readNextEntry();

            GameEntityLogEntry gameEntityLogEntry = createGameLogEntry.getGameEntityLogEntry();

            assertEquals("1", gameEntityLogEntry.getEntityId());

            assertEquals("85", gameEntityLogEntry.getTagValue("10"));
            assertEquals("1", gameEntityLogEntry.getTagValue("TURN"));
            assertEquals("PLAY", gameEntityLogEntry.getTagValue("ZONE"));
            assertEquals("1", gameEntityLogEntry.getTagValue("ENTITY_ID"));
            assertEquals("BEGIN_MULLIGAN", gameEntityLogEntry.getTagValue("NEXT_STEP"));
            assertEquals("GAME", gameEntityLogEntry.getTagValue("CARDTYPE"));
            assertEquals("RUNNING", gameEntityLogEntry.getTagValue("STATE"));

            PlayerLogEntry playerLogEntry1 = getPlayerLogEntryByPlayerId("1", createGameLogEntry.getPlayerLogEntries());
            PlayerLogEntry playerLogEntry2 = getPlayerLogEntryByPlayerId("2", createGameLogEntry.getPlayerLogEntries());

            assertEquals("2", playerLogEntry1.getEntityId());
            assertEquals("1", playerLogEntry1.getPlayerId());
            assertEquals("144115198130930503", playerLogEntry1.getGameAccountId().getHi());
            assertEquals("27162067", playerLogEntry1.getGameAccountId().getLo());

            assertEquals("75", playerLogEntry1.getTagValue("TIMEOUT"));
            assertEquals("PLAYING", playerLogEntry1.getTagValue("PLAYSTATE"));
            assertEquals("1", playerLogEntry1.getTagValue("CURRENT_PLAYER"));
            assertEquals("1", playerLogEntry1.getTagValue("FIRST_PLAYER"));
            assertEquals("4", playerLogEntry1.getTagValue("HERO_ENTITY"));
            assertEquals("10", playerLogEntry1.getTagValue("MAXHANDSIZE"));
            assertEquals("4", playerLogEntry1.getTagValue("STARTHANDSIZE"));
            assertEquals("1", playerLogEntry1.getTagValue("PLAYER_ID"));
            assertEquals("1", playerLogEntry1.getTagValue("TEAM_ID"));
            assertEquals("PLAY", playerLogEntry1.getTagValue("ZONE"));
            assertEquals("1", playerLogEntry1.getTagValue("CONTROLLER"));
            assertEquals("2", playerLogEntry1.getTagValue("ENTITY_ID"));
            assertEquals("10", playerLogEntry1.getTagValue("MAXRESOURCES"));
            assertEquals("PLAYER", playerLogEntry1.getTagValue("CARDTYPE"));
            assertEquals("1", playerLogEntry1.getTagValue("NUM_TURNS_LEFT"));

            assertEquals("3", playerLogEntry2.getEntityId());
            assertEquals("2", playerLogEntry2.getPlayerId());
            assertEquals("144115198130930503", playerLogEntry2.getGameAccountId().getHi());
            assertEquals("37543301", playerLogEntry2.getGameAccountId().getLo());

            assertEquals("75", playerLogEntry2.getTagValue("TIMEOUT"));
            assertEquals("PLAYING", playerLogEntry2.getTagValue("PLAYSTATE"));
            assertEquals("36", playerLogEntry2.getTagValue("HERO_ENTITY"));
            assertEquals("10", playerLogEntry2.getTagValue("MAXHANDSIZE"));
            assertEquals("4", playerLogEntry2.getTagValue("STARTHANDSIZE"));
            assertEquals("2", playerLogEntry2.getTagValue("PLAYER_ID"));
            assertEquals("2", playerLogEntry2.getTagValue("TEAM_ID"));
            assertEquals("PLAY", playerLogEntry2.getTagValue("ZONE"));
            assertEquals("2", playerLogEntry2.getTagValue("CONTROLLER"));
            assertEquals("3", playerLogEntry2.getTagValue("ENTITY_ID"));
            assertEquals("10", playerLogEntry2.getTagValue("MAXRESOURCES"));
            assertEquals("PLAYER", playerLogEntry2.getTagValue("CARDTYPE"));
            assertEquals("1", playerLogEntry2.getTagValue("NUM_TURNS_LEFT"));
        }
    }

    @Test
    public void testCreateGameTwice() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("CreateGame-twice.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new CreateGameEntryParser())))) {
            assertEquals(CreateGameLogEntry.class, logReader.readNextEntry().getClass());
            assertEquals(CreateGameLogEntry.class, logReader.readNextEntry().getClass());
        }
    }

    private static PlayerLogEntry getPlayerLogEntryByPlayerId(final String playerId, final Set<PlayerLogEntry> playerLogEntries) {
        return playerLogEntries.stream()
            .filter(playerLogEntry -> playerLogEntry.getPlayerId().equals(playerId))
            .findFirst()
            .orElse(null);
    }
}