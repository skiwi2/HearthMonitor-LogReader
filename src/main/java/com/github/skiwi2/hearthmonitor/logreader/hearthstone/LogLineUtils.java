package com.github.skiwi2.hearthmonitor.logreader.hearthstone;

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
    private static final Pattern FROM_NAMED_LOGGER_PATTERN = Pattern.compile("^\\[\\w+\\] [\\w.\\(\\)]+ - (.*)$");

    /**
     * Returns whether this log line comes from a named logger.
     *
     * Log lines from named loggers are of the form:
     *   [LoggerName] Class.MethodCall() - ...
     *
     * @param line  The log line to check
     * @return  Whether this log line comes from a named logger.
     */
    public static boolean isFromNamedLogger(final String line) {
        return FROM_NAMED_LOGGER_PATTERN.matcher(line).matches();
    }

    /**
     * Returns the content part of a log line that comes from a named logger.
     *
     * Considered log lines are of the form:
     *   [LoggerName] Class.MethodCall() - ...
     *
     * The ... section is called the content.
     *
     * @param line  The log line to get the content from
     * @return  The content part of a log line that comes from a named logger.
     * @throws java.lang.IllegalArgumentException If the log line is not in the [LoggerName] Class.MethodCall() - ... form.
     */
    public static String getContentFromLineFromNamedLogger(final String line) {
        Matcher matcher = FROM_NAMED_LOGGER_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("does not match a log line pattern");
        }
        return matcher.group(1);
    }

    /**
     * Returns the number of leading spaces in a string.
     *
     * @param string  The string to check
     * @return  The number of leading spaces in the string.
     */
    public static int countLeadingSpaces(final String string) {
        int spaces = 0;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
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
