package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;

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
        private final ListIterator<String> listIterator;

        private ListLineReader(final String... lines) {
            this(Arrays.asList(lines));
        }

        private ListLineReader(final List<String> lines) {
            Objects.requireNonNull(lines, "lines");
            this.listIterator = new ArrayList<>(lines).listIterator();
        }

        @Override
        public String readNextLine() {
            return listIterator.next();
        }

        @Override
        public boolean hasNextLine() {
            return listIterator.hasNext();
        }

        @Override
        public boolean nextLineMatches(final Predicate<String> condition) {
            if (!listIterator.hasNext()) {
                return false;
            }
            String next = listIterator.next();
            listIterator.previous();
            return condition.test(next);
        }
    }
}