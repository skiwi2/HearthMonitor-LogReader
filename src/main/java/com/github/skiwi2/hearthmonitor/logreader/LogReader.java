package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

/**
 * Used to read log entries.
 *
 * @author Frank van Heeswijk
 */
public interface LogReader {
    /**
     * Returns the next log entry.
     *
     * If you get a NotReadableException, then you can recover the lines that could not be read by calling NotReadableException#lines.
     *
     * @return  The next log entry.
     * @throws NotReadableException If the log entry could not be read.
     * @throws NoMoreInputException If there is no more input.
     */
    LogEntry readEntry() throws NotReadableException, NoMoreInputException;
}