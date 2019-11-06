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

import java.util.Map;

import brave.Tracing;
import brave.propagation.TraceContext.Extractor;
import io.vertx.core.AbstractVerticle;

/**
 * Base class for Omero Microservice verticles to
 * centralize shared functionality.
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
public abstract class OmeroMsAbstractVerticle extends AbstractVerticle {

    /**
     * Retrieves the current tracing extractor if
     * tracing is being used in this microservice.
     * @return current extractor or <code>null</code> if tracing is not
     * enabled
     */
    protected Extractor<Map<String, String>> extractor() {
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return null;
        }
        return tracing.propagation()
            .extractor((carrier, key) -> {
                return carrier.get(key);
            });
    }

}
