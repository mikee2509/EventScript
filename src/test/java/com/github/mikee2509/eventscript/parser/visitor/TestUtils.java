package com.github.mikee2509.eventscript.parser.visitor;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@AllArgsConstructor
class TestUtils {
    private Logger logger;

    List<String> captureLogs(Code code) {
        List<String> logRecords = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logRecords.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        logger.addHandler(handler);
        code.execute();
        logger.removeHandler(handler);
        return logRecords;
    }

    public interface Code {
        void execute();
    }
}
