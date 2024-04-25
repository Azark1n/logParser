package ru.azarkin;

import org.apache.commons.cli.*;
import ru.azarkin.parser.DelimiterParser;
import ru.azarkin.parser.Parser;
import ru.azarkin.parser.RegexParser;
import ru.azarkin.result.SystemOutResultMaker;

import java.io.*;

public class Main {
    private static final int STATUS_FAIL_MIN = 500;
    private static final int STATUS_FAIL_MAX = 599;
    private static int durationMillisOk;
    private static float availabilityMin;
    private static long periodMillis;
    private static String fileName;
    private static String parserType;
    private static boolean measureTime;

    public static void main(String[] args) {
        readArgs(args);

        Parser parser;
        if ("d".equals(parserType)) {
            parser = new DelimiterParser();
        } else {
            parser = new RegexParser();
        }

        SystemOutResultMaker resultMaker = new SystemOutResultMaker();

        LogStatistics logStatistics = new LogStatistics(periodMillis, STATUS_FAIL_MIN, STATUS_FAIL_MAX, durationMillisOk, availabilityMin, parser, resultMaker);

        long startTime = 0;
        if (measureTime) {
            startTime = System.currentTimeMillis();
        }

        if (fileName == null) {
            InputStreamReader isr = new InputStreamReader(System.in);
            logStatistics.process(isr);
        } else {
            try (InputStreamReader isr = new FileReader(fileName)) {
                logStatistics.process(isr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (measureTime) {
            System.out.printf("Execution time: %s ms\n", System.currentTimeMillis() - startTime);
        }
    }

    private static void readArgs(String[] args) {
        Options options = new Options();

        Option uptimeOption = new Option("u", "uptime", true, "set uptime availability percentage. Ex: 99.9");
        uptimeOption.setRequired(true);
        options.addOption(uptimeOption);

        Option timeOption = new Option("t", "time", true, "set time duration OK per request in millis. Ex: 45");
        timeOption.setRequired(true);
        options.addOption(timeOption);

        Option periodOption = new Option("p", "period", true, "set measurement period in seconds. Default: 3600");
        periodOption.setRequired(false);
        options.addOption(periodOption);

        Option fileNameOption = new Option("f", "fileName", true, "set log file name. If not set - System.In");
        fileNameOption.setRequired(false);
        options.addOption(fileNameOption);

        Option parserTypeOption = new Option("pt", "parserType", true, "set parser type [d - delimiter, r - regex]. Default: d");
        parserTypeOption.setRequired(false);
        options.addOption(parserTypeOption);

        Option measureTimeOption = new Option("mt", "measureTime", true, "set measure time [y/n]. Default: n");
        measureTimeOption.setRequired(false);
        options.addOption(measureTimeOption);

        CommandLineParser parserCli = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parserCli.parse(options, args);
            availabilityMin = Float.parseFloat(cmd.getOptionValue("uptime"));
            durationMillisOk = Integer.parseInt(cmd.getOptionValue("time"));
            periodMillis = Long.parseLong(cmd.getOptionValue("period", "3600")) * 1_000;
            fileName = cmd.getOptionValue("fileName");
            parserType = cmd.getOptionValue("parserType", "d");
            measureTime = cmd.getOptionValue("measureTime", "n").equals("y");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(120, "logParser", "", options, "");

            System.exit(1);
        }
    }
}