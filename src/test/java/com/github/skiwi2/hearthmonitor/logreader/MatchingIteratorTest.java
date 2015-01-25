package com.github.skiwi2.hearthmonitor.logreader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class MatchingIteratorTest {
    @Test
    public void testFromIterator() {
        Iterator<String> iterator = Arrays.asList("a", "b", "c").iterator();
        MatchingIterator<String> matchingIterator = MatchingIterator.fromIterator(iterator);

        assertTrue(matchingIterator.hasNext());
        assertFalse(matchingIterator.nextMatches(string -> !string.equals("a")));
        assertTrue(matchingIterator.nextMatches(string -> string.equals("a")));
        assertEquals("a", matchingIterator.next());

        assertTrue(matchingIterator.hasNext());
        assertFalse(matchingIterator.nextMatches(string -> !string.equals("b")));
        assertTrue(matchingIterator.nextMatches(string -> string.equals("b")));
        assertEquals("b", matchingIterator.next());

        assertTrue(matchingIterator.hasNext());
        assertFalse(matchingIterator.nextMatches(string -> !string.equals("c")));
        assertTrue(matchingIterator.nextMatches(string -> string.equals("c")));
        assertEquals("c", matchingIterator.next());

        assertFalse(matchingIterator.hasNext());
        assertFalse(matchingIterator.nextMatches(string -> true));
        assertFalse(matchingIterator.nextMatches(string -> false));

        try {
            matchingIterator.next();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFromIteratorPECS() {
        Iterator<Integer> iterator = Arrays.asList(1, 2, 3).iterator();
        MatchingIterator<Number> matchingIterator = MatchingIterator.fromIterator(iterator);

        assertTrue(matchingIterator.nextMatches(number -> number.intValue() == 1));
    }
}