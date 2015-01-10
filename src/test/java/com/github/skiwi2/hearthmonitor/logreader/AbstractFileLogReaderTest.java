package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AbstractFileLogReaderTest {
    @Test
    public void testReadEntry() throws Exception {
        try (CloseableLogReader logReader = new ABCFileLogReader(Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8))) {
            assertEquals(ALogEntry.class, logReader.readEntry().getClass());
            assertEquals(BLogEntry.class, logReader.readEntry().getClass());
            assertEquals(CLogEntry.class, logReader.readEntry().getClass());
        }
    }

    @Test(expected = NoMoreInputException.class)
    public void testReadEntryNoMoreInput() throws Exception {
        try (CloseableLogReader logReader = new ABCFileLogReader(Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8))) {
            logReader.readEntry();
            logReader.readEntry();
            logReader.readEntry();
            logReader.readEntry();
        }
    }

    private static class ABCFileLogReader extends AbstractFileLogReader {
        private ABCFileLogReader(final BufferedReader bufferedReader) {
            super(bufferedReader);
        }

        @Override
        protected Set<EntryReader> entryReaders() {
            return new HashSet<>(Arrays.asList(
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("A");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.startsWith("A")) {
                            throw new NotParsableException();
                        }
                        return new ALogEntry();
                    }
                },
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("B");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.startsWith("B")) {
                            throw new NotParsableException();
                        }
                        return new BLogEntry();
                    }
                },
                new EntryReader() {
                    @Override
                    public boolean isParsable(String input) {
                        return input.equals("C");
                    }

                    @Override
                    public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                        if (!input.startsWith("C")) {
                            throw new NotParsableException();
                        }
                        return new CLogEntry();
                    }
                }
            ));
        }
    }

    private static class ALogEntry implements LogEntry { }

    private static class BLogEntry implements LogEntry { }

    private static class CLogEntry implements LogEntry { }
}