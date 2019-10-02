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

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import brave.ScopedSpan;
import brave.Tracing;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Verticle that provides access to a Redis backed cache.
 * @author Chris Allan <callan@glencoesoftware.com>
 */
public class RedisCacheVerticle extends AbstractVerticle {

	private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(RedisCacheVerticle.class);

    public static final String REDIS_CACHE_GET_EVENT =
            "omero.ms.core.redis_cache_get";

    public static final String REDIS_CACHE_SET_EVENT =
            "omero.ms.core.redis_cache_set";

    /** Redis client */
    private RedisClient client;

    /** Redis connection */
    private StatefulRedisConnection<byte[], byte[]> connection;

    /* (non-Javadoc)
     * @see io.vertx.core.AbstractVerticle#start()
     */
    @Override
    public void start() {
        log.info("Starting verticle");

        JsonObject config = config().getJsonObject("redis-cache");
        if (config != null) {
            String uri = config.getString("uri");
            client = RedisClient.create(uri);
            connection = client.connect(new ByteArrayCodec());
        }

        vertx.eventBus().<String>consumer(
                REDIS_CACHE_GET_EVENT, event -> {
                    get(event);
                });
        vertx.eventBus().<JsonObject>consumer(
                REDIS_CACHE_SET_EVENT, event -> {
                    set(event);
                });
    }

    @Override
    public void stop() {
        connection.close();
        client.shutdown();
    }

    /**
     * Get a key from the cache.
     */
    private void get(Message<String> message) {
        log.info("In RedisCacheVerticle:set");
        if (connection == null) {
            log.debug("Cache not enabled");
            message.reply(null);
            return;
        }

        String key = message.body();
        if (key == null) {
            message.reply(null);
            return;
        }
        log.debug("Getting cache key: {}", key);

        RedisAsyncCommands<byte[], byte[]> commands = connection.async();
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("get_redis_cache");
        span.tag("key", key);
        // Binary retrieval, get(String) includes a UTF-8 step
        RedisFuture<byte[]> future = commands.get(key.getBytes());
        future.whenComplete((v, t) -> {
            try {
                if (t != null) {
                    log.error("Exception while getting cache value", t);
                    message.fail(500, t.getMessage());
                    return;
                }
                message.reply(v);
            } finally {
                span.finish();
            }
        });
    }

    /**
     * Set a key in the cache.
     */
    private void set(Message<JsonObject> message) {
        if (connection == null) {
            log.debug("Cache not enabled");
            message.reply(null);
            return;
        }

        JsonObject data = message.body();
        String key = data.getString("key");
        byte[] value = data.getBinary("value");
        if (key == null) {
            message.reply(null);
            return;
        }
        log.debug("Setting cache key: {}", key);

        RedisAsyncCommands<byte[], byte[]> commands = connection.async();
        ScopedSpan span = Tracing.currentTracer().startScopedSpan("set_redis_cache");
        span.tag("key", key);
        // Binary retrieval, get(String) includes a UTF-8 step
        RedisFuture<String> future = commands.set(key.getBytes(), value);
        future.whenComplete((v, t) -> {
            try {
                if (t != null) {
                    log.error("Exception while setting cache value", t);
                    message.fail(500, t.getMessage());
                    return;
                }
                if (!"OK".equals(v)) {
                    message.fail(500, "Non OK reply: " + v);
                    return;
                }
                message.reply(null);
            } finally {
                span.finish();
            }
        });
    }
}
