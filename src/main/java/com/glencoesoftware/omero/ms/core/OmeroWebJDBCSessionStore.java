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

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.lang.ArrayUtils;

import org.slf4j.LoggerFactory;

import brave.ScopedSpan;
import brave.Tracing;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


/**
 * An OMERO.web session store.
 * @author Kevin Kozlowski <kevin@glencoesoftware.com>
 *
 */
public class OmeroWebJDBCSessionStore implements OmeroWebSessionStore{

    /** SQL statement for retrieving session key from django_session table*/
    private static final String SELECT_SESSION_SQL =
        "select session_data from django_session where session_key = ?";

    /** logger */
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroWebJDBCSessionStore.class);

    /** Vertx Async JDBC client */
    private JDBCClient client;

    /**
     * Constructor
     * @param url full database URL with connection parameters.
     *   e.g. "jdbc:postgresql://localhost:5432/omero_database?user=fred&password=secret&ssl=true"
     * @param vertx the vertx instance for this verticle
     * @since 3.3
     */
    public OmeroWebJDBCSessionStore(String url, Vertx vertx) {
        client = JDBCClient.createShared(vertx, new JsonObject()
            .put("url", url)
            .put("driver_class", "org.postgresql.Driver")
            .put("max_pool_size", 30));
    }

    /**
     * Gets the <code>omeroweb.connector.Connerctor</code>
     * object from the raw database text
     * @param sessionData The session_data text from the database
     * @return The connector from the session data
     * @since 3.3
     */
    private IConnector getConnectorFromSessionData(String sessionData) {
        if (sessionData == null) {
            return null;
        }
        return new JDBCPickledSessionConnector(sessionData);
    }

    /* (non-Javadoc)
     * @see com.glencoesoftware.omero.ms.core.OmeroWebSessionStore#getConnector(java.lang.String)
     */
    @Override
    public CompletionStage<IConnector> getConnector(String sessionKey) {
        CompletableFuture<IConnector> future =
                new CompletableFuture<IConnector>();
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("get_connector_jdbc_async");
        span.tag("omero_web.session_key", sessionKey);
        client.getConnection(result -> {
            if (result.failed()) {
                span.finish();
                future.completeExceptionally(result.cause());
                return;
            }

            try (final SQLConnection connection = result.result()) {
                connection.queryWithParams(SELECT_SESSION_SQL,
                        new JsonArray().add(sessionKey), innerResult -> {
                    if (innerResult.failed()) {
                        future.completeExceptionally(innerResult.cause());
                        return;
                    }

                    IConnector connector = null;
                    List<JsonArray> results = innerResult.result().getResults();
                    if (!results.isEmpty()){
                        // Take the first column, first row
                        JsonArray record = results.get(0);
                        String sessionData = record.getString(0);
                        connector = getConnectorFromSessionData(sessionData);
                    }
                    future.complete(connector);
                });
            } finally {
                span.finish();
            }
        });
        return future;
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        client.close();
    }

}
