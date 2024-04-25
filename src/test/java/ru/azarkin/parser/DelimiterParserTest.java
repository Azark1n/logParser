package ru.azarkin.parser;

import org.junit.jupiter.api.Test;
import ru.azarkin.model.LogItem;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DelimiterParserTest {
    @Test
    public void testParseValidLog() {
        DelimiterParser parser = new DelimiterParser();
        String logString = "192.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 200 2 44.510983 \"-\" \"@list-item-updater\" prio:0";
        LogItem logItem = parser.parse(logString);

        ZonedDateTime time = ZonedDateTime.of(2017, 6, 14, 16, 47, 2, 0, ZoneOffset.ofHours(10));
        assertEquals(time, logItem.getTime());
        assertEquals(200, logItem.getStatus());
        assertEquals(44, logItem.getDurationMillis());
    }

    @Test
    public void testParseInvalidLogDate() {
        DelimiterParser parser = new DelimiterParser();
        String logString = "192.168.32.181 - - [14/06/2017:16:47:02] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 200 2 44.510983 \"-\" \"@list-item-updater\" prio:0";
        assertThrows(RuntimeException.class, () -> parser.parse(logString));
    }

    @Test
    public void testParseInvalidLogStatus() {
        DelimiterParser parser = new DelimiterParser();
        String logString = "192.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" ABC 2 44.510983 \"-\" \"@list-item-updater\" prio:0";
        assertThrows(NumberFormatException.class, () -> parser.parse(logString));
    }

    @Test
    public void testParseInvalidDuration() {
        DelimiterParser parser = new DelimiterParser();
        String logStringWithInvalidDuration = "192.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 200 2 44,510983 \"-\" \"@list-item-updater\" prio:0"; // Notice the comma in the duration part
        assertThrows(NumberFormatException.class, () -> parser.parse(logStringWithInvalidDuration));
    }

    @Test
    public void testParseEmptyString() {
        DelimiterParser parser = new DelimiterParser();
        String emptyLogString = "";
        assertThrows(RuntimeException.class, () -> parser.parse(emptyLogString));
    }

    @Test
    public void testParseIncorrectFormatString() {
        DelimiterParser parser = new DelimiterParser();
        String incorrectFormatLogString = "This is not a correct log format";
        assertThrows(RuntimeException.class, () -> parser.parse(incorrectFormatLogString));
    }
}