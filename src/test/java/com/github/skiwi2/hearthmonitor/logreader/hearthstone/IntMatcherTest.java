package com.github.skiwi2.hearthmonitor.logreader.hearthstone;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntMatcherTest {
    @Test
    public void testValueEquals() {
        IntMatcher intMatcher = IntMatcher.valueEquals(4);
        assertFalse(intMatcher.matches(3));
        assertTrue(intMatcher.matches(4));
        assertFalse(intMatcher.matches(5));
    }

    @Test
    public void testValueNotEquals() {
        IntMatcher intMatcher = IntMatcher.valueNotEquals(4);
        assertTrue(intMatcher.matches(3));
        assertFalse(intMatcher.matches(4));
        assertTrue(intMatcher.matches(5));
    }

    @Test
    public void testValueGreaterThan() {
        IntMatcher intMatcher = IntMatcher.valueGreaterThan(4);
        assertFalse(intMatcher.matches(3));
        assertFalse(intMatcher.matches(4));
        assertTrue(intMatcher.matches(5));
    }

    @Test
    public void testValueLessThan() {
        IntMatcher intMatcher = IntMatcher.valueLessThan(4);
        assertTrue(intMatcher.matches(3));
        assertFalse(intMatcher.matches(4));
        assertFalse(intMatcher.matches(5));
    }

    @Test
    public void testValueGreaterThanOrEqual() {
        IntMatcher intMatcher = IntMatcher.valueGreaterThanOrEqual(4);
        assertFalse(intMatcher.matches(3));
        assertTrue(intMatcher.matches(4));
        assertTrue(intMatcher.matches(5));
    }

    @Test
    public void testValueLessThanOrEqual() {
        IntMatcher intMatcher = IntMatcher.valueLessThanOrEqual(4);
        assertTrue(intMatcher.matches(3));
        assertTrue(intMatcher.matches(4));
        assertFalse(intMatcher.matches(5));
    }

    @Test
    public void testApplyFunction() {
        IntMatcher oldIntMatcher = IntMatcher.valueEquals(4);
        IntMatcher intMatcher = oldIntMatcher.applyFunction(i -> (i * 2) + 1);
        assertTrue(intMatcher.matches(9));
    }

    @Test
    public void testPlus() {
        IntMatcher oldIntMatcher = IntMatcher.valueEquals(4);
        IntMatcher intMatcher = oldIntMatcher.plus(4);
        assertTrue(intMatcher.matches(8));
    }

    @Test
    public void testMinus() {
        IntMatcher oldIntMatcher = IntMatcher.valueEquals(4);
        IntMatcher intMatcher = oldIntMatcher.minus(4);
        assertTrue(intMatcher.matches(0));
    }

    @Test
    public void testMultiply() {
        IntMatcher oldIntMatcher = IntMatcher.valueEquals(4);
        IntMatcher intMatcher = oldIntMatcher.multiply(2);
        assertTrue(intMatcher.matches(8));
    }

    @Test
    public void testDivide() {
        IntMatcher oldIntMatcher = IntMatcher.valueEquals(4);
        IntMatcher intMatcher = oldIntMatcher.divide(2);
        assertTrue(intMatcher.matches(2));
    }
}