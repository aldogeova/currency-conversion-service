package com.pros.currencyconversionservice.utils;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2024-01-03
 */
public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    private final List<ILoggingEvent> logs = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        logs.add(event);
    }

    public List<ILoggingEvent> getLogs() {
        return logs;
    }
}
