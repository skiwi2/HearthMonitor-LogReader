package com.github.skiwi2.hearthmonitor.logreader.hearthstone.zone;

import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
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

public class TransitioningEntryParserTest {
    @Test
    public void testTransitioning() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("Transitioning.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new TransitioningEntryParser())))) {
            TransitioningLogEntry transitioningLogEntry = (TransitioningLogEntry)logReader.readNextEntry();

            assertEquals("Gul'dan", transitioningLogEntry.getName());
            assertEquals("4", transitioningLogEntry.getId());
            assertEquals("PLAY", transitioningLogEntry.getZone());
            assertEquals("0", transitioningLogEntry.getZonePos());
            assertEquals("HERO_07", transitioningLogEntry.getCardId());
            assertEquals("1", transitioningLogEntry.getPlayer());
            assertEquals("FRIENDLY PLAY (Hero)", transitioningLogEntry.getTargetZone());
        }
    }
}