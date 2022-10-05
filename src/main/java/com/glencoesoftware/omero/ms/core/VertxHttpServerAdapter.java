/*
 * Copyright 2013-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.glencoesoftware.omero.ms.core;

import brave.Span;
import brave.http.HttpServerAdapter;
import brave.internal.Nullable;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;

/**
 * Ported from <code>brave.vertx.web.VertxHttpServerAdapter</code> at tag
 * <code>release-5.6.8</code>.  That implementation is package private so
 * cannot be used by us unless we bring the entire thing in.
 *
 * Why use a thread local for the http route when parsing {@linkplain HttpServerResponse}?</h3>
 *
 * <p>When parsing the response, we use a thread local to make the current route's path visible.
 * This is an alternative to wrapping {@linkplain HttpServerResponse} or declaring a custom type. We
 * don't wrap {@linkplain HttpServerResponse}, because this would lock the instrumentation to the
 * signatures currently present on it (for example, if a method is added, we'd have to recompile).
 * If a wrapper is eventually provided by vertx, we could use that, but it didn't exist at the time.
 * We could also define a custom composite type like ResponseWithTemplate. However, this would
 * interfere with people using "instanceof" in http samplers or parsers: they'd have to depend on a
 * brave type. The least impact means was to use a thread local, as eventhough this costs a little,
 * it prevents revision lock or routine use of custom types.
 */
class VertxHttpServerAdapter extends HttpServerAdapter<HttpServerRequest, HttpServerResponse> {

  @Override public String method(HttpServerRequest request) {
    return request.rawMethod();
  }

  @Override public String path(HttpServerRequest request) {
    return request.path();
  }

  @Override public String url(HttpServerRequest request) {
    return request.absoluteURI();
  }

  @Override public String requestHeader(HttpServerRequest request, String name) {
    return request.headers().get(name);
  }

  @Override public String methodFromResponse(HttpServerResponse ignored) {
    String[] methodAndPath = METHOD_AND_PATH.get();
    return methodAndPath != null ? methodAndPath[0] : null;
  }

  @Override public String route(HttpServerResponse ignored) {
    String[] methodAndPath = METHOD_AND_PATH.get();
    String result = methodAndPath != null ? methodAndPath[1] : null;
    return result != null ? result : "";
  }

  @Override @Nullable public Integer statusCode(HttpServerResponse response) {
    int result = statusCodeAsInt(response);
    return result != 0 ? result : null;
  }

  @Override public int statusCodeAsInt(HttpServerResponse response) {
    return response.getStatusCode();
  }

  /**
   * This sets the client IP:port to the {@linkplain HttpServerRequest#remoteAddress() remote
   * address} if the {@link HttpServerAdapter#parseClientIpAndPort default parsing} fails.
   */
  @Override public boolean parseClientIpAndPort(HttpServerRequest req, Span span) {
    if (parseClientIpFromXForwardedFor(req, span)) return true;
    SocketAddress addr = req.remoteAddress();
    return span.remoteIpAndPort(addr.host(), addr.port());
  }

  /**
   * This uses a string array to avoid leaking a Brave type onto a thread local. If we used a Brave
   * type, it would prevent unloading Brave classes.
   */
  static final ThreadLocal<String[]> METHOD_AND_PATH = new ThreadLocal<>();

  static void setCurrentMethodAndPath(String method, String path) {
    String[] methodAndPath = METHOD_AND_PATH.get();
    if (methodAndPath == null) {
      methodAndPath = new String[2];
      METHOD_AND_PATH.set(methodAndPath);
    }
    methodAndPath[0] = method;
    methodAndPath[1] = path;
  }
}
