/*
 * Copyright (C) 2019 Glencoe Software, Inc. All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpTracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * Vertx event handler which sets up a top-level span
 * as part of a Zipkin trace
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
public class OmeroHttpTracingHandler implements Handler<RoutingContext> {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroHttpTracingHandler.class);

    /**
     * The tracer associtated with the Tracing object
     * configured in the verticle.
     */
    final Tracer tracer;

    /**
     * A list of keys to pull from the {@link RequestContext}
     * and assign as tags to the span
     */
    final List<String> tags;

    /**
     * Injector for putting the TracingContext into the RoutingContext
     */
    Injector<Map<String, String>> injector;

    /**
     * Extractor for getting trace info from request headers
     */
    final Extractor<HttpServerRequest> extractor;

    /**
     * Constructor.
     * @param httpTracing Configured brave {@link HttpTracing}
     * @param tags Keys to be retrieved from the {@link RoutingContext}
     * and assigned as tags to the span.
     */
    public OmeroHttpTracingHandler(HttpTracing httpTracing, List<String> tags) {
        this.tags = tags;
        this.tracer = httpTracing.tracing().tracer();
        injector =
                httpTracing
                    .tracing()
                    .propagation()
                    .injector((carrier, key, value) -> {
                    carrier.put(key, value);
                }
            );
        extractor = httpTracing
                        .tracing()
                        .propagation()
                        .extractor((carrier, key) -> {
                            return carrier.getHeader(key);
                        });
    }

    /**
     * Sets up the top-level span for each request and
     * assigns an end handler which will finish the span.
     * @param context The context of the Vertx request
     */
    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        Span span = null;
        //Check for tracing context in the request
        TraceContext traceContext = extractor.extract(request).context();
        if(traceContext != null) {
            span = tracer.newChild(traceContext).name("vertx.http_request");
        } else {
            span = tracer.newTrace().name("vertx.http_request");
        }
        log.info(span.context().toString());
        span.start();
        span.tag("http.method", request.rawMethod());
        span.tag("http.path", request.path());
        span.tag("http.params", request.params().toString());
        Map<String, String> traceContextMap = new HashMap<String, String>();
        injector.inject(
            span.context(), traceContextMap);
        TracingEndHandler handler = new TracingEndHandler(context, span, tags);
        context.put(TracingEndHandler.class.getName(), handler);
        context.put("traceContext", traceContextMap);
        context.addHeadersEndHandler(handler);
        try (SpanInScope ws = tracer.withSpanInScope(span)){
            context.next();
        }
    }
}

/**
 * Vertx end handler to assign tags and finish the span.
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
final class TracingEndHandler implements Handler<Void> {

    /** For logging */
    private static final org.slf4j.Logger log =
        LoggerFactory.getLogger(TracingEndHandler.class);

    /** The span to be finished */
    private final Span span;

    /** The routing context of the request being traced */
    private final RoutingContext context;

    /** Keys in the {@link RoutingContext} to be pulled as span tags */
    private final List<String> tags;

    /**
     * Constructor.
     * @param httpTracing Configured brave {@link HttpTracing}
     * @param span The span to tag and finish
     * @param tags Keys to be retrieved from the {@link RoutingContext}
     * and assigned as tags to the span.
     */
    TracingEndHandler(RoutingContext context, Span span, List<String> tags){
        this.context = context;
        this.span = span;
        this.tags = tags;
    }

    /**
     * Sets up the top-level span for each request and
     * assigns an end handler which will finish the span.
     * @param context The context of the Vertx request
     */
    @Override
    public void handle(Void event) {
        if (tags != null) {
            for (String t : tags) {
                try {
                    span.tag(t, context.get(t));
                } catch(Exception e) {
                    log.error("Failed to assign tag " + t, e);
                }
            }
        }
        span.finish();
    }
}
