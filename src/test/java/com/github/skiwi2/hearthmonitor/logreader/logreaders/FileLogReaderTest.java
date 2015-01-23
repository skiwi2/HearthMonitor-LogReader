package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class FileLogReaderTest {
    @Test
    public void testReadEntry() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new ABCEntryParsers())) {

            assertTrue(logReader.hasNextEntry());
            assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

            assertTrue(logReader.hasNextEntry());
            assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
            
            assertTrue(logReader.hasNextEntry());
            assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

            assertFalse(logReader.hasNextEntry());
        }
    }

    @Test
    public void testReadEntryFilterLines() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test-filter.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new ABCEntryParsers(), string -> !string.equals("0"))) {

            assertTrue(logReader.hasNextEntry());
            assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

            assertTrue(logReader.hasNextEntry());
            assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());

            assertTrue(logReader.hasNextEntry());
            assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

            assertFalse(logReader.hasNextEntry());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testReadEntryNoMoreInput() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new ABCEntryParsers())) {

            logReader.readNextEntry();
            logReader.readNextEntry();
            logReader.readNextEntry();
            logReader.readNextEntry();
        }
    }
}