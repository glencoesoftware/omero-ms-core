/*
 * Copyright (C) 2024 Glencoe Software, Inc. All rights reserved.
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

import java.util.List;

import ome.api.IQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;

/**
 * No-op server side internal query service implementation to shim certain
 * services for which it is required but not actually used. 
 */
public class NoopQueryImpl implements IQuery {

    public NoopQueryImpl() {
    }

    @Override
    public <T extends IObject> T get(Class<T> klass, long id)
            throws ValidationException {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> T find(Class<T> klass, long id) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> List<T> findAll(Class<T> klass, Filter filter) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> T findByExample(T example)
            throws ApiUsageException {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> List<T> findAllByExample(T example,
            Filter filter) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> T findByString(Class<T> klass, String field,
            String value) throws ApiUsageException {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> List<T> findAllByString(Class<T> klass,
            String field, String stringValue, boolean caseSensitive,
            Filter filter) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> T findByQuery(String queryName,
            Parameters parameters) throws ValidationException {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> List<T> findAllByQuery(String queryName,
            Parameters parameters) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> List<T> findAllByFullText(Class<T> type,
            String query, Parameters parameters) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public List<Object[]> projection(String query, Parameters parameters) {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public <T extends IObject> T refresh(T iObject) throws ApiUsageException {
        throw new IllegalArgumentException("Not implemented");
    }

}
