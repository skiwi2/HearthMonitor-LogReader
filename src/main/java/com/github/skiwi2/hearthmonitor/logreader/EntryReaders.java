package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Functional interface to supply a set of entry readers.
 *
 * @author Frank van Heeswijk
 */
@FunctionalInterface
public interface EntryReaders extends Supplier<Set<EntryReader>> { }
