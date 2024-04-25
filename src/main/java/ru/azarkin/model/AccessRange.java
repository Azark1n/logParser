package ru.azarkin.model;

import java.time.ZonedDateTime;

public class AccessRange {
    private final ZonedDateTime begin;
    private ZonedDateTime end;
    private long success;
    private long failure;

    public AccessRange(ZonedDateTime begin) {
        this.begin = begin;
    }

    public void putItemStatus(ZonedDateTime time, boolean status) {
        end = time;
        if (status) {
            success++;
        } else {
            failure++;
        }
    }

    public ZonedDateTime getBegin() {
        return begin;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public long getSuccess() {
        return success;
    }

    public long getFailure() {
        return failure;
    }
}
