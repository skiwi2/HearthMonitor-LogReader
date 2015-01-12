package com.github.skiwi2.hearthmonitor.logreader.hearthstone;

import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for methods that help in parsing the logs.
 *
 * @author Frank van Heeswijk
 */
public final class LogLineUtils {
    private LogLineUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with a [
     *   - followed by one or more word characters
     *   - followed by a ]
     *   - followed by zero or more characters
     */
    private static final Pattern FROM_NAMED_LOGGER_PATTERN = Pattern.compile("^\\[\\w+\\].*");

    /**
     * Returns whether this log line comes from a named logger.
     *
     * Log lines from named loggers are of the form:
     *   [LoggerName] ...
     *
     * @param line  The log line to check
     * @return  Whether this log line comes from a named logger.
     */
    public static boolean isFromNamedLogger(final String line) {
        return FROM_NAMED_LOGGER_PATTERN.matcher(line).matches();
    }

    /**
     * Pattern that checks if a string matches the following:
     *   - starts with a [
     *   - followed by one or more word characters
     *   - followed by a ]
     *   - followed by a space
     *   - followed by one or more of:
     *     - word character
     *     - a dot (.)
     *     - parentheses
     *   - followed by a space
     *   - followed by a dash (-)
     *   - followed by a space
     *   - ending with zero or more characters, that are also captured as the 1st capture group
     */
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile("^\\[\\w+\\] [\\w.\\(\\)]+ - (.*)$");

    /**
     * Returns the number of spaces that are in a log line starting at the content.
     *
     * Considered log lines are of the form:
     *   [LoggerName] Class.MethodCall() - ...
     *
     * The number of spaces at the start of the ... section are counted.
     *
     * @param line  The log line to check
     * @return  The number of spaces that are in the log line starting at the content.
     * @throws NotParsableException If the log line is not in the [LoggerName] Class.MethodCall() - ... form.
     */
    public static int getNumberOfSpaces(final String line) throws NotParsableException {
        Matcher matcher = LOG_LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new NotParsableException("does not match a log line pattern");
        }
        String content = matcher.group(1);
        int spaces = 0;
        for (int i = 0; i < content.length(); i++) {
            char character = content.charAt(i);
            if (character == ' ') {
                spaces++;
            }
            else {
                break;
            }
        }
        return spaces;
    }
}
