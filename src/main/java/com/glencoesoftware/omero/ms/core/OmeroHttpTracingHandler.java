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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
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
    final Injector<Map<String, String>> injector;

    /**
     * Extractor for getting trace info from request headers
     */
    final Extractor<HttpServerRequest> extractor;

    /** Our HTTP server handler implementation */
    final HttpServerHandler<HttpServerRequest, HttpServerResponse> serverHandler;

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
                    });
        extractor =
                httpTracing
                    .tracing()
                    .propagation()
                    .extractor((carrier, key) -> {
                        return carrier.getHeader(key);
                    });
        serverHandler = HttpServerHandler.create(
                httpTracing, new VertxHttpServerAdapter());
    }

    /**
     * Sets up the top-level span for each request and
     * assigns an end handler which will finish the span.
     * @param context The context of the Vertx request
     */
    @Override
    public void handle(RoutingContext context) {
        TracingEndHandler tracingHandler =
                context.get(TracingEndHandler.class.getName());
        if (tracingHandler != null) { // then we already have a span
            if (!context.failed()) { // re-routed, so re-attach the end handler
                context.addHeadersEndHandler(tracingHandler);
            }
            context.next();
            return;
        }

        Span span = serverHandler.handleReceive(extractor, context.request());
        TracingEndHandler handler = new TracingEndHandler(context, span, tags);
        context.put(TracingEndHandler.class.getName(), handler);
        context.addHeadersEndHandler(handler);
        try (SpanInScope ws = tracer.withSpanInScope(span)) {
            context.next();
        }
    }

    /**
     * Vertx end handler to assign tags and finish the span.
     * @author Kevin Kozlowski <kevin@glencoesoftware.com>
     *
     */
    final class TracingEndHandler implements Handler<Void> {

        /** The span to be finished */
        final Span span;

        /** The routing context of the request being traced */
        final RoutingContext context;

        /** Whether or not the request has completed */
        final AtomicBoolean finished = new AtomicBoolean();

        /** Keys in the {@link RoutingContext} to be pulled as span tags */
        final List<String> tags;

        /**
         * Constructor.
         * @param httpTracing Configured brave {@link HttpTracing}
         * @param span The span to tag and finish
         * @param tags Keys to be retrieved from the {@link RoutingContext}
         * and assigned as tags to the span.
         */
        TracingEndHandler(
                RoutingContext context, Span span, List<String> tags) {
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
            // Tags are added to the span at the end of the request as values
            // will be put on the context as the request is being fulfilled
            if (tags != null) {
                for (String t : tags) {
                    try {
                        String value = context.get(t);
                        if (value != null) {
                            span.tag(t, value);
                        }
                    } catch (Exception e) {
                        log.error("Failed to assign tag " + t, e);
                    }
                }
            }

            if (!finished.compareAndSet(false, true)) {
                return;
            }
            VertxHttpServerAdapter.setCurrentMethodAndPath(
                context.request().rawMethod(),
                context.currentRoute().getPath()
            );
            try {
                serverHandler.handleSend(
                        context.response(), context.failure(), span);
            } finally {
                VertxHttpServerAdapter.setCurrentMethodAndPath(null, null);
            }
        }
    }
}
