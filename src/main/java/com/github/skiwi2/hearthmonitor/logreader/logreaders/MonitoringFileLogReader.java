package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.AbstractLogReader;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Used to read log entries from a log file, blocking until more input is available.
 *
 * @author Frank van Heeswijk
 */
public class MonitoringFileLogReader extends AbstractLogReader implements CloseableLogReader {
    private final BufferedReader bufferedReader;

    /**
     * Constructs a new MonitoringFileLogReader instance.
     *
     * @param bufferedReader    The buffered reader from which to read
     * @param entryParsers  The supplier of a set of entry parsers
     * @throws  java.lang.NullPointerException  If bufferedReader or entryParsers.get() is null.
     */
    public MonitoringFileLogReader(final BufferedReader bufferedReader, final EntryParsers entryParsers) {
        super(entryParsers);
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    /**
     * Returns the next line from the log file.
     *
     * This method will block until input is available, or an IOException or InterruptedException has occurred.
     *
     * @return  The next line from the log file.
     * @throws com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException If an underlying IOException or InterruptedException has been thrown.
     */
    @Override
    protected String readLineFromLog() throws NoMoreInputException {
        try {
            String line;
            while ((line = bufferedReader.readLine()) == null) {
                Thread.sleep(100);
            }
            return line;
        } catch (IOException | InterruptedException | RuntimeException ex) {
            throw new NoMoreInputException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
