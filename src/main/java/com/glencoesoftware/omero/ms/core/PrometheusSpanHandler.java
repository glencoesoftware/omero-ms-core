package com.glencoesoftware.omero.ms.core;

import org.slf4j.LoggerFactory;

import brave.handler.FinishedSpanHandler;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;

import io.prometheus.client.Summary;


public class PrometheusSpanHandler extends FinishedSpanHandler {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PrometheusSpanHandler.class);

    private Summary spanDuration;

    public PrometheusSpanHandler() {
        spanDuration = Summary.build()
                .name("spanDuration")
                .labelNames("spanName")
                .help("The duration of spans")
                .register();
    }

    @Override
    public boolean handle(TraceContext context, MutableSpan span) {
        spanDuration.labels(span.name()).observe(span.finishTimestamp() - span.startTimestamp());
        log.info(span.name());
        log.info("In FinishedSpanHandler");
        return true;
    }

}
