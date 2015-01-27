package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
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

public class TagChangeEntryParserTest {
    @Test
    public void testTagChange() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("TagChange.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new HashSet<>(Arrays.asList(new TagChangeEntryParser())))) {
            TagChangeLogEntry tagChangeLogEntry = (TagChangeLogEntry)logReader.readNextEntry();

            assertEquals("skiwi", tagChangeLogEntry.getEntity());
            assertEquals("TIMEOUT", tagChangeLogEntry.getTag());
            assertEquals("75", tagChangeLogEntry.getValue());
        }
    }
}