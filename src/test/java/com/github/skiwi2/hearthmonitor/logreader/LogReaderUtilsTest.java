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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class LogReaderUtilsTest {
    @Test
    public void testFromInputAndExtraLineReader() throws NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C", "random1", "random2");
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(BLogEntry.class, logReader.readNextEntry().getClass());

        assertTrue(logReader.hasNextEntry());
        assertEquals(CLogEntry.class, logReader.readNextEntry().getClass());

        try {
            assertFalse(logReader.hasNextEntry());
            logReader.readNextEntry();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderFalseExtraReadCondition() throws NotReadableException {
        LineReader lineReader = new ListLineReader("A", "B", "C");
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> false, new ABCEntryParsers());

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        try {
            assertFalse(logReader.hasNextEntry());
            logReader.readNextEntry();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderNoExtraInput() throws NotReadableException {
        LineReader lineReader = new ListLineReader();
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("A", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        assertTrue(logReader.hasNextEntry());
        assertEquals(ALogEntry.class, logReader.readNextEntry().getClass());

        try {
            assertFalse(logReader.hasNextEntry());
            logReader.readNextEntry();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderInputNotReadable() throws NotReadableException {
        LineReader lineReader = new ListLineReader();
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("", lineReader, line -> line.length() == 1, new ABCEntryParsers());

        try {
            assertTrue(logReader.hasNextEntry());
            logReader.readNextEntry();
            fail();
        } catch (NotReadableException ex) {
            //ok
        }

        try {
            assertFalse(logReader.hasNextEntry());
            logReader.readNextEntry();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFromInputAndExtraLineReaderPECS() {
        LineReader lineReader = new ListLineReader();
        Predicate<Object> predicate = obj -> true;
        LogReader logReader = LogReaderUtils.fromInputAndExtraLineReader("", lineReader, predicate, new ABCEntryParsers());

        assertNotNull(logReader);
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
        public boolean nextLineMatches(final Predicate<? super String> condition) {
            if (!listIterator.hasNext()) {
                return false;
            }
            String next = listIterator.next();
            listIterator.previous();
            return condition.test(next);
        }
    }
}