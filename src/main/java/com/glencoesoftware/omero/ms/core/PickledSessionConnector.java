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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.glencoesoftware.omero.ms.core.PythonPickle.Op;

import io.kaitai.struct.ByteBufferKaitaiStream;

public class PickledSessionConnector implements IConnector {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PickledSessionConnector.class);

    private static final List<PythonPickle.Opcode> STRING_TYPE_OPCODES =
        Arrays.asList(new PythonPickle.Opcode[] {
                PythonPickle.Opcode.SHORT_BINSTRING,
                PythonPickle.Opcode.BINUNICODE
        });

    private Long serverId;

    private Boolean isSecure;

    private Boolean isPublic;

    private String omeroSessionKey;

    private Long userId;

    protected PickledSessionConnector() {
    }

    public PickledSessionConnector(byte[] serialized) {
        init(serialized);
    }

    protected void init(byte[] sessionData) {
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(sessionData);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while (opIterator.hasNext()) {
            Op op = opIterator.next();
            // Loop through until we find the dictionary key "connector" being
            // set
            if (STRING_TYPE_OPCODES.contains(op.code())) {
                String key = toString(op.arg());
                if ("connector".equals(key)) {
                    deserializeConnector(opIterator);
                }
            }
        }
    }

    private String toString(Object string) {
        if (string instanceof PythonPickle.String1) {
            return new String(
                ((PythonPickle.String1) string).val(),
                StandardCharsets.US_ASCII);
        }
        if (string instanceof PythonPickle.Unicodestring1) {
            return ((PythonPickle.Unicodestring1) string).val();
        }
        if (string instanceof PythonPickle.Unicodestring4) {
            return ((PythonPickle.Unicodestring4) string).val();
        }
        if (string instanceof PythonPickle.Unicodestring8) {
            return ((PythonPickle.Unicodestring8) string).val();
        }
        throw new IllegalArgumentException(
            "Unexpected string type: " + string.getClass());
    }

    private void deserializeConnector(Iterator<Op> opIterator) {
        while (opIterator.hasNext()) {
            Op op = opIterator.next();
            if (STRING_TYPE_OPCODES.contains(op.code())) {
                String fieldName = toString(op.arg());
                switch (fieldName) {
                    case "is_secure":
                        isSecure = deserializeBooleanField(opIterator);
                        break;
                    case "server_id":
                        serverId = Long.parseLong(
                                deserializeStringField(opIterator));
                        break;
                    case "user_id":
                        userId = deserializeNumberField(opIterator);
                        break;
                    case "omero_session_key":
                        omeroSessionKey = deserializeStringField(opIterator);
                        break;
                    case "is_public":
                        isPublic = deserializeBooleanField(opIterator);
                        break;
                }
            }
        }
    }

    private void assertStoreOpCode(Iterator<Op> opIterator) {
        Op store = opIterator.next();
        if (store.code() != PythonPickle.Opcode.BINPUT) {
            throw new IllegalArgumentException(
                    "Unexpected opcode: + " + store.code());
        }
    }

    private Boolean deserializeBooleanField(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        Op value = opIterator.next();
        switch (value.code()) {
            case NEWTRUE:
                return true;
            case NEWFALSE:
                return false;
            default:
                throw new IllegalArgumentException(
                        "Unexpected opcode for boolean field: " + value.code());
        }
    }

    private Long deserializeNumberField(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        Op value = opIterator.next();
        switch (value.code()) {
            case BININT:
            case BININT1:
            case BININT2:
                return new Long((Integer) value.arg());
            case LONG1:
                return ((PythonPickle.Long1) value.arg()).longVal();
            default:
                throw new IllegalArgumentException(
                        "Unexpected opcode for number field: " + value.code());
        }
    }

    private String deserializeStringField(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        Op value = opIterator.next();
        if (STRING_TYPE_OPCODES.contains(value.code())) {
            return toString(value.arg());
        }
        throw new IllegalArgumentException(
                "Unexpected opcode for string field: " + value.code());
    }

    @Override
    public Long getServerId() {
        return serverId;
    }

    @Override
    public Boolean getIsSecure() {
        return isSecure;
    }

    @Override
    public Boolean getIsPublic() {
        return isPublic;
    }

    @Override
    public String getOmeroSessionKey() {
        return omeroSessionKey;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

}
