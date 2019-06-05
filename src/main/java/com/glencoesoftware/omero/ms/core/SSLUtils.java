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

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SSLUtils {

    /**
     * Ensure that anonymous cipher suites are enabled in the JRE.
     * OMERO 5.5 will no longer require this. See:
     * https://github.com/openmicroscopy/openmicroscopy/pull/5949/
     */
    @Deprecated
    public static void fixDisabledAlgorithms() {
        final String property = "jdk.tls.disabledAlgorithms";
        final String value = Security.getProperty(property);
        if (!(value == null || value.trim().isEmpty())) {
            final List<String> algorithms = new ArrayList<>();
            boolean isChanged = false;
            for (String algorithm : value.split(",")) {
                algorithm = algorithm.trim();
                if (algorithm.isEmpty()) {
                    /* ignore */
                } else if ("anon".equals(algorithm.toLowerCase())) {
                    isChanged = true;
                } else {
                    algorithms.add(algorithm);
                }
            }
            if (isChanged) {
                boolean needsComma = false;
                final StringBuilder newValue = new StringBuilder();
                for (final String algorithm : algorithms) {
                    if (needsComma) {
                        newValue.append(", ");
                    } else {
                        needsComma = true;
                    }
                    newValue.append(algorithm);
                }
                Security.setProperty(property, newValue.toString());
            }
        }
    }

}
