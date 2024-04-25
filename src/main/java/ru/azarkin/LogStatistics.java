package ru.azarkin;

import ru.azarkin.model.AccessRange;
import ru.azarkin.model.LogItem;
import ru.azarkin.parser.Parser;
import ru.azarkin.result.ResultMaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;

public class LogStatistics {
    private AccessRange currentAccessRange;
    private AccessRange result;
    private boolean resultAvailable;
    private final long periodMillis;
    private final ResultMaker resultMaker;
    private final Parser parser;
    private final int statusFailMin;
    private final int statusFailMax;
    private final long durationMillisOk;
    private final float availabilityMin;

    public LogStatistics(long periodMillis, int statusFailMin, int statusFailMax, int durationMillisOk, float availabilityMin, Parser parser, ResultMaker resultMaker) {
        this.periodMillis = periodMillis;
        this.statusFailMin = statusFailMin;
        this.statusFailMax = statusFailMax;
        this.availabilityMin = availabilityMin;
        this.durationMillisOk = durationMillisOk;
        this.resultMaker = resultMaker;
        this.parser = parser;
    }

    public void append(LogItem logItem) {
        if (currentAccessRange == null) {
            currentAccessRange = new AccessRange(logItem.getTime());
        }
        if (Duration.between(currentAccessRange.getBegin(), logItem.getTime()).toMillis() >= periodMillis) {
            result = currentAccessRange;
            resultAvailable = true;
            currentAccessRange = new AccessRange(logItem.getTime());
        }
        boolean success = (logItem.getStatus() < statusFailMin || logItem.getStatus() > statusFailMax) && logItem.getDurationMillis() <= durationMillisOk;
        currentAccessRange.putItemStatus(logItem.getTime(), success);
    }

    public boolean isResultAvailable() {
        return resultAvailable;
    }

    public AccessRange pollResult() {
        if (resultAvailable) {
            resultAvailable = false;
            return result;
        } else {
            return currentAccessRange;
        }
    }

    public void process(Reader reader) {
        try (BufferedReader buffer = new BufferedReader(reader)) {
            String line;
            while ((line = buffer.readLine()) != null) {
                LogItem logItem;
                try {
                    logItem = parser.parse(line);
                } catch (RuntimeException e) {
                    System.err.println("Error parsing: " + e.getMessage());
                    continue;
                }
                append(logItem);
                if (resultAvailable) {
                    checkAndMake();
                }
            }
            if (currentAccessRange != null) {
                checkAndMake();
            }
        } catch (IOException e) {
            System.err.println("Error reading: " + e.getMessage());
        }
    }

    private void checkAndMake() {
        AccessRange accessRange = pollResult();
        float availability = (float) accessRange.getSuccess() / (accessRange.getSuccess() + accessRange.getFailure()) * 100;
        if (Math.round(availability * 10) < availabilityMin * 10) {
            resultMaker.make(accessRange);
        }
    }
}
