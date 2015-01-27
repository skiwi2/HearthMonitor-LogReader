package com.github.skiwi2.hearthmonitor.logreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exception to indicate that a log entry is not readable.
 *
 * @author Frank van Heeswijk
 */
public class NotReadableException extends Exception {
    private static final long serialVersionUID = -117259271357929934L;

    private final List<String> lines = new ArrayList<>();
    private final List<Exception> occurredExceptions = new ArrayList<>();

    /**
     * Constructs a new NotReadableException instance.
     *
     * @param lines The lines that were not readable
     * @param occurredExceptions    The exceptions that occurred during reading
     */
    public NotReadableException(final List<String> lines, final List<Exception> occurredExceptions) {
        Objects.requireNonNull(lines, "lines");
        Objects.requireNonNull(occurredExceptions, "occurredExceptions");
        this.lines.addAll(lines);
        this.occurredExceptions.addAll(occurredExceptions);
    }

    /**
     * Returns the lines that were not readable.
     *
     * @return  The lines that were not readable.
     */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    /**
     * Returns the exceptions that occurred during reading.
     *
     * @return  The exceptions that occurred during reading.
     */
    public List<Exception> getOccurredExceptions() {
        return new ArrayList<>(occurredExceptions);
    }
}
