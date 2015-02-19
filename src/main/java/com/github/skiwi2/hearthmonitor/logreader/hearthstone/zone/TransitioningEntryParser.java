package com.github.skiwi2.hearthmonitor.logreader.hearthstone.zone;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.LogLineUtils;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.EntityObjectParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse [Zone] TRANSITIONING entries.
 *
 * @author Frank van Heeswijk
 */
public class TransitioningEntryParser implements EntryParser {
    /*
     * [Zone] ZoneChangeList.ProcessChanges() - TRANSITIONING card [name=Gul'dan id=4 zone=PLAY zonePos=0 cardId=HERO_07 player=1] to FRIENDLY PLAY (Hero)
     */

    private final int indentation;

    private TransitioningEntryParser(final int indentation) {
        this.indentation = indentation;
    }


    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Zone] ZoneChangeList.ProcessChanges() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'TRANSITIONING card '
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' to '
     *  - ending with zero or more characters, captured as the 3rd group
     */
    private static final Pattern EXTRACT_TRANSITIONING_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Zone] ZoneChangeList.ProcessChanges() - ") + "(\\s*)" + Pattern.quote("TRANSITIONING card ") + "(.*)" + Pattern.quote(" to ") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return (EXTRACT_TRANSITIONING_PATTERN.matcher(input).matches() && isValidIndentation(input));
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        if (!isValidIndentation(input)) {
            throw new NotParsableException();
        }

        TransitioningLogEntry.Builder builder = new TransitioningLogEntry.Builder();

        Matcher transitioningMatcher = EXTRACT_TRANSITIONING_PATTERN.matcher(input);
        if (!transitioningMatcher.find()) {
            throw new NotParsableException();
        }
        int localIndentation = transitioningMatcher.group(1).length();
        String entity = transitioningMatcher.group(2);
        String targetZone = transitioningMatcher.group(3);

        EntityObjectParser entityObjectParser = new EntityObjectParser();
        if (!entityObjectParser.isParsable(entity)) {
            throw new NotParsableException();
        }
        EntityLogObject entityLogObject = (EntityLogObject)entityObjectParser.parse(entity);

        builder.indentation(localIndentation);
        builder.entity(entityLogObject);
        builder.targetZone(targetZone);

        return builder.build();
    }

    private boolean isValidIndentation(final String input) {
        return (LogLineUtils.countLeadingSpaces(LogLineUtils.getContentFromLineFromNamedLogger(input)) == indentation);
    }

    public static EntryParser.Factory<TransitioningEntryParser> createFactory() {
        return new Factory();
    }

    public static TransitioningEntryParser createParser(final int indentation) {
        return createFactory().create(indentation);
    }

    public static class Factory implements EntryParser.Factory<TransitioningEntryParser> {
        @Override
        public TransitioningEntryParser create(final int indentation) {
            return new TransitioningEntryParser(indentation);
        }
    }
}
