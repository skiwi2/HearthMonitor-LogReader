package com.github.skiwi2.hearthmonitor.logreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Used to read log entries from a log file, blocking until more input is available.
 *
 * @author Frank van Heeswijk
 */
public class MonitoringFileLogReader extends AbstractLineLogReader implements CloseableLogReader {
    private final BufferedReader bufferedReader;

    /**
     * Constructs a new MonitoringFileLogReader instance.
     *
     * @param bufferedReader    The buffered reader from which to read
     * @param entryReaders  The supplier of a set of entry readers
     * @throws  java.lang.NullPointerException  If bufferedReader or entryReaders.get() is null.
     */
    public MonitoringFileLogReader(final BufferedReader bufferedReader, final EntryReaders entryReaders) {
        super(entryReaders);
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    /**
     * Returns the next line from the log file.
     *
     * This method will block until input is available, or an IOException or InterruptedException has occurred.
     *
     * @return  The next line from the log file.
     * @throws NoMoreInputException If an underlying IOException or InterruptedException has been thrown.
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
