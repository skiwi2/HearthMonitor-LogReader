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
    public void testGetNumberOfSpacesNoIndentation() throws NotParsableException {
        assertEquals(0, LogLineUtils.getNumberOfSpaces("[Power] GameState.DebugPrintPower() - CREATE_GAME"));
    }

    @Test
    public void testGetNumberOfSpacesWithIndentation() throws NotParsableException {
        assertEquals(4, LogLineUtils.getNumberOfSpaces("[Power] GameState.DebugPrintPower() -     GameEntity EntityID=1"));
    }

    @Test(expected = NotParsableException.class)
    public void testGetNumberOfSpacesIncorrectLogLine() throws NotParsableException {
        LogLineUtils.getNumberOfSpaces("Initialize engine version: 4.5.5p3 (b8dc95101aa8)");
    }
}