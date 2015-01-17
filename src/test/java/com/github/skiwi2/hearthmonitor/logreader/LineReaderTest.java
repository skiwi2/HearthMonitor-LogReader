package com.github.skiwi2.hearthmonitor.logreader;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class LineReaderTest {
    @Test
    public void testReadWhileConditionTrue() {
        LineReader originalLineReader = new ListLineReader(Arrays.asList("A", "B", "C", "random"));
        LineReader lineReader = LineReader.readWhile(originalLineReader, line -> true);

        Predicate<String> condition = line -> line.length() == 1;

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("A", lineReader.readNextLine());

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("B", lineReader.readNextLine());

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("C", lineReader.readNextLine());

        assertTrue(lineReader.hasNextLine());
        assertFalse(lineReader.nextLineMatches(condition));
        assertEquals("random", lineReader.readNextLine());

        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testReadWhileCondition() {
        LineReader originalLineReader = new ListLineReader(Arrays.asList("A", "B", "C", "random"));
        LineReader lineReader = LineReader.readWhile(originalLineReader, line -> line.length() == 1);

        Predicate<String> condition = line -> line.length() == 1;

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("A", lineReader.readNextLine());

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("B", lineReader.readNextLine());

        assertTrue(lineReader.hasNextLine());
        assertTrue(lineReader.nextLineMatches(condition));
        assertEquals("C", lineReader.readNextLine());

        //no "random"
        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }

        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testReadWhileConditionFalse() {
        LineReader originalLineReader = new ListLineReader(Arrays.asList("A", "B", "C", "random"));
        LineReader lineReader = LineReader.readWhile(originalLineReader, line -> false);

        Predicate<String> condition = line -> line.length() == 1;

        //no "A"
        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }

        //no "B"
        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }

        //no "C"
        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }

        //no "random"
        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }

        try {
            assertFalse(lineReader.hasNextLine());
            assertFalse(lineReader.nextLineMatches(condition));
            lineReader.readNextLine();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    private static class ListLineReader implements LineReader {
        private final ListIterator<String> listIterator;

        private ListLineReader(final List<String> list) {
            this.listIterator = list.listIterator();
        }

        @Override
        public String readNextLine() throws NoSuchElementException {
            return listIterator.next();
        }

        @Override
        public boolean hasNextLine() {
            return listIterator.hasNext();
        }

        @Override
        public boolean nextLineMatches(final Predicate<String> condition) {
            String line = listIterator.next();
            listIterator.previous();
            return condition.test(line);
        }
    }
}