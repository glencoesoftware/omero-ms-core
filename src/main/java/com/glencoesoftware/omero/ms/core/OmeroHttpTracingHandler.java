package com.glencoesoftware.omero.ms.core;

import java.util.List;

import org.slf4j.LoggerFactory;

import brave.ScopedSpan;
import brave.Tracer;
import brave.http.HttpTracing;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class OmeroHttpTracingHandler implements Handler<RoutingContext> {

    final Tracer tracer;
    List<String> tags;

    public OmeroHttpTracingHandler(HttpTracing httpTracing, List<String> tags) {
        this.tags = tags;
        this.tracer = httpTracing.tracing().tracer();
    }

    @Override public void handle(RoutingContext context) {

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

final class TracingEndHandler implements Handler<Void> {

    private static final org.slf4j.Logger log =
        LoggerFactory.getLogger(TracingEndHandler.class);

    private final ScopedSpan span;
    private final RoutingContext context;
    private final List<String> tags;

    TracingEndHandler(RoutingContext context, ScopedSpan span, List<String> tags){
        this.context = context;
        this.span = span;
        this.tags = tags;
  }

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
