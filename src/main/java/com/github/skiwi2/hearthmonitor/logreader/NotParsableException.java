package com.github.skiwi2.hearthmonitor.logreader;

/**
 * Exception to indicate that a log entry is not parsable.
 *
 * @author Frank van Heeswijk
 */
class NotParsableException extends Exception {
    private static final long serialVersionUID = 3147294996191143729L;

    NotParsableException() {

    }

    NotParsableException(final String message) {
        super(message);
    }

    NotParsableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    NotParsableException(final Throwable cause) {
        super(cause);
    }
}
