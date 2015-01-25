package com.github.skiwi2.hearthmonitor.logreader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class IteratorUtilsTest {
    @Test
    public void testFilteredIterator() {
        Iterator<String> iterator = Arrays.asList("0", "a", "0", "b", "0", "c", "0").iterator();
        Iterator<String> filteredIterator = IteratorUtils.filteredIterator(iterator, string -> !string.equals("0"));

        assertTrue(filteredIterator.hasNext());
        assertEquals("a", filteredIterator.next());

        assertTrue(filteredIterator.hasNext());
        assertEquals("b", filteredIterator.next());

        assertTrue(filteredIterator.hasNext());
        assertEquals("c", filteredIterator.next());

        assertFalse(filteredIterator.hasNext());
        try {
            filteredIterator.next();
            fail();
        } catch (NoSuchElementException ex) {
            //ok
        }
    }

    @Test
    public void testFilteredIteratorPECS() {
        Iterator<Integer> iterator = Arrays.asList(0, 1, 2).iterator();
        Predicate<Object> predicate = obj -> true;
        Iterator<Number> filteredIterator = IteratorUtils.filteredIterator(iterator, predicate);

        assertNotNull(filteredIterator);
    }
}