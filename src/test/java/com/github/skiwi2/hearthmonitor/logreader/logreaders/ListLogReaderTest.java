package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.LogReader;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;
import com.github.skiwi2.hearthmonitor.logreader.NotReadableException;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ListLogReaderTest {
    @Test
    public void testReadEntry() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), new ABCEntryReaders());
        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());
    }

    @Test(expected = NoMoreInputException.class)
    public void testReadEntryNoMoreInput() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "C"), new ABCEntryReaders());
        logReader.readEntry();
        logReader.readEntry();
        logReader.readEntry();
        logReader.readEntry();
    }
}