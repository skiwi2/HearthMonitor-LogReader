package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.EntryParsers;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class ListLogReaderTest {
    @Test
    public void testConstructorPECS() {
        Predicate<Object> predicate = obj -> true;
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), EntryParsers.getABCEntryParsers(), predicate);

        assertNotNull(logReader);
    }

    @Test
    public void testReadEntry() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), EntryParsers.getABCEntryParsers());

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

        assertFalse(logReader.hasNextEntry());
    }

    @Test
    public void testReadEntryFilterLines() throws NotReadableException {
        LogReader logReader = new ListLogReader(
            Arrays.asList("0", "A", "0", "B", "0", "C", "0"),
            EntryParsers.getABCEntryParsers(),
            string -> !string.equals("0")
        );

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

        assertFalse(logReader.hasNextEntry());
    }

    @Test(expected = NoSuchElementException.class)
    public void testReadEntryNoMoreInput() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), EntryParsers.getABCEntryParsers());

        logReader.readNextEntry();
        logReader.readNextEntry();
        logReader.readNextEntry();
        logReader.readNextEntry();
    }
}