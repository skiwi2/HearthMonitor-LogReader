package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Frank van Heeswijk
 */
public class ABDEntryParsers implements EntryParsers {
    @Override
    public Set<EntryParser> get() {
        return new HashSet<>(Arrays.asList(
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("A");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.startsWith("A")) {
                        throw new NotParsableException();
                    }
                    return new ALogEntry();
                }
            },
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("B");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.startsWith("B")) {
                        throw new NotParsableException();
                    }
                    return new BLogEntry();
                }
            },
            new EntryParser() {
                @Override
                public boolean isParsable(String input) {
                    return input.equals("D");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.startsWith("D")) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("1", lineReader.readNextLine())) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("2", lineReader.readNextLine())) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("3", lineReader.readNextLine())) {
                        throw new NotParsableException();
                    }
                    return new DLogEntry();
                }
            }
        ));
    }
}
