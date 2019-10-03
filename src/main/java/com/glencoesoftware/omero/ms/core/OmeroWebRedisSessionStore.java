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
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletionStage;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.util.StringUtil;
import org.python.modules.cPickle;
import org.slf4j.LoggerFactory;

import brave.ScopedSpan;
import brave.Tracing;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;

/**
 * A Redis backed OMERO.web session store. Based on a provided session key,
 * retrieves the Python pickled session and then utilizes Jython to unpickle
 * the current OMERO.web connector.
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class OmeroWebRedisSessionStore implements OmeroWebSessionStore {

    /**
     * Django cache session storage engine key format.
     */
    public static final String KEY_FORMAT =
            "%s:%d:django.contrib.sessions.cache%s";

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroWebRedisSessionStore.class);

    /** Redis client */
    private final RedisClient client;

    /** Redis connection */
    private final StatefulRedisConnection<byte[], byte[]> connection;

    /**
     * Default constructor.
     * @param uri Redis connection URI.
     */
    public OmeroWebRedisSessionStore(String uri) {
        client = RedisClient.create(uri);
        connection = client.connect(new ByteArrayCodec());
    }

    /* (non-Javadoc)
     * @see com.glencoesoftware.omero.ms.core.OmeroWebSessionStore#getConnector(java.lang.String, com.glencoesoftware.omero.ms.core.ConnectorHandler)
     */
    @Override
    public CompletionStage<IConnector> getConnector(String sessionKey) {
        RedisAsyncCommands<byte[], byte[]> commands = connection.async();
        String key = String.format(
                KEY_FORMAT,
                "",  // OMERO_WEB_CACHE_KEY_PREFIX
                1,  // OMERO_WEB_CACHE_VERSION
                sessionKey);
        log.debug("Retrieving OMERO.web session with key: {}", key);

        ScopedSpan span = Tracing.currentTracer().startScopedSpan("get_connector_redis_async");
        span.tag("omero_web.session_key", sessionKey);
        // Binary retrieval, get(String) includes a UTF-8 step
        RedisFuture<byte[]> future = commands.get(key.getBytes());
        return future.<IConnector>thenApply(value -> {
            try {
                if (value != null) {
                    PyDictionary djangoSession =
                            (PyDictionary) cPickle.loads(
                                    Py.newString(StringUtil.fromBytes(value)));
                    return (IConnector) djangoSession.get("connector");
                }
            } catch (Exception e) {
                log.error("Exception while unpickling connector", e);
            } finally {
                span.finish();
            }
            return null;
        }).exceptionally(t -> {
            log.error(t.getMessage(), t);
            return null;
        });
    }

    @Override
    public void close() throws IOException {
        connection.close();
        client.shutdown();
    }

}
