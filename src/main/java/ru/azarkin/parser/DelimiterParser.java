package ru.azarkin.parser;

import ru.azarkin.model.LogItem;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parser for a string of the following format:
 * 192.168.32.181 - - [14/06/2017:16:47:02 +1000] "PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1" 200 2 44.510983 "-" "@list-item-updater" prio:0
 */
public class DelimiterParser implements Parser {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss Z");

    @Override
    public LogItem parse(String logString) throws RuntimeException {
        LogItem logItem = new LogItem();

        String[] parts = logString.split("\"");

        String dateTimePart = parts[0].substring(parts[0].indexOf('[') + 1, parts[0].indexOf(']'));
        logItem.setTime(ZonedDateTime.parse(dateTimePart, formatter));

        String[] statusAndDuration = parts[2].trim().split(" ");
        logItem.setStatus(Integer.parseInt(statusAndDuration[0]));

        int durationInMillis = Integer.parseInt(statusAndDuration[2].split("\\.")[0]);
        logItem.setDurationMillis(durationInMillis);

        return logItem;
    }
}
