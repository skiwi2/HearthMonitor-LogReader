package com.github.skiwi2.hearthmonitor.logreader.logentries;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Frank van Heeswijk
 */
public class InfiniteLogEntry implements LogEntry {
    private final List<String> content = new ArrayList<>();

    public InfiniteLogEntry(final List<String> content) {
        Objects.requireNonNull(content, "content");
        this.content.addAll(content);
    }

    public List<String> getContent() {
        return new ArrayList<>(content);
    }
}