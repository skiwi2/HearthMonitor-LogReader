package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.DLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.EntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.logentries.InfiniteLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.ListLogReader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.*;

public class AbstractLogReaderTest {
    @Test
    public void testReadNextEntryExpectedEntries() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), EntryParsers.getABCEntryParsers());

        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
    }

    @Test(expected = NoSuchElementException.class)
    public void testReadNextEntryNoMoreEntries() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), EntryParsers.getABCEntryParsers());

        logReader.readNextEntry();
        logReader.readNextEntry();
        logReader.readNextEntry();
    }

    @Test
    public void testReadNextEntryNoReadersAvailable() {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), EntryParsers.getEmptyEntryParsers());

        try {
            logReader.readNextEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(1, ex.getLines().size());
            assertEquals("A", ex.getLines().get(0));
        }
    }

    @Test
    public void testHasNextEntryNoEntries() {
        LogReader logReader = new ListLogReader(Arrays.asList(), EntryParsers.getABCEntryParsers());

        assertFalse(logReader.hasNextEntry());
        assertFalse(logReader.hasNextEntry());
    }

    @Test
    public void testHasNextEntryOneEntry() {
        LogReader logReader = new ListLogReader(Arrays.asList("A"), EntryParsers.getABCEntryParsers());

        assertTrue(logReader.hasNextEntry());
        assertTrue(logReader.hasNextEntry());
    }

    @Test
    public void testHasNextEntryWithReads() throws NotReadableException {
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
    public void testReadNextEntrySpanningMultipleLines() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "D", "1","2", "3", "B"), EntryParsers.getABDEntryParsers());

        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(DLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
    }

    @Test
    public void testReadNextEntrySpanningMultipleLinesLastEntryNotReadable() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "D", "1","2"), EntryParsers.getABDEntryParsers());

        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());

        try {
            logReader.readNextEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("D", "1", "2"), ex.getLines());
        }
    }

    @Test
    public void testReadNextEntrySpanningMultipleLinesIncorrectInput() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "D", "1","2", "4", "B"), EntryParsers.getABDEntryParsers());

        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        try {
            logReader.readNextEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("D", "1", "2", "4"), ex.getLines());
        }

        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
    }

    @Test
    public void testReadNextEntryWithPeekingEntryReaders() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "A", "A", "B", "1", "B", "1", "C", "C", "B", "1", "A", "C"), getABCPeekEntryParsers());

        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

        try {
            logReader.readNextEntry();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testReadNextEntryInfiniteLogEntries() throws NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("START", "1", "2", "3", "START", "START", "1", "2", "3", "4", "5"), getInfiniteReadPeekEntryParsers());

        LogEntry logEntry1 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry1.getClass());
        assertEquals(Arrays.asList("1", "2", "3"), ((InfiniteLogEntry)logEntry1).getContent());

        LogEntry logEntry2 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry2.getClass());
        assertEquals(Arrays.<String>asList(), ((InfiniteLogEntry)logEntry2).getContent());

        LogEntry logEntry3 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry3.getClass());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), ((InfiniteLogEntry)logEntry3).getContent());
    }

    @Test
    public void testReadNextEntryInfiniteLogEntriesFilterLines() throws NotReadableException {
        LogReader logReader = new ListLogReader(
            Arrays.asList("0", "START", "0", "0", "1", "2", "0", "3", "START", "0", "START", "0", "1", "0", "2", "0", "3", "0", "4", "0", "5", "0"),
            getInfiniteReadPeekEntryParsers(),
            string -> !string.equals("0")
        );

        LogEntry logEntry1 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry1.getClass());
        assertEquals(Arrays.asList("1", "2", "3"), ((InfiniteLogEntry)logEntry1).getContent());

        LogEntry logEntry2 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry2.getClass());
        assertEquals(Arrays.<String>asList(), ((InfiniteLogEntry)logEntry2).getContent());

        LogEntry logEntry3 = logReader.readNextEntry();
        assertEquals(InfiniteLogEntry.class, logEntry3.getClass());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), ((InfiniteLogEntry)logEntry3).getContent());
    }

    private static final Set<EntryParser> ABC_PEEK_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("A");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.equals("A")) {
                        throw new NotParsableException();
                    }
                    lineReader.hasNextLine();
                    return new ALogEntry();
                }
            },
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("B");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.equals("B")) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("1", lineReader.readNextLine())) {
                        throw new NotParsableException();
                    }
                    lineReader.hasNextLine();
                    return new BLogEntry();
                }
            },
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("C");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.equals("C")) {
                        throw new NotParsableException();
                    }
                    lineReader.hasNextLine();
                    return new CLogEntry();
                }
            }
        ));

    private static Set<EntryParser> getABCPeekEntryParsers() {
        return new HashSet<>(ABC_PEEK_ENTRY_PARSERS);
    }

    private static final Set<EntryParser> INFINITE_READ_PEEK_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("START");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.equals("START")) {
                        throw new NotParsableException();
                    }
                    List<String> content = new ArrayList<>();
                    while (lineReader.nextLineMatches(line -> !line.equals("START"))) {
                        content.add(lineReader.readNextLine());
                    }
                    return new InfiniteLogEntry(content);
                }
            }
        ));

    private static Set<EntryParser> getInfiniteReadPeekEntryParsers() {
        return new HashSet<>(INFINITE_READ_PEEK_ENTRY_PARSERS);
    }
}
