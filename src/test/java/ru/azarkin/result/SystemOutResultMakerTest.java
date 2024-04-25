package ru.azarkin.result;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.azarkin.model.AccessRange;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemOutResultMakerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testMake() {
        SystemOutResultMaker resultMaker = new SystemOutResultMaker();
        ZonedDateTime startTime = ZonedDateTime.of(2024, 4, 23, 15, 23, 2, 0, ZoneId.systemDefault());

        AccessRange accessRange = new AccessRange(startTime);
        accessRange.putItemStatus(startTime.plusSeconds(1), true);
        accessRange.putItemStatus(startTime.plusSeconds(2), false);
        accessRange.putItemStatus(startTime.plusSeconds(3), true);

        resultMaker.make(accessRange);

        String expectedOutput = String.format("%tT\t%tT\t%.1f%n", startTime, startTime.plusSeconds(3), 66.7);
        assertEquals(expectedOutput, outContent.toString());
    }
}