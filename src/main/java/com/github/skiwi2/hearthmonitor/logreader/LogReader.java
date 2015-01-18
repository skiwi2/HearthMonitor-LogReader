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
     * The NotReadableException has more information available.
     * You can recover the lines that could not be read by calling NotReadableException#getLines.
     * You can see which exceptions were thrown internally by calling NotReadableException#getOccurredExceptions.
     *
     * @return  The next log entry.
     * @throws NotReadableException If the log entry could not be read.
     * @throws java.util.NoSuchElementException If there is no more input.
     */
    LogEntry readNextEntry() throws NotReadableException;

    /**
     * Returns whether there is a next log entry.
     *
     * @return  Whether there is a next log entry.
     */
    boolean hasNextEntry();
}