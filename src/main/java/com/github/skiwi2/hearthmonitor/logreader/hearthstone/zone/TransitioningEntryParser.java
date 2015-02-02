package com.github.skiwi2.hearthmonitor.logreader.hearthstone.zone;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.LineReader;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;

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

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[Zone] ZoneChangeList.ProcessChanges() - '
     *  - followed by zero or more space characters, captured as the 1st group
     *  - followed by literal text 'TRANSITIONING card [name='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' id='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' zone='
     *  - followed by zero or more characters, captured as the 4th group
     *  - followed by literal text ' zonePos='
     *  - followed by zero or more characters, captured as the 5th group
     *  - followed by literal text ' cardId='
     *  - followed by zero or more characters, captured as the 6th group
     *  - followed by literal text ' player='
     *  - followed by zero or more characters, captured as the 7th group
     *  - followed by literal text '] to '
     *  - ending with zero or more characters, captured as the 8th group
     */
    private static final Pattern EXTRACT_TRANSITIONING_PATTERN =
        Pattern.compile("^" + Pattern.quote("[Zone] ZoneChangeList.ProcessChanges() - ") + "(\\s*)" + Pattern.quote("TRANSITIONING card [name=") + "(.*)"
            + Pattern.quote(" id=") + "(.*)" + Pattern.quote(" zone=") + "(.*)" + Pattern.quote(" zonePos=") + "(.*)"
            + Pattern.quote(" cardId=") + "(.*)" + Pattern.quote(" player=") + "(.*)" + Pattern.quote("] to ") + "(.*)$");

    @Override
    public boolean isParsable(final String input) {
        return EXTRACT_TRANSITIONING_PATTERN.matcher(input).matches();
    }

    @Override
    public LogEntry parse(final String input, final LineReader lineReader) throws NotParsableException {
        TransitioningLogEntry.Builder builder = new TransitioningLogEntry.Builder();

        Matcher transitioningMatcher = EXTRACT_TRANSITIONING_PATTERN.matcher(input);
        if (!transitioningMatcher.find()) {
            throw new NotParsableException();
        }
        int indentation = transitioningMatcher.group(1).length();
        String name = transitioningMatcher.group(2);
        String id = transitioningMatcher.group(3);
        String zone = transitioningMatcher.group(4);
        String zonePos = transitioningMatcher.group(5);
        String cardId = transitioningMatcher.group(6);
        String player = transitioningMatcher.group(7);
        String targetZone = transitioningMatcher.group(8);

        builder.indentation(indentation);
        builder.name(name);
        builder.id(id);
        builder.zone(zone);
        builder.zonePos(zonePos);
        builder.cardId(cardId);
        builder.player(player);
        builder.targetZone(targetZone);

        return builder.build();
    }
}
