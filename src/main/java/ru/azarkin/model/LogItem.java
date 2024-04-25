package ru.azarkin.model;

import java.time.ZonedDateTime;

public class LogItem {
    private ZonedDateTime time;
    private int status;
    private int durationMillis;

    public LogItem(ZonedDateTime time, int status, int durationMillis) {
        this.time = time;
        this.status = status;
        this.durationMillis = durationMillis;
    }

    public LogItem() {
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(int durationMillis) {
        this.durationMillis = durationMillis;
    }

}
