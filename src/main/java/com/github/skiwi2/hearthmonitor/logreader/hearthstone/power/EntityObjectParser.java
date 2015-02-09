package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.LogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import com.github.skiwi2.hearthmonitor.logreader.ObjectParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse entity log objects.
 *
 * @author Frank van Heeswijk
 */
public class EntityObjectParser implements ObjectParser {
    /**
     * Examples of supported cases:
     *  - skiwi
     *  - [name=Dread Infernal id=34 zone=HAND zonePos=3 cardId=CS2_064 player=1]
     *  - [id=33 cardId= type=INVALID zone=DECK zonePos=0 player=1]
     */

    @Override
    public boolean isParsable(final String input) {
        return true;
    }

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[name='
     *  - followed by zero or more characters, captured as the 1st group
     *  - followed by literal text ' id='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' zone='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' zonePos='
     *  - followed by zero or more characters, captured as the 4th group
     *  - followed by literal text ' cardId='
     *  - followed by zero or more characters, captured as the 5th group
     *  - followed by literal text ' player='
     *  - followed by zero or more characters, captured as the 6th group
     *  - ending with literal text ']'
     */
    private static final Pattern EXTRACT_ENTITY_CASE_1_PATTERN =
        Pattern.compile("^" + Pattern.quote("[name=") + "(.*)" + Pattern.quote(" id=") + "(.*)" + Pattern.quote(" zone=") + "(.*)" + Pattern.quote(" zonePos=")
            + "(.*)" + Pattern.quote(" cardId=") + "(.*)" + Pattern.quote(" player=") + "(.*)" + Pattern.quote("]") + "$");

    /**
     * Pattern that checks if a string matches the following:
     *  - starts with literal text '[id='
     *  - followed by zero or more characters, captured as the 1st group
     *  - followed by literal text ' cardId='
     *  - followed by zero or more characters, captured as the 2nd group
     *  - followed by literal text ' type='
     *  - followed by zero or more characters, captured as the 3rd group
     *  - followed by literal text ' zone='
     *  - followed by zero or more characters, captured as the 4th group
     *  - followed by literal text ' ZonePos='
     *  - followed by zero or more characters, captured as the 5th group
     *  - followed by literal text ' player='
     *  - followed by zero or more characters, captured as the 6th group
     *  - ending with literal text ']'
     */
    private static final Pattern EXTRACT_ENTITY_CASE_2_PATTERN =
        Pattern.compile("^" + Pattern.quote("[id=") + "(.*)" + Pattern.quote(" cardId=") + "(.*)" + Pattern.quote(" type=") + "(.*)" + Pattern.quote(" zone=") + "(.*)" + Pattern.quote(" zonePos=")
            + "(.*)" + Pattern.quote(" player=") + "(.*)" + Pattern.quote("]") + "$");

    @Override
    public LogObject parse(final String input) throws NotParsableException {
        Matcher case1Matcher = EXTRACT_ENTITY_CASE_1_PATTERN.matcher(input);
        Matcher case2Matcher = EXTRACT_ENTITY_CASE_2_PATTERN.matcher(input);
        if (case1Matcher.find()) {
            String name = case1Matcher.group(1);
            String id = case1Matcher.group(2);
            String zone = case1Matcher.group(3);
            String zonePos = case1Matcher.group(4);
            String cardId = case1Matcher.group(5);
            String player = case1Matcher.group(6);
            return new CardEntityLogObject.Builder()
                .name(name)
                .id(id)
                .zone(zone)
                .zonePos(zonePos)
                .cardId(cardId)
                .player(player)
                .build();
        }
        else if (case2Matcher.find()) {
            String id = case2Matcher.group(1);
            String cardId = case2Matcher.group(2);
            String type = case2Matcher.group(3);
            String zone = case2Matcher.group(4);
            String zonePos = case2Matcher.group(5);
            String player = case2Matcher.group(6);
            return new CardEntityLogObject.Builder()
                .id(id)
                .cardId(cardId)
                .type(type)
                .zone(zone)
                .zonePos(zonePos)
                .player(player)
                .build();
        }
        else {
            return new PlayerEntityLogObject.Builder()
                .name(input)
                .build();
        }
    }
}
