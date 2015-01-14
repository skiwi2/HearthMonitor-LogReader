package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logreader.EntryReader;
import com.github.skiwi2.hearthmonitor.logreader.EntryReaders;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Frank van Heeswijk
 */
public class EmptyEntryReaders implements EntryReaders {
    @Override
    public Set<EntryReader> get() {
        return new HashSet<>();
    }
}
