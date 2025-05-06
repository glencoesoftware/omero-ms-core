/*
 * Copyright (C) 2025 Glencoe Software, Inc. All rights reserved.
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

import io.vertx.core.MultiMap;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class OmeroRequestCtxTest {

    private MultiMap params = MultiMap.caseInsensitiveMultiMap();

    @Test
    public void testGetCheckedParams() {
        params.add("imageId", "1");
        params.add("key1", "value1");
        params.add("key2", "value2");
        Assert.assertEquals(OmeroRequestCtx.getCheckedParam(params, "imageId"), "1");
        Assert.assertEquals(OmeroRequestCtx.getCheckedParam(params, "ImageID"), "1");
        Assert.assertEquals(OmeroRequestCtx.getCheckedParam(params, "key1"), "value1");
        Assert.assertEquals(OmeroRequestCtx.getCheckedParam(params, "key2"), "value2");
    }

    @Test(
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp="Missing parameter '.*'"
    )
    public void testGetCheckedParamsMissingKey() {
        OmeroRequestCtx.getCheckedParam(params, "missingkey");
    }

    @Test
    public void testGetCheckedParamsMultipleValues() {
        params.add("imageId", "1");
        params.add("imageId", "2");
        params.add("imageId", "3");
        Assert.assertEquals(OmeroRequestCtx.getCheckedParam(params, "imageId"), "1");
    }

    @Test
    public void testGetBooleanParameter() {
        params.add("imageId", "1");
        params.add("boolean1", "true");
        params.add("boolean2", "false");
        params.add("boolean3", "TRUE");
        params.add("boolean4", "FALSE");
        Assert.assertFalse(OmeroRequestCtx.getBooleanParameter(params, "imageId"));
        Assert.assertTrue(OmeroRequestCtx.getBooleanParameter(params, "boolean1"));
        Assert.assertFalse(OmeroRequestCtx.getBooleanParameter(params, "boolean2"));
        Assert.assertTrue(OmeroRequestCtx.getBooleanParameter(params, "boolean3"));
        Assert.assertFalse(OmeroRequestCtx.getBooleanParameter(params, "boolean4"));
        Assert.assertFalse(OmeroRequestCtx.getBooleanParameter(params, "missingkey"));
    }

    @Test
    public void testGetBooleanParameterMultipleValue() {
        params.add("boolean1", "true");
        params.add("boolean1", "false");
        Assert.assertTrue(OmeroRequestCtx.getBooleanParameter(params, "boolean1"));
    }

    @DataProvider(name = "valid image IDs")
    public Object[][] validLongs() {
        return new Object[][] {
            {"1", Long.valueOf(1)},
            {String.valueOf(Integer.MAX_VALUE), Long.valueOf(Integer.MAX_VALUE)},
            {String.valueOf(Long.MAX_VALUE), Long.valueOf(Long.MAX_VALUE)}
        };
    }

    @Test(dataProvider = "valid image IDs")
    public void testGetImageIdFromString(String value, Long id) {
        Assert.assertEquals(OmeroRequestCtx.getImageIdFromString(value), id);
    }

    @DataProvider(name = "invalid image IDs")
    public Object[][] invalidLongs() {
        return new Object[][] {
            {"0"},
            {"not a number"},
            {"0.1"},
            {String.valueOf(-Integer.MAX_VALUE)}
        };
    }

    @Test(
        dataProvider = "invalid image IDs",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp="Incorrect format for imageId parameter '.*'"
    )
    public void testGetImageIdFromStringInvalid(String imageId) {
        OmeroRequestCtx.getImageIdFromString(imageId);
    }

    @DataProvider(name = "valid integers")
    public Object[][] validInts() {
        return new Object[][] {
            {"0", Integer.valueOf(0)},
            {"1", Integer.valueOf(1)},
            {String.valueOf(Integer.MAX_VALUE), Integer.valueOf(Integer.MAX_VALUE)},
            {String.valueOf(-Integer.MAX_VALUE), Integer.valueOf(-Integer.MAX_VALUE)}
        };
    }

    @Test(dataProvider = "valid integers")
    public void testGetIntegerFromString(String value, Integer integer) {
        Assert.assertEquals(OmeroRequestCtx.getIntegerFromString(value), integer);
    }

    @DataProvider(name = "invalid integers")
    public Object[][] invalidInts() {
        return new Object[][] {
            {"not a number"},
            {"0.1" },
            {"0L"},
            {String.valueOf(Long.valueOf(Integer.MAX_VALUE) + 1)}
        };
    }

    @Test(
        dataProvider = "invalid integers",
        expectedExceptions = {IllegalArgumentException.class},
        expectedExceptionsMessageRegExp="Incorrect format for parameter value '.*'"
    )
    public void testGetIntegerFromStringInvalid(String value) {
        OmeroRequestCtx.getIntegerFromString(value);
    }
}
