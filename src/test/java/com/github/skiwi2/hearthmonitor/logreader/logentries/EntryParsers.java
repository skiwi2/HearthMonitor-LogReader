package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Frank van Heeswijk
 */
public final class EntryParsers {
    private EntryParsers() {
        throw new UnsupportedOperationException();
    }

    private static final Set<EntryParser> ABC_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
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
                    return input.equals("C");
                }

                @Override
                public LogEntry parse(String input, LineReader lineReader) throws NotParsableException {
                    if (!input.startsWith("C")) {
                        throw new NotParsableException();
                    }
                    return new CLogEntry();
                }
            }
        ));

    public static Set<EntryParser> getABCEntryParsers() {
        return new HashSet<>(ABC_ENTRY_PARSERS);
    }

    private static final Set<EntryParser> ABD_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
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

    public static Set<EntryParser> getABDEntryParsers() {
        return new HashSet<>(ABD_ENTRY_PARSERS);
    }

    public static Set<EntryParser> getEmptyEntryParsers() {
        return new HashSet<>();
    }

    private static final Set<ExtendedEntryParser> AB_EXTENDED_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
            new AExtendedEntryParser(),
            new BExtendedEntryParser()
        ));

    public static Set<ExtendedEntryParser> getABExtendedEntryParsers() {
        return new HashSet<>(AB_EXTENDED_ENTRY_PARSERS);
    }
}
