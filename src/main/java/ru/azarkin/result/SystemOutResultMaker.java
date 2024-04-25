package ru.azarkin.result;

import ru.azarkin.model.AccessRange;

public class SystemOutResultMaker implements ResultMaker {
    @Override
    public void make(AccessRange accessRange) {
        System.out.printf("%tT\t%tT\t%.1f%n", accessRange.getBegin(), accessRange.getEnd(),
                (float) accessRange.getSuccess() / (accessRange.getSuccess() + accessRange.getFailure()) * 100);
    }
}
