package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

public class LogReaderUtilsTest {
    @Test
    public void testFromInputAndExtraLineReader() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C", "random1", "random2");
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());
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
    public void testFromInputAndExtraLineReaderFalseExtraReadCondition() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C");
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> false, new ABCEntryParsers());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderNoExtraInput() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader();
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        assertEquals(ALogEntry.class, logReader.readEntry().getClass());

        try {
            logReader.readEntry();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderInputNotReadable() throws NoMoreInputException, NotReadableException {
        LineReader lineReader = new ListLineReader();
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        try {
            logReader.readEntry();
            fail();
        } catch (NotReadableException ex) {
            //ok
        }

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
}