package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.AbstractLogReader;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Used to read log entries from a log file.
 *
 * @author Frank van Heeswijk
 */
public class FileLogReader extends AbstractLogReader implements CloseableLogReader {
    private final BufferedReader bufferedReader;

    /**
     * Constructs a new FileLogReader instance.
     *
     * @param bufferedReader    The buffered reader from which to read
     * @param entryReaders  The supplier of a set of entry readers
     * @throws  java.lang.NullPointerException  If bufferedReader or entryReaders.get() is null.
     */
    public FileLogReader(final BufferedReader bufferedReader, final EntryReaders entryReaders) {
        super(entryReaders);
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
