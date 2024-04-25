package ru.azarkin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.azarkin.model.AccessRange;
import ru.azarkin.model.LogItem;
import ru.azarkin.parser.DelimiterParser;
import ru.azarkin.parser.Parser;
import ru.azarkin.result.ResultMaker;
import ru.azarkin.result.SystemOutResultMaker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogStatisticsTest {
    @Mock
    private Parser parser;
    @Mock
    private ResultMaker resultMaker;
    private LogStatistics logStatistics;

    @BeforeEach
    void setup() {
        logStatistics = new LogStatistics(5000, 500, 599, 45_000_000, 99.9f, parser, resultMaker);
    }

    @Test
    void testAppendAndPollResults() {
        ZonedDateTime now = ZonedDateTime.now();
        LogItem item1 = new LogItem(now, 204, 44_999_999);
        LogItem item2 = new LogItem(now.plusNanos(6000_000_000L), 200, 45_000_001);

        logStatistics.append(item1);
        assertFalse(logStatistics.isResultAvailable(), "No result should be available after one item");

        logStatistics.append(item2);
        assertTrue(logStatistics.isResultAvailable(), "Result should be available after starting new period");

        AccessRange result = logStatistics.pollResult();
        assertNotNull(result, "Result should not be null after polling");
        assertEquals(result.getEnd(), item1.getTime());
        assertEquals(result.getSuccess(), 1);
        assertEquals(result.getFailure(), 0);

        AccessRange result2 = logStatistics.pollResult();
        assertNotNull(result2, "Result should not be null after polling");
        assertEquals(result2.getEnd(), item2.getTime());
        assertEquals(result2.getSuccess(), 0);
        assertEquals(result2.getFailure(), 1);
    }

    @Test
    void testAppendAndPollResultsFailDuration() {
        ZonedDateTime now = ZonedDateTime.now();
        LogItem item = new LogItem(now, 204, 45_000_001);

        logStatistics.append(item);
        assertFalse(logStatistics.isResultAvailable(), "No result should be available after one item");

        AccessRange result = logStatistics.pollResult();
        assertNotNull(result, "Result should not be null after polling");
        assertEquals(result.getEnd(), item.getTime());
        assertEquals(result.getSuccess(), 0);
        assertEquals(result.getFailure(), 1);
    }

    @Test
    void testAppendAndPollResultsFailStatus() {
        ZonedDateTime now = ZonedDateTime.now();
        LogItem item = new LogItem(now, 500, 44_999_999);

        logStatistics.append(item);
        assertFalse(logStatistics.isResultAvailable(), "No result should be available after one item");

        AccessRange result = logStatistics.pollResult();
        assertNotNull(result, "Result should not be null after polling");
        assertEquals(result.getEnd(), item.getTime());
        assertEquals(result.getSuccess(), 0);
        assertEquals(result.getFailure(), 1);
    }

    @Test
    void testAppendAndPollResultsFailStatusAndDuration() {
        ZonedDateTime now = ZonedDateTime.now();
        LogItem item = new LogItem(now, 500, 45_000_001);

        logStatistics.append(item);
        assertFalse(logStatistics.isResultAvailable(), "No result should be available after one item");

        AccessRange result = logStatistics.pollResult();
        assertNotNull(result, "Result should not be null after polling");
        assertEquals(result.getEnd(), item.getTime());
        assertEquals(result.getSuccess(), 0);
        assertEquals(result.getFailure(), 1);
    }

    @Test
    void testResultNotAvailableWhenConditionsNotMet() {
        ZonedDateTime now = ZonedDateTime.now();
        LogItem item = new LogItem(now, 500, 44_000_000);

        logStatistics.append(item);
        assertFalse(logStatistics.isResultAvailable(), "Result should not be available");
    }

    @Test
    void testProcessWithErrorInParsing() {
        String logLine = "invalid log line";
        when(parser.parse(logLine)).thenThrow(new RuntimeException("Parsing error"));

        assertDoesNotThrow(() -> logStatistics.process(new StringReader(logLine)),
                "Processing should handle runtime exceptions without throwing");

        verify(parser).parse(logLine);
        verify(resultMaker, never()).make(any());
    }

    @Test
    void testProcessWithLogString() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        DelimiterParser parser = new DelimiterParser();
        SystemOutResultMaker resultMaker = new SystemOutResultMaker();
        logStatistics = new LogStatistics(5000, 500, 599, 45_000_000, 99.9f, parser, resultMaker);

        StringReader logReader = new StringReader("192.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 200 2 44.510983 \"-\" \"@list-item-updater\" prio:0\n" +
                "192.168.32.181 - - [14/06/2017:17:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 500 2 44.510983 \"-\" \"@list-item-updater\" prio:0\n" +
                "192.168.32.181 - - [14/06/2017:17:47:04 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 201 2 44.510983 \"-\" \"@list-item-updater\" prio:0\n");
        logStatistics.process(logReader);

        String output = outContent.toString();
        assertEquals(output,"17:47:02\t17:47:04\t50,0\n");

        System.setOut(System.out);
    }
}