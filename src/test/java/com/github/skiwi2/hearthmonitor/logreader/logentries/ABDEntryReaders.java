package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NoMoreInputException;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Frank van Heeswijk
 */
public class ABDEntryReaders implements EntryReaders {
    @Override
    public Set<EntryReader> get() {
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
                    return input.equals("D");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException, NoMoreInputException {
                    if (!input.startsWith("D")) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("1", lineReader.readLine())) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("2", lineReader.readLine())) {
                        throw new NotParsableException();
                    }
                    if (!Objects.equals("3", lineReader.readLine())) {
                        throw new NotParsableException();
                    }
                    return new DLogEntry();
                }
            }
        ));
    }
}
