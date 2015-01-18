package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.AbstractLogReader;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
        super(entryParsers, createReadIterator(bufferedReader));
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    /**
     * Returns an iterator for the given buffered reader.
     *
     * @param bufferedReader    The given buffered reader
     * @return  The iterator for the given buffered reader.
     */
    private static Iterator<String> createReadIterator(final BufferedReader bufferedReader) {
        Iterator<String> bufferedReaderIterator = bufferedReader.lines().iterator();
        return new Iterator<String>() {
            private boolean isInterrupted = false;

            @Override
            public boolean hasNext() {
                return !isInterrupted;
            }

            @Override
            public String next() {
                if (isInterrupted) {
                    throw new NoSuchElementException();
                }
                try {
                    while (!bufferedReaderIterator.hasNext()) {
                        Thread.sleep(100);
                    }
                    return bufferedReaderIterator.next();
                } catch (InterruptedException | UncheckedIOException ex) {
                    isInterrupted = true;
                    Thread.currentThread().isInterrupted();
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
