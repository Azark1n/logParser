package ru.azarkin.parser;

import ru.azarkin.model.LogItem;

public interface Parser {
    LogItem parse(String logString);
}
