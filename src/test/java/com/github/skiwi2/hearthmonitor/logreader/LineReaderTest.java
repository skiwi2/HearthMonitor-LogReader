package com.github.skiwi2.hearthmonitor.logreader;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

public class LineReaderTest {
    @Test
    public void testConditionalLineReaderTrueConditionReadLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> true);

        assertEquals("A", lineReader.readLine());
        assertEquals("B", lineReader.readLine());
        assertEquals("C", lineReader.readLine());
        assertEquals("random", lineReader.readLine());

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testConditionalLineReaderWithConditionReadLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> line.length() == 1);

        assertEquals("A", lineReader.readLine());
        assertEquals("B", lineReader.readLine());
        assertEquals("C", lineReader.readLine());

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testConditionalLineReaderWithFalseConditionReadLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> false);

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }

        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
    }

    @Test
    public void testConditionalLineReaderTrueConditionPeekLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> true);

        assertEquals(Optional.of("A"), lineReader.peekLine());
        assertEquals(Optional.of("B"), lineReader.peekLine());
        assertEquals(Optional.of("C"), lineReader.peekLine());
        assertEquals(Optional.of("random"), lineReader.peekLine());

        assertEquals(Optional.<String>empty(), lineReader.peekLine());
    }

    @Test
    public void testConditionalLineReaderWithConditionPeekLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> line.length() == 1);

        assertEquals(Optional.of("A"), lineReader.peekLine());
        assertEquals(Optional.of("B"), lineReader.peekLine());
        assertEquals(Optional.of("C"), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());

        assertEquals(Optional.<String>empty(), lineReader.peekLine());
    }

    @Test
    public void testConditionalLineReaderWithFalseConditionPeekLine() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> false);

        assertEquals(Optional.<String>empty(), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());
    }

    @Test
    public void testConditionalLineReaderWithConditionReadAndPeek() throws NoMoreInputException {
        LineReader originalLineReader = new ListLineReader("A", "B", "C", "random");
        LineReader lineReader = LineReader.conditionalLineReader(originalLineReader, line -> line.length() == 1);

        //next read "A"
        assertEquals("A", lineReader.readLine());
        assertEquals(Optional.of("B"), lineReader.peekLine());
        assertEquals(Optional.of("C"), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());

        //next read "B"
        assertEquals("B", lineReader.readLine());
        assertEquals(Optional.of("C"), lineReader.peekLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());

        //next read "C"
        assertEquals("C", lineReader.readLine());
        assertEquals(Optional.<String>empty(), lineReader.peekLine());

        //next read "random"
        try {
            lineReader.readLine();
            fail();
        } catch (NoMoreInputException ex) {
            //ok
        }
        assertEquals(Optional.<String>empty(), lineReader.peekLine());
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