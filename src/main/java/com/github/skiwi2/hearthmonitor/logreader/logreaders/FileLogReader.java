package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logreader.AbstractLogReader;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

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
     * @param entryParsers  The supplier of a set of entry parsers
     * @throws  java.lang.NullPointerException  If bufferedReader or entryParsers.get() is null.
     */
    public FileLogReader(final BufferedReader bufferedReader, final EntryParsers entryParsers) {
        super(entryParsers, bufferedReader.lines().iterator());
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    /**
     * Constructs a new FileLogReader instance.
     *
     * The filter predicate can be used to filter the lines you want to traverse.
     *
     * @param bufferedReader    The buffered reader from which to read
     * @param entryParsers  The supplier of a set of entry parsers
     * @param filterPredicate   The predicate to filter the lines with
     * @throws  java.lang.NullPointerException  If bufferedReader, filterPredicate or entryParsers.get() is null.
     */
    public FileLogReader(final BufferedReader bufferedReader, final EntryParsers entryParsers, final Predicate<? super String> filterPredicate) {
        super(entryParsers, bufferedReader.lines().iterator(), filterPredicate);
        this.bufferedReader = Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
