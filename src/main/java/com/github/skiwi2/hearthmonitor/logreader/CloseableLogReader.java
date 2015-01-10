package com.github.skiwi2.hearthmonitor.logreader;

/**
 * Used to read log entries from a closeable resource.
 *
 * @author Frank van Heeswijk
 */
public interface CloseableLogReader extends LogReader, AutoCloseable { }
