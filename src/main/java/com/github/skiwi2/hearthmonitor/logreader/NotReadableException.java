package com.github.skiwi2.hearthmonitor.logreader;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception to indicate that a log entry is not readable.
 *
 * @author Frank van Heeswijk
 */
public class NotReadableException extends Exception  {
    private static final long serialVersionUID = -117259271357929934L;

    private final List<String> lines = new ArrayList<>();

    public NotReadableException(final List<String> lines) {
        this.lines.addAll(lines);
    }

    /**
     * Returns the lines that were not readable.
     *
     * @return  The lines that were not readable.
     */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
}
