package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.EntryParsers;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class FileLogReaderTest {
    @Test
    public void testConstructorPECS() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, EntryParsers.getABExtendedEntryParsers())) {
            assertNotNull(logReader);
        }
    }

    @Test
    public void testConstructorWithFilterPredicatePECS() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        Predicate<Object> predicate = obj -> true;
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, EntryParsers.getABExtendedEntryParsers(), predicate)) {
            assertNotNull(logReader);
        }
    }

    @Test
    public void testReadEntry() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, EntryParsers.getABCEntryParsers())) {

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
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, EntryParsers.getABCEntryParsers(), string -> !string.equals("0"))) {

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
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, EntryParsers.getABCEntryParsers())) {

            logReader.readNextEntry();
            logReader.readNextEntry();
            logReader.readNextEntry();
            logReader.readNextEntry();
        }
    }
}