# logParser

`logParser` is a robust log parsing utility designed to analyze and interpret server log files. It identifies errors and calculates uptime statistics to help in monitoring server performance.

## Log Format

The tool is capable of parsing logs in the following format:

```
192.168.32.181 - - [14/06/2017:16:47:02 +1000] "PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1" 200 2 44.510983 "-" "@list-item-updater" prio:0
```

The format captures various details of HTTP requests, including their statuses and response timings.

## Error Detection

An 'error' is determined based on:

- HTTP status codes in the 5xx range indicating server errors.
- Response times that exceed a pre-specified threshold.

## Input

The tool processes:

- A continuous stream of access log data.
- A set minimum availability level percentage, e.g., "99.9%".
- A defined acceptable response time in milliseconds, e.g., "45 ms".

## Output

`logParser` outputs intervals when the system's failure rate surpasses the given threshold and reports the availability level for those intervals.

## Usage Example

```bash
$ cat access.log | java -jar logParser -u 99.9 -t 45
13:32:26    13:33:15    94.5
15:23:02    15:23:08    99.8
```

```bash
$ java -jar logParser -u 99.9 -t 45 -f /var/log/access.log
13:32:26    13:33:15    94.5
15:23:02    15:23:08    99.8
```

### Streaming Processing
logParser is built to process data in a streaming fashion, ensuring efficient log file handling without the need for loading large data sets into memory.

### Implementation
The project is implemented in Java 8, which provides a balance between modern language features and broad compatibility with different environments.

### Building with Apache Maven
To build the project, Apache Maven is required. The following command can be used to compile the project and create an executable JAR:

```bash
mvn clean package
```

This command compiles the code, runs tests, and packages the build into an executable JAR file within the target/ directory.

### Parameters
```
-f,--fileName <arg>       Set log file name. If not set, defaults to System.in.
-mt,--measureTime <arg>   Enable time measurement [y/n]. Default: n.
-p,--period <arg>         Set the measurement period in seconds. Default: 3600.
-pt,--parserType <arg>    Choose parser type [d - delimiter, r - regex]. Default: d.
-t,--time <arg>           Set the acceptable response time per request in millis. Example: 45.
-u,--uptime <arg>         Set the minimum uptime availability percentage. Example: 99.9.
```

The tool allows customization of its behavior and thresholds through various command-line options to fit different scenarios and requirements.

Feel free to fork, clone, and contribute to this project. Your insights and contributions are highly appreciated!