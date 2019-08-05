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

import java.io.Closeable;
import org.slf4j.LoggerFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import brave.ScopedSpan;
import brave.Tracing;
import omero.ServerError;


/**
 * OMERO session aware request which provides session joining and lifecycle
 * management for executing OMERO client actions.
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class OmeroRequest implements Closeable {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroRequest.class);

    /** OMERO session key. */
    private final String omeroSessionKey;

    /** OMERO client. */
    private final omero.client client;

    /**
     * Default constructor. The session is joined once the instance has been
     * constructed. {@link #execute(OmeroRequestHandler)} can be called as
     * soon as required.
     * @param host OMERO server host.
     * @param port OMERO server port.
     * @param omeroSessionKey OMERO session key which will be used to join an
     * active session.
     * @throws PermissionDeniedException If there was a permissions related
     * error joining the session. Should be considered an authentication
     * failure.
     * @throws CannotCreateSessionException If there was an unknown error
     * creating the session. Should be considered an authentication failure.
     * @throws ServerError If there is a server error joining an active
     * session.
     */
    public OmeroRequest(String host, int port, String omeroSessionKey)
            throws PermissionDeniedException, CannotCreateSessionException,
                ServerError {
        log.debug("Connecting to the server: {}, {}, {}",
                host, port, omeroSessionKey);
        this.omeroSessionKey = omeroSessionKey;
        this.client = new omero.client(host, port);
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("join_omero_session");
        span.tag("omero.session_key", omeroSessionKey);
        try {
            client.joinSession(omeroSessionKey).detachOnDestroy();
            log.debug("Successfully joined session: {}", omeroSessionKey);
        } finally {
            span.finish();
        }
    }

    /**
     * Execute OMERO client actions with a valid session.
     * @param handler The handler that is to actually execute the one or more
     * client actions.
     * @throws ServerError If there is a server error executing one or more of
     * the client actions.
     */
    public <T> T execute(OmeroRequestHandler<T> handler)
            throws ServerError {
        return handler.execute(client);
    }

    /**
     * Responsible for closing the OMERO session after one or more executions
     * have completed.
     */
    @Override
    public void close() {
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("close_omero_session");
        span.tag("session_id", client.getSessionId());
        try {
            client.closeSession();
            log.debug("Successfully closed session: {}", omeroSessionKey);
        } catch (Exception e) {
            log.error("Exception while closing session: {}", omeroSessionKey);
        } finally {
            span.finish();
        }
    }

}
