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

import java.util.List;

import org.slf4j.LoggerFactory;

import brave.ScopedSpan;
import brave.Tracer;
import brave.http.HttpTracing;
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

    /** The tracer associtated with the Tracing object
     * configured in the verticle.
     */
    final Tracer tracer;

    /** A list of keys to pull from the RequestContext
     * and assign as tags to the span
     */
    List<String> tags;

    /**
     * Constructor.
     * @param httpTracing Configured brave HttpTracing
     * @param tags Keys to be retrieved from the RoutingContext
     * and assigned as tags to the span.
     */
    public OmeroHttpTracingHandler(HttpTracing httpTracing, List<String> tags) {
        this.tags = tags;
        this.tracer = httpTracing.tracing().tracer();
    }

    /**
     * Sets up the top-level span for each request and
     * assigns an end handler which will finish the span.
     * @param context The context of the Vertx request
     */
    @Override
    public void handle(RoutingContext context) {

      ScopedSpan span = tracer.startScopedSpan("vertx.http_request");
      HttpServerRequest request = context.request();
      span.tag("http.method", request.rawMethod());
      span.tag("http.path", request.path());
      span.tag("http.params", request.params().toString());
      TracingEndHandler handler = new TracingEndHandler(context, span, tags);
      context.put(TracingEndHandler.class.getName(), handler);
      context.addHeadersEndHandler(handler);
      context.next();
    }
}

/**
 * Vertx end handler to assign tags and finish the span.
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
final class TracingEndHandler implements Handler<Void> {

    /**For logging*/
    private static final org.slf4j.Logger log =
        LoggerFactory.getLogger(TracingEndHandler.class);

    /**The span to be finished*/
    private final ScopedSpan span;

    /** The routing context of the request being traced*/
    private final RoutingContext context;

    /** Keys in the RoutingContext to be pulled as span tags*/
    private final List<String> tags;

    /**
     * Constructor.
     * @param httpTracing Configured brave HttpTracing
     * @param span The span to tag and finish
     * @param tags Keys to be retrieved from the RoutingContext
     * and assigned as tags to the span.
     */
    TracingEndHandler(RoutingContext context, ScopedSpan span, List<String> tags){
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
        for(String t : tags) {
            try {
                span.tag(t, context.get(t));
            } catch(Exception e) {
                log.error("Failed to assign tag " + t);
                log.error(e.getMessage());
            }
        }
        span.finish();
    }
}
