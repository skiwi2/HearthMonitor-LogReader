package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class LogReaderUtilsTest {
    @Test
    public void testFromLineReader() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C", "random1", "random2");
        Set<EntryReader> entryReaders = new HashSet<>(Arrays.asList(new ALogEntryReader(), new BLogEntryReader(), new CLogEntryReader()));
        LogReader logReader = LogReaderUtils.fromLineReader(lineReader, string -> string.length() == 1, entryReaders);

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
        assertEquals(BLogEntry.class, logReader.readEntry().getClass());
        assertEquals(CLogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testFromLineReaderFalseCondition() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C");
        Set<EntryReader> entryReaders = new HashSet<>(Arrays.asList(new ALogEntryReader(), new BLogEntryReader(), new CLogEntryReader()));
        LogReader logReader = LogReaderUtils.fromLineReader(lineReader, string -> false, entryReaders);

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testFromLineReaderNoInput() throws NotReadableException {
        LineReader lineReader = new ListLineReader();
        Set<EntryReader> entryReaders = new HashSet<>(Arrays.asList(new ALogEntryReader(), new BLogEntryReader(), new CLogEntryReader()));
        LogReader logReader = LogReaderUtils.fromLineReader(lineReader, string -> string.length() == 1, entryReaders);

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    private static class ListLineReader implements LineReader {
        private final Iterator<String> iterator;
        private final ListIterator<String> peekIterator;

        private int peeks = 0;

        private ListLineReader(final String... lines) {
            this(Arrays.asList(lines));
        }

        private ListLineReader(final List<String> lines) {
            Objects.requireNonNull(lines, "lines");
            List<String> tempLines = new ArrayList<>(lines);
            this.iterator = tempLines.iterator();
            this.peekIterator = tempLines.listIterator();
        }

        @Override
        public String readLine() throws NoMoreInputException {
            for (int i = 0; i < peeks; i++) {
                peekIterator.previous();
            }
            peeks = 0;
            if (!iterator.hasNext()) {
                throw new NoMoreInputException();
            }
            peekIterator.next();
            return iterator.next();
        }

        @Override
        public Optional<String> peekLine() {
            if (!peekIterator.hasNext()) {
                return Optional.empty();
            }
            peeks++;
            return Optional.of(peekIterator.next());
        }
    }

    private static class ALogEntryReader implements EntryReader {
        @Override
        public boolean isParsable(final String input) {
            return input.equals("A");
        }

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
            if (!input.equals("A")) {
                throw new NotParsableException();
            }
            return new ALogEntry();
        }
    }

    private static class BLogEntryReader implements EntryReader {
        @Override
        public boolean isParsable(final String input) {
            return input.equals("B");
        }

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
            if (!input.equals("B")) {
                throw new NotParsableException();
            }
            return new BLogEntry();
        }
    }

    private static class CLogEntryReader implements EntryReader {
        @Override
        public boolean isParsable(final String input) {
            return input.equals("C");
        }

        @Override
        public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException, NoMoreInputException {
            if (!input.equals("C")) {
                throw new NotParsableException();
            }
            return new CLogEntry();
        }
    }

    private static class ALogEntry implements LogEntry { }

    private static class BLogEntry implements LogEntry { }

    private static class CLogEntry implements LogEntry { }
}