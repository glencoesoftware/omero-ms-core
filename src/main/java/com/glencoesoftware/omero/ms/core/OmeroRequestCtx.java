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

import java.util.HashMap;
import java.util.Map;

import org.python.jline.internal.Log;

import brave.Tracing;
import brave.propagation.TraceContext.Injector;

/**
 * A context object which is designed to be subclassed and passed as a
 * serialized message through the Vert.x {@link io.vertx.core.eventbus.EventBus}
 * and handled by a service verticle.
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public abstract class OmeroRequestCtx {

    /** OMERO session key. */
    public String omeroSessionKey;

    /** Current trace context to be propagated */
    public Map<String, String> traceContext = new HashMap<String, String>();

    /**
     * Constructor. If using brave tracing, inject the tracing context
     * into the OmeroRequestCtx traceContext.
     */
    public OmeroRequestCtx() {
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return;
        }
        Injector<Map<String, String>> injector =
            tracing.propagation().injector((carrier, key, value) -> {
                carrier.put(key, value);
            }
        );
        injector.inject(
            Tracing.currentTracer().currentSpan().context(), traceContext);
    }

}
