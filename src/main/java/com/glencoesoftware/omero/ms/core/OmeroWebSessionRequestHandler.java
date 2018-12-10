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

import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

/**
 * OMERO.web session Vert.x web request handler.
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class OmeroWebSessionRequestHandler implements Handler<RoutingContext>{

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroWebSessionRequestHandler.class);

    /** OMERO.web session store. */
    private final OmeroWebSessionStore sessionStore;

    /** Microservice wide configuration. */
    private final JsonObject config;

    /** Vertx instance */
    private final Vertx vertx;

    private final String handlerSynchronicity;

    /**
     * Default constructor.
     * @param config Microservice wide configuration.
     * @param sessionStore OMERO.web session store implementation.
     */
    public OmeroWebSessionRequestHandler(
            JsonObject config, OmeroWebSessionStore sessionStore, Vertx vertx) {
        this.config = config;
        this.vertx = vertx;
        handlerSynchronicity = config.getJsonObject("session-store")
            .getString("synchronicity");

        this.sessionStore = sessionStore;
    }


    private void handleConnector(IConnector connector, RoutingContext event) {
        if (connector == null) {
            log.error("Connector was null!");
            event.response().setStatusCode(403);
            event.response().end();
            return;
        }
        event.put("omero.session_key", connector.getOmeroSessionKey());
        event.next();
    }

    /**
     * Handler implementation whose responsibility is to make the OMERO session
     * key into the routing context via the <code>omero.session_key</code>
     * key of the <code>event</code> context dictionary.
     * @see Handler#handle(Object)
     */
    @Override
    public void handle(RoutingContext event) {
        if (handlerSynchronicity.equals("sync")) {
            handleSync(event);
        }
        else if (handlerSynchronicity.equals("async")) {
            handleAsync(event);
        }
    }


    public void handleAsync(RoutingContext event) {
        // First try to get the OMERO session key from the
        // `X-OMERO-Session-Key` request header.
        String sessionKey =
                event.request().headers().get("X-OMERO-Session-Key");
        if (sessionKey != null) {
            log.debug("OMERO session key from header: {}", sessionKey);
            event.put("omero.session_key", sessionKey);
            event.next();
            return;
        }

        // Next see if it was provided via the `bsession` URL parameter
        sessionKey = event.request().getParam("bsession");
        if (sessionKey != null) {
            log.debug(
                "OMERO session key from 'bsession' URL parameter: {}",
                sessionKey
            );
            event.put("omero.session_key", sessionKey);
            event.next();
            return;
        }

        // Finally, check if we have a standard OMERO.web cookie available to
        // retrieve the session key from.
        JsonObject omeroWeb = config.getJsonObject(
                "omero.web", new JsonObject());
        String name = omeroWeb.getString("session_cookie_name", "sessionid");
        Cookie cookie = event.getCookie(name);
        if (cookie == null) {
            event.response().setStatusCode(403);
            event.response().end();
            return;
        }
        final String djangoSessionKey = cookie.getValue();
        log.debug("OMERO.web session key: {}", djangoSessionKey);
        sessionStore.getConnectorAsync(djangoSessionKey)
            .whenComplete((connector, throwable) -> {
            if (throwable != null) {
                log.error("Exception retrieving connector", throwable);
            }
            handleConnector(connector, event);
        });
    }


    public void handleSync(RoutingContext event) {
        // First try to get the OMERO session key from the
        // `X-OMERO-Session-Key` request header.
        String sessionKey =
                event.request().headers().get("X-OMERO-Session-Key");
        if (sessionKey != null) {
            log.debug("OMERO session key from header: {}", sessionKey);
            event.put("omero.session_key", sessionKey);
            event.next();
            return;
        }

        // Next see if it was provided via the `bsession` URL parameter
        sessionKey = event.request().getParam("bsession");
        if (sessionKey != null) {
            log.debug(
                "OMERO session key from 'bsession' URL parameter: {}",
                sessionKey
            );
            event.put("omero.session_key", sessionKey);
            event.next();
            return;
        }

        // Finally, check if we have a standard OMERO.web cookie available to
        // retrieve the session key from.
        JsonObject omeroWeb = config.getJsonObject(
                "omero.web", new JsonObject());
        String name = omeroWeb.getString("session_cookie_name", "sessionid");
        Cookie cookie = event.getCookie(name);
        if (cookie == null) {
            event.response().setStatusCode(403);
            event.response().end();
            return;
        }
        final String djangoSessionKey = cookie.getValue();
        log.debug("OMERO.web session key: {}", djangoSessionKey);
        vertx.executeBlocking(future -> {
            IConnector connector = sessionStore.getConnector(djangoSessionKey);
            future.complete(connector);
        }, res-> {
            handleConnector((IConnector) res.result(), event);
        });
    }
}
