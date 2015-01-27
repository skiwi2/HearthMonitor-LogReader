package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.FileLogReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class FullEntityEntryParserTest {
    @Test
    public void testFullEntity() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("FullEntity.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new FullEntityEntryParser())))) {
            FullEntityLogEntry fullEntityLogEntry = (FullEntityLogEntry)logReader.readNextEntry();

            assertEquals("4", fullEntityLogEntry.getId());
            assertEquals("HERO_07", fullEntityLogEntry.getCardId());

            assertEquals("30", fullEntityLogEntry.getTagValue("HEALTH"));
            assertEquals("PLAY", fullEntityLogEntry.getTagValue("ZONE"));
            assertEquals("1", fullEntityLogEntry.getTagValue("CONTROLLER"));
            assertEquals("4", fullEntityLogEntry.getTagValue("ENTITY_ID"));
            assertEquals("NEUTRAL", fullEntityLogEntry.getTagValue("FACTION"));
            assertEquals("HERO", fullEntityLogEntry.getTagValue("CARDTYPE"));
            assertEquals("FREE", fullEntityLogEntry.getTagValue("RARITY"));
        }
    }
}