package com.glencoesoftware.omero.ms.core;

import java.util.List;

import org.python.jline.internal.Log;
import org.slf4j.LoggerFactory;

import brave.ScopedSpan;
import brave.Tracer;
import brave.http.HttpTracing;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class OmeroHttpTracingHandler implements Handler<RoutingContext> {

    final Tracer m_tracer;
    List<String> m_tags;
    
    public OmeroHttpTracingHandler(HttpTracing httpTracing, List<String> tags) {
        m_tags = tags;
        m_tracer = httpTracing.tracing().tracer();
    }

    @Override public void handle(RoutingContext context) {
      ScopedSpan span = m_tracer.startScopedSpan("ms_span");
      TracingEndHandler handler = new TracingEndHandler(context, span, m_tags);
      context.addHeadersEndHandler(handler);
      context.next();
    }
}

final class TracingEndHandler implements Handler<Void> {
    
    private static final org.slf4j.Logger log =
        LoggerFactory.getLogger(TracingEndHandler.class);
      
    private ScopedSpan m_span;
    private RoutingContext m_context;
    private List<String> m_tags;
  
    TracingEndHandler(RoutingContext context, ScopedSpan span, List<String> tags){
        m_context = context;
        m_span = span;
        m_tags = tags;
  }

  @Override
  public void handle(Void event) {
      for(String t : m_tags) {
          try {
              m_span.tag(t, m_context.get(t));
          } catch(Exception e) {
              log.error("Failed to assign tag " + t);
              log.error(e.getMessage());
          }
      }
      m_span.finish();
  }

}
