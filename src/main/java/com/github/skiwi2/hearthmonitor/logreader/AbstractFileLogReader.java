package com.github.skiwi2.hearthmonitor.logreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Can be extended to read log entries from a log file.
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractFileLogReader extends AbstractLineLogReader implements CloseableLogReader {
    private final BufferedReader bufferedReader;

    /**
     * Initializes an AbstractFileLogReader instance.
     *
     * @param bufferedReader    The buffered reader from which to read
     * @throws  java.lang.NullPointerException  If bufferedReader is null.
     */
    protected AbstractFileLogReader(final BufferedReader bufferedReader) {
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    @Override
    protected String readLineFromLog() throws NoMoreInputException {
        try {
            String line = bufferedReader.readLine();
            if (line == null) {
                throw new NoMoreInputException();
            }
            return line;
        } catch (IOException ex) {
            throw new NoMoreInputException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
