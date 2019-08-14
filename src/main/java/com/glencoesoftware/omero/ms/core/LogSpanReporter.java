package com.glencoesoftware.omero.ms.core;

import org.slf4j.LoggerFactory;

import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;

public class LogSpanReporter extends AsyncReporter<Span> {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(LogSpanReporter.class);

    @Override
    public void report(Span span) {
        System.out.println("In LogSpanReporter report");
        if(span != null) {
            log.info(span.toString());
        } else {
            log.info("Null span");
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

}
