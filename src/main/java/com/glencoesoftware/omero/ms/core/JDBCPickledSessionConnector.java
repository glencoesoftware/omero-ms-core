/*
 * Copyright (C) 2020 Glencoe Software, Inc. All rights reserved.
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

import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

public class JDBCPickledSessionConnector extends PickledSessionConnector {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(JDBCPickledSessionConnector.class);

    public JDBCPickledSessionConnector(String serialized) {
        byte[] b64bytes = Base64.getDecoder().decode(serialized);
        int idx = ArrayUtils.indexOf(b64bytes, (byte)':');
        byte[] sessionData = Arrays.copyOfRange(
                b64bytes, idx + 1, b64bytes.length);
        init(sessionData);
    }

}
