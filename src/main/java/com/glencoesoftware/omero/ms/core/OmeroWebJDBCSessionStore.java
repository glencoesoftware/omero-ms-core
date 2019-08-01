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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Base64;

import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PyList;
import org.python.core.PyDictionary;
import org.python.core.util.StringUtil;
import org.python.modules.cPickle;

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

    /** Synchronous JDBC connection */
    private Connection syncConnection;

    /** Connection URL */
    private final String url;

    /**
     * Constructor
     * @param url full database URL with connection parameters.
     *   e.g. "jdbc:postgresql://localhost:5432/omero_database?user=fred&password=secret&ssl=true"
     * @param vertx the vertx instance for this verticle
     * @since 3.3
     */
    public OmeroWebJDBCSessionStore(String url, Vertx vertx) {
        this.url = url;
        client = JDBCClient.createShared(vertx, new JsonObject()
            .put("url", url)
            .put("driver_class", "org.postgresql.Driver")
            .put("max_pool_size", 30));
    }

    /**
     * Retrieves the current synchronous connection, creating it if it has not
     * been initialized yet.
     * @return Synchronous JDBC connection
     * @throws SQLException If there is an error retrieving the connection.
     */
    private Connection getSyncConnection() throws SQLException {
        if (syncConnection == null) {
            syncConnection = DriverManager.getConnection(url);
        }
        return syncConnection;
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
        String decodedSessionData =
            StringUtil.fromBytes(Base64.getDecoder().decode(sessionData));
        PyString pystring = Py.newString(decodedSessionData);
        PyList hash_and_data = pystring.split(":", 1);
        PyString data_str = new PyString((String) hash_and_data.get(1));
        PyDictionary djangoSession =
            (PyDictionary) cPickle.loads(data_str);
        log.debug("Session: {}", djangoSession);
        IConnector connector = (IConnector) djangoSession.get("connector");
        return connector;
    }

    /* (non-Javadoc)
     * @see com.glencoesoftware.omero.ms.core.OmeroWebSessionStore#getConnector(java.lang.String)
     */
    @Override
    public IConnector getConnector(String sessionKey) {
        PreparedStatement st = null;
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("get_connector");
        span.tag("session_key", sessionKey);
        try {
            st = getSyncConnection().prepareStatement(SELECT_SESSION_SQL);
            st.setString(1, sessionKey);
            java.sql.ResultSet rs = st.executeQuery();
            if (!rs.next()){
                // Nothing returned from query
                return null;
            } else {
                String sessionData = rs.getString(1);
                return getConnectorFromSessionData(sessionData);
            }
        } catch (SQLException e) {
            log.error("SQLException caught when trying to get connector", e);
        } finally {
            span.finish();
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                log.error("Error closing JDBC statement", e);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.glencoesoftware.omero.ms.core.OmeroWebSessionStore#getConnectorAsync(java.lang.String)
     */
    @Override
    public CompletionStage<IConnector> getConnectorAsync(String sessionKey) {
        CompletableFuture<IConnector> future =
                new CompletableFuture<IConnector>();
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("get_connector_async");
        span.tag("session_key", sessionKey);
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
        try {
            client.close();
            if (syncConnection != null) {
                syncConnection.close();
            }
        } catch (SQLException e) {
            log.error("SQLException when closing connection.", e);
        }
        return;
    }

}
