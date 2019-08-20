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

import org.slf4j.LoggerFactory;

import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;

/**
 * Zipkin reporter for logging traces instead of sending them
 * to a Zipkin server
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
public class LogSpanReporter extends AsyncReporter<Span> {

    /** For logging */
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(LogSpanReporter.class);

    /**
     * Report the span - which in this case is just to log it
     * @param span The span to report
     */
    @Override
    public void report(Span span) {
        if (span != null) {
            log.info(span.toString());
        } else {
            log.info("Null span");
        }
    }

    /**
     * Inherited from AsyncReporter. Noop in our case.
     */
    @Override
    public void flush() {
    }

    /**
     * Inherited from AsyncReporter. Noop in our case.
     */
    @Override
    public void close() {
    }

}
