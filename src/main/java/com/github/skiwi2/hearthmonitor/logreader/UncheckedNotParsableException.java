package com.github.skiwi2.hearthmonitor.logreader;

/**
 * Unchecked exception to indicate that a log entry is not parsable.
 *
 * @author Frank van Heeswijk
 */
public class UncheckedNotParsableException extends RuntimeException {
    private static final long serialVersionUID = 3147294996191143729L;

    public UncheckedNotParsableException() {

    }

    public UncheckedNotParsableException(final String message) {
        super(message);
    }

    public UncheckedNotParsableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UncheckedNotParsableException(final Throwable cause) {
        super(cause);
    }
}
