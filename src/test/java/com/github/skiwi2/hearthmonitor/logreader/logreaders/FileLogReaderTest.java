package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class FileLogReaderTest {
    @Test
    public void testReadEntry() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new ABCEntryReaders())) {
            assertEquals(ALogEntry.class, logReader.readEntry().getClass());
            assertEquals(BLogEntry.class, logReader.readEntry().getClass());
            assertEquals(CLogEntry.class, logReader.readEntry().getClass());
        }
    }

    @Test(expected = NoMoreInputException.class)
    public void testReadEntryNoMoreInput() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new FileLogReader(bufferedReader, new ABCEntryReaders())) {
            logReader.readEntry();
            logReader.readEntry();
            logReader.readEntry();
            logReader.readEntry();
        }
    }
}