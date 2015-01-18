package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class ListLogReaderTest {
    @Test
    public void testReadEntry() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), new ABCEntryParsers());

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
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), new ABCEntryParsers());

        logReader.readNextEntry();
        logReader.readNextEntry();
        logReader.readNextEntry();
        logReader.readNextEntry();
    }
}