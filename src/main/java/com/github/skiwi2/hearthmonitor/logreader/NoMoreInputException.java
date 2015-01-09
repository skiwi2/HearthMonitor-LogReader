package com.github.skiwi2.hearthmonitor.logreader;

/**
 * Exception to indicate that there is no more input.
 *
 * @author Frank van Heeswijk
 */
public class NoMoreInputException extends Exception {
    private static final long serialVersionUID = -4640787627068619913L;

    public NoMoreInputException() {

    }

    public NoMoreInputException(final String message) {
        super(message);
    }

    public NoMoreInputException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NoMoreInputException(final Throwable cause) {
        super(cause);
    }
}
