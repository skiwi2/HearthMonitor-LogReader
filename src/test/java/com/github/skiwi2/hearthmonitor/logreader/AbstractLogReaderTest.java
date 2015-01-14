package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABDEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.DLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.EmptyEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.InfiniteLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logreaders.ListLogReader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class AbstractLogReaderTest {
    @Test
    public void testReadEntryExpectedEntries() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), new ABCEntryReaders());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test(expected = NoMoreInputException.class)
    public void testReadEntryNoMoreEntries() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), new ABCEntryReaders());

        logReader.readEntry();
        logReader.readEntry();
        logReader.readEntry();
    }

    @Test
    public void testReadEntryNoReadersAvailable() throws NoMoreInputException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B"), new EmptyEntryReaders());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(1, ex.getLines().size());
            assertEquals("A", ex.getLines().get(0));
        }
    }

    @Test
    public void testReadLogEntrySpanningMultipleLines() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "D", "1","2", "3", "B"), new ABDEntryReaders());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(DLogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test
    public void testReadLogEntrySpanningMultipleLinesLastEntryNotReadable() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "B", "D", "1","2"), new ABDEntryReaders());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("D", "1", "2"), ex.getLines());
        }
    }

    @Test
    public void testReadLogEntrySpanningMultipleLinesIncorrectInput() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "D", "1","2", "4", "B"), new ABDEntryReaders());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("D", "1", "2", "4"), ex.getLines());
        }

        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test
    public void testReadLogEntryWithPeekingEntryReaders() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("A", "A", "A", "B", "1", "B", "1", "C", "C", "B", "1", "A", "C"), new ABCPeekEntryReaders());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testReadLogEntryInfiniteLogEntries() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ListLogReader(Arrays.asList("START", "1", "2", "3", "START", "START", "1", "2", "3", "4", "5"), new InfiniteReadPeekEntryReaders());

        LogEntry logEntry1 = logReader.readEntry();
        assertEquals(InfiniteLogEntry.class, logEntry1.getClass());
        assertEquals(Arrays.asList("1", "2", "3"), ((InfiniteLogEntry)logEntry1).getContent());

        LogEntry logEntry2 = logReader.readEntry();
        assertEquals(InfiniteLogEntry.class, logEntry2.getClass());
        assertEquals(Arrays.<String>asList(), ((InfiniteLogEntry)logEntry2).getContent());

        LogEntry logEntry3 = logReader.readEntry();
        assertEquals(InfiniteLogEntry.class, logEntry3.getClass());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), ((InfiniteLogEntry)logEntry3).getContent());
    }

    private static class ABCPeekEntryReaders implements EntryReaders {
        @Override
        public Set<EntryReader> get() {
            return new HashSet<>(Arrays.asList(
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("A");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.equals("A")) {
                            throw new NotParsableException();
                        }
                        lineReader.peekLine();
                        return new ALogEntry();
                    }
                },
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("B");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.equals("B")) {
                            throw new NotParsableException();
                        }
                        if (!Objects.equals("1", lineReader.readLine())) {
                            throw new NotParsableException();
                        }
                        lineReader.peekLine();
                        return new BLogEntry();
                    }
                },
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("C");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.equals("C")) {
                            throw new NotParsableException();
                        }
                        lineReader.peekLine();
                        lineReader.peekLine();
                        return new CLogEntry();
                    }
                }
            ));
        }
    }

    private static class InfiniteReadPeekEntryReaders implements EntryReaders {
        @Override
        public Set<EntryReader> get() {
            return new HashSet<>(Arrays.asList(
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("START");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.equals("START")) {
                            throw new NotParsableException();
                        }
                        List<String> content = new ArrayList<>();
                        while (true) {
                            Optional<String> peekedLine = lineReader.peekLine();
                            if (!peekedLine.isPresent() || peekedLine.get().equals("START")) {
                                return new InfiniteLogEntry(content);
                            }
                            content.add(lineReader.readLine());
                        }
                    }
                }
            ));
        }
    }
}
