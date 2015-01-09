package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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

    private abstract static class ListLineLogReader extends AbstractLineLogReader {
        private final Iterator<String> iterator;

        private ListLineLogReader(final String... inputList) {
            this(Arrays.asList(inputList));
        }

        private ListLineLogReader(final List<String> inputList) {
            Objects.requireNonNull(inputList, "inputList");
            this.iterator = inputList.iterator();
        }

        @Override
        protected String readLineFromLog() throws NoMoreInputException {
            if (!iterator.hasNext()) {
                throw new NoMoreInputException();
            }
            return iterator.next();
        }
    }

    private static class ABCListLineLogReader extends ListLineLogReader {
        private ABCListLineLogReader(final String... inputList) {
            super(inputList);
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

    private static class EmptyListLineLogReader extends ListLineLogReader {
        private EmptyListLineLogReader(final String... inputList) {
            super(inputList);
        }

        private EmptyListLineLogReader(final List<String> inputList) {
            super(inputList);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
            return new HashSet<>();
        }
    }

    private static class ALogEntry implements LogEntry { }

    private static class BLogEntry implements LogEntry { }

    private static class CLogEntry implements LogEntry { }
}
