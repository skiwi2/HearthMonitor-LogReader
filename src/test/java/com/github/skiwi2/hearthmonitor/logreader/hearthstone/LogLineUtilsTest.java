package com.github.skiwi2.hearthmonitor.logreader.hearthstone;

import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogLineUtilsTest {
    @Test
    public void testIsFromNamedLoggerReturnsTrue() {
        assertTrue(LogLineUtils.isFromNamedLogger("[Power] GameState.DebugPrintPowerList() - Count=66"));
    }

    @Test
    public void testIsFromNamedLoggerReturnsFalse() {
        assertFalse(LogLineUtils.isFromNamedLogger("Initialize engine version: 4.5.5p3 (b8dc95101aa8)"));
    }

    @Test
    public void testGetContentFromLineFromNamedLogger() {
        assertEquals("Count=66", LogLineUtils.getContentFromLineFromNamedLogger("[Power] GameState.DebugPrintPowerList() - Count=66"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetContentFromLineFromNamedLoggerNotFromNamedLogger() {
        LogLineUtils.getContentFromLineFromNamedLogger("Initialize engine version: 4.5.5p3 (b8dc95101aa8)");
    }

    @Test
    public void testCountLeadingSpacesNoIndentation() throws NotParsableException {
        assertEquals(0, LogLineUtils.countLeadingSpaces("CREATE_GAME"));
    }

    @Test
    public void testCountLeadingSpacesWithIndentation() throws NotParsableException {
        assertEquals(4, LogLineUtils.countLeadingSpaces("    GameEntity EntityID=1"));
    }
}