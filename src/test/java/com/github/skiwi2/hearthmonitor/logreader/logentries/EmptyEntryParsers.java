package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.EntryParsers;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Frank van Heeswijk
 */
public class EmptyEntryParsers implements EntryParsers {
    @Override
    public Set<EntryParser> get() {
        return new HashSet<>();
    }
}
