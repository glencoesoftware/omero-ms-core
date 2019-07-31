/*
 * Copyright (C) 2017 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.glencoesoftware.omero.ms.core;

import org.slf4j.LoggerFactory;

import brave.handler.FinishedSpanHandler;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;

import io.prometheus.client.Summary;

/**
 * Brave FinishSpanHandler which if registered records
 * a prometheus timestamp metric every time a span is finished.
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
public class PrometheusSpanHandler extends FinishedSpanHandler {

    /** For logging */
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PrometheusSpanHandler.class);

    /** The Prometheus Summary which will be used to
     * record the duration of the spans
     */
    private Summary spanDuration;

    /** Default constructor. Sets up the Prometheus Summary
     * to record the span durations by span name.
     * Spans recording the same operation should have the same name.
     */
    public PrometheusSpanHandler() {
        spanDuration = Summary.build()
                .name("spanDuration")
                .labelNames("spanName")
                .help("The duration of spans")
                .register();
    }

    /**
     * The function which gets called on span.finish()
     * Records the name and duration of the span.
     */
    @Override
    public boolean handle(TraceContext context, MutableSpan span) {
        spanDuration.labels(span.name()).observe(span.finishTimestamp() - span.startTimestamp());
        log.info(span.name());
        log.info("In FinishedSpanHandler");
        return true;
    }

}
