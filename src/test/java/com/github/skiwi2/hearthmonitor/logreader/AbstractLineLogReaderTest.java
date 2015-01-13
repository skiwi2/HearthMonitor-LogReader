package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class AbstractLineLogReaderTest {
    @Test
    public void testReadEntryExpectedEntries() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ABCListLineLogReader("A", "B");

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test(expected = NoMoreInputException.class)
    public void testReadEntryNoMoreEntries() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ABCListLineLogReader("A", "B");

        logReader.readEntry();
        logReader.readEntry();
        logReader.readEntry();
    }

    @Test
    public void testReadEntryNoReadersAvailable() throws NoMoreInputException {
        LogReader logReader = new EmptyListLineLogReader("A", "B");

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
        LogReader logReader = new ABCListLineLogReader("A", "C", "1","2", "3", "B");

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test
    public void testReadLogEntrySpanningMultipleLinesLastEntryNotReadable() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ABCListLineLogReader("A", "B", "C", "1","2");

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("C", "1", "2"), ex.getLines());
        }
    }

    @Test
    public void testReadLogEntrySpanningMultipleLinesIncorrectInput() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ABCListLineLogReader("A", "C", "1","2", "4", "B");

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            assertEquals(Arrays.asList("C", "1", "2", "4"), ex.getLines());
        }

        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
    }

    @Test
    public void testReadLogEntryWithPeekingEntryReaders() throws NoMoreInputException, NotReadableException {
        LogReader logReader = new ABCPeekListLineLogReader("A", "A", "A", "B", "1", "B", "1", "C", "C", "B", "1", "A", "C");

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
        LogReader logReader = new InfiniteReadPeekListLineLogReader("START", "1", "2", "3", "START", "START", "1", "2", "3", "4", "5");

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

    private static class ABCListLineLogReader extends AbstractListLogReader {
        private ABCListLineLogReader(final String... inputList) {
            this(Arrays.asList(inputList));
        }

        private ABCListLineLogReader(final List<String> inputList) {
            super(inputList);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
            return new HashSet<>(Arrays.asList(
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("A");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.startsWith("A")) {
                            throw new NotParsableException();
                        }
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
                        if (!Objects.equals("1", lineReader.readLine())) {
                            throw new NotParsableException();
                        }
                        if (!Objects.equals("2", lineReader.readLine())) {
                            throw new NotParsableException();
                        }
                        if (!Objects.equals("3", lineReader.readLine())) {
                            throw new NotParsableException();
                        }
                        return new CLogEntry();
                    }
                }
            ));
        }
    }

    private static class EmptyListLineLogReader extends AbstractListLogReader {
        private EmptyListLineLogReader(final String... inputList) {
            this(Arrays.asList(inputList));
        }

        private EmptyListLineLogReader(final List<String> inputList) {
            super(inputList);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
            return new HashSet<>();
        }
    }

    private static class ABCPeekListLineLogReader extends AbstractListLogReader {
        private ABCPeekListLineLogReader(final String... inputList) {
            this(Arrays.asList(inputList));
        }

        private ABCPeekListLineLogReader(final List<String> inputList) {
            super(inputList);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
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

    private static class InfiniteReadPeekListLineLogReader extends AbstractListLogReader {
        private InfiniteReadPeekListLineLogReader(final String... inputList) {
            this(Arrays.asList(inputList));
        }

        private InfiniteReadPeekListLineLogReader(final List<String> inputList) {
            super(inputList);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
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

    private static class ALogEntry implements LogEntry { }

    private static class BLogEntry implements LogEntry { }

    private static class CLogEntry implements LogEntry { }

    private static class InfiniteLogEntry implements LogEntry {
        private final List<String> content = new ArrayList<>();

        private InfiniteLogEntry(final List<String> content) {
            Objects.requireNonNull(content, "content");
            this.content.addAll(content);
        }

        private List<String> getContent() {
            return new ArrayList<>(content);
        }
    }
}
