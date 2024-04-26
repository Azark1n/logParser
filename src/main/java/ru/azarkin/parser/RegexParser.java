package ru.azarkin.parser;

import ru.azarkin.model.LogItem;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for a string of the following format:
 * 192.168.32.181 - - [14/06/2017:16:47:02 +1000] "PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1" 200 2 44.510983 "-" "@list-item-updater" prio:0
 */
public class RegexParser implements Parser {
    private static final Pattern LOG_PATTERN = Pattern.compile("^[0-9\\.]+ - - \\[([\\d\\/: \\+]+)\\] \\\".+\\\" (\\d+) \\d+ (\\d{0,3})\\..+$", Pattern.MULTILINE);

    public LogItem parse(String logString) throws RuntimeException {
        Matcher matcher = LOG_PATTERN.matcher(logString);
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid log format");
        }

        LogItem logItem = new LogItem();

        ZonedDateTime dateTime = ZonedDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss Z"));
        logItem.setTime(dateTime);

        logItem.setStatus(Integer.parseInt(matcher.group(2)));

        logItem.setDurationMillis(Integer.parseInt(matcher.group(3).replace(".", "")));

        return logItem;
    }
}
