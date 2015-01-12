package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Used to read lines from an input source.
 *
 * @author Frank van Heeswijk
 */
public interface LineReader {
    /**
     * Reads the next line.
     *
     * @return  The next line.
     * @throws NoMoreInputException If no more input could be obtained.
     */
    String readLine() throws NoMoreInputException;

    /**
     * Peeks into the next line if more input is present, meaning that the line is not being consumed from the input source.
     *
     * @return  The next line, if present.
     */
    Optional<String> peekLine();

    /**
     * Returns a LineReader that reads from another LineReader while the read condition is true.
     *
     * @param lineReader    The LineReader to be read from
     * @param readCondition The read condition
     * @return  A LineReader that reads from another LineReader while the read condition is true.
     * @throws  java.lang.NullPointerException  If lineReader or readCondition is null.
     */
    static LineReader conditionalLineReader(final LineReader lineReader, final Predicate<String> readCondition) {
        Objects.requireNonNull(lineReader, "lineReader");
        Objects.requireNonNull(readCondition, "readCondition");
        return new LineReader() {
            private Optional<String> peekLineAfterRead = null;
            private int peeks = 0;

            @Override
            public String readLine() throws NoMoreInputException {
                //you want to peek in the line at position (read + 1), calling lineReader.peekLine() distorts this number
                Optional<String> peekLine = (peeks == 0) ? lineReader.peekLine() : peekLineAfterRead;
                if (peekLine.isPresent() && readCondition.test(peekLine.get())) {
                    peeks = 0;
                    return lineReader.readLine();
                }
                throw new NoMoreInputException();
            }

            @Override
            public Optional<String> peekLine() {
                Optional<String> peekLine = lineReader.peekLine();
                if (peeks == 0) {
                    peekLineAfterRead = peekLine;
                    peeks++;
                }
                if (peekLine.isPresent() && readCondition.test(peekLine.get())) {
                    return peekLine;
                }
                return Optional.empty();
            }
        };
    }
}
