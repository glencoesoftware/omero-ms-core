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

import io.vertx.core.MultiMap;

import brave.Tracing;
import brave.propagation.Propagation.Setter;
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

    /** Setter which will be used during <code>traceContext</code> injection */
    static final Setter<Map<String, String>, String> SETTER =
            (carrier, key, value) -> {
                carrier.put(key, value);
            };

    /**
     * If using brave tracing, inject the tracing context
     * into the <code>traceContext</code>.
     */
    public void injectCurrentTraceContext() {
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return;
        }
        Injector<Map<String, String>> injector =
                tracing.propagation().injector(SETTER);
        injector.inject(
                Tracing.currentTracer().currentSpan().context(), traceContext);
    }

    /**
     * Return the key of a multi-map
     * @param params a multi-map object
     * @param key the key to return
     * @return true if the key exists and has a value equal to true, false otherwise
     */
    public static String getCheckedParam(MultiMap params, String key)
        throws IllegalArgumentException {
        String value = params.get(key);
        if (null == value) {
            throw new IllegalArgumentException("Missing parameter '"
                + key + "'");
        }
        return value;
    }

    /**
     * Return a key as a boolean from a multimap
     * @param params a MultiMap object
     * @param key a String specifying the key
     * @return true if the key exists and has a value equal to true, false otherwise
     */
    public static Boolean getBooleanParameter(MultiMap params, String key) {
        if (params.get(key) != null) {
            String booleanString = params.get(key).toLowerCase();
            if (booleanString.equals("true")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse a string value as a Long object
     * @param imageIdString a value representing an image ID
     * @return the image ID as an Long object
     * @throw an IllegalArgumentException if the string cannot be parsed as a Long
     */
    public static Long getImageIdFromString(String imageIdString)
        throws IllegalArgumentException{
        try {
            return Long.parseLong(imageIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect format for "
                + "imageId parameter '" + imageIdString + "'");
        }
    }

    /**
     * Parse a string value as an Integer and return it
     * @param intString a value representing an integer
     * @return the value parsed as an Integer object
     * @throw an IllegalArgumentException if the string cannot be parsed as an integer
     */
    public static Integer getIntegerFromString(String intString)
        throws IllegalArgumentException{
        Integer i = null;
        try {
            i = Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect format for "
                + "parameter value '" + intString + "'");
        }
        return i;
    }
}
