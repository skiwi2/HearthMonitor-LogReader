package com.github.skiwi2.hearthmonitor.logreader.hearthstone.power;

import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logreader.NotParsableException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class EntityObjectParserTest {
    @Test
    public void testPlayerEntity() throws URISyntaxException, IOException, NotParsableException {
        String entity = Files.lines(Paths.get(getClass().getResource("PlayerEntity.object").toURI()), StandardCharsets.UTF_8).findFirst().get();
        EntityObjectParser entityObjectParser = new EntityObjectParser();

        assertTrue(entityObjectParser.isParsable(entity));

        PlayerEntityLogObject playerEntityLogObject = (PlayerEntityLogObject)entityObjectParser.parse(entity);
        assertEquals("skiwi", playerEntityLogObject.getName());
    }

    @Test
    public void testCardEntity1() throws URISyntaxException, IOException, NotParsableException {
        String entity = Files.lines(Paths.get(getClass().getResource("CardEntity-1.object").toURI()), StandardCharsets.UTF_8).findFirst().get();
        EntityObjectParser entityObjectParser = new EntityObjectParser();

        assertTrue(entityObjectParser.isParsable(entity));

        CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityObjectParser.parse(entity);
        assertEquals("Dread Infernal", cardEntityLogObject.getName());
        assertEquals("34", cardEntityLogObject.getId());
        assertEquals("HAND", cardEntityLogObject.getZone());
        assertEquals("3", cardEntityLogObject.getZonePos());
        assertEquals("CS2_064", cardEntityLogObject.getCardId());
        assertEquals("1", cardEntityLogObject.getPlayer());
    }
    @Test
    public void testCardEntity2() throws URISyntaxException, IOException, NotParsableException {
        String entity = Files.lines(Paths.get(getClass().getResource("CardEntity-2.object").toURI()), StandardCharsets.UTF_8).findFirst().get();
        EntityObjectParser entityObjectParser = new EntityObjectParser();

        assertTrue(entityObjectParser.isParsable(entity));

        CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityObjectParser.parse(entity);
        assertEquals("33", cardEntityLogObject.getId());
        assertEquals("", cardEntityLogObject.getCardId());
        assertEquals("INVALID", cardEntityLogObject.getType());
        assertEquals("DECK", cardEntityLogObject.getZone());
        assertEquals("0", cardEntityLogObject.getZonePos());
        assertEquals("1", cardEntityLogObject.getPlayer());
    }
}