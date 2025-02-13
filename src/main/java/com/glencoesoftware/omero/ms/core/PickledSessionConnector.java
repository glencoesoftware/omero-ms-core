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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;

import com.glencoesoftware.omero.ms.core.PythonPickle.Op;

import io.kaitai.struct.ByteBufferKaitaiStream;

public class PickledSessionConnector implements IConnector {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PickledSessionConnector.class);

    public static final List<PythonPickle.Opcode> STRING_TYPE_OPCODES =
        Arrays.asList(new PythonPickle.Opcode[] {
                PythonPickle.Opcode.SHORT_BINSTRING,
                PythonPickle.Opcode.BINUNICODE,
                PythonPickle.Opcode.SHORT_BINUNICODE,
                PythonPickle.Opcode.BINUNICODE8,
                PythonPickle.Opcode.UNICODE
        });

    /**
     * All the memoized strings we have encountered before we hit the
     * connector
     */
    private Map<Integer, String> memo = new HashMap<Integer, String>();

    /**
     * Current memo offset, incremented each time a memo Opcode is encountered
     * before we hit the connector
     */
    private int memoOffset = 0;

    private Long serverId;

    private Boolean isSecure;

    private Boolean isPublic;

    private String omeroSessionKey;

    private Long userId;

    private String b64Session;

    protected PickledSessionConnector() {
    }

    public PickledSessionConnector(byte[] serialized) {
        try {
            init(serialized);
        } catch (Exception e) {
            log.error("Pickled Session: {}",
                    Base64.getEncoder().encodeToString(serialized));
            throw e;
        }
    }

    protected void init(byte[] sessionData) {
        b64Session = Base64.getEncoder().encodeToString(sessionData);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(sessionData);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        String arg = null;
        while (opIterator.hasNext()) {
            Op op = opIterator.next();

            if (STRING_TYPE_OPCODES.contains(op.code())) {
                arg = toString(op.arg());
                if ("connector".equals(arg)) {
                    // When we find the string "connector" being memoized
                    // deserialize the dictionary we know is going to be
                    // present under that key and exit.
                    deserializeConnector(opIterator);
                    return;
                }
            } else if (PythonPickle.Opcode.MEMOIZE == op.code()) {
                // If we've been asked to memoize a string, remember it,
                // otherwise just increment the offset of values we've been
                // asked to memoize.  We may need one or more of these memoized
                // strings once we hit the connector.
                if (arg != null) {
                    memo.put(memoOffset, arg);
                }
                memoOffset++;
            } else {
                arg = null;
            }
        }
    }

    private static String toString(Object string) {
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
        if (string instanceof PythonPickle.Unicodestringnl) {
            return ((PythonPickle.Unicodestringnl) string).val();
        }
        throw new IllegalArgumentException(
            "Unexpected string type: " + string.getClass());
    }

    private void deserializeConnector(Iterator<Op> opIterator) {
        while (opIterator.hasNext()) {
            Op op = opIterator.next();
            if (STRING_TYPE_OPCODES.contains(op.code())) {
                String fieldName = toString(op.arg());
                try {
                    switch (fieldName) {
                        case "is_secure":
                            isSecure = deserializeBooleanField(opIterator);
                            break;
                        case "server_id":
                            serverId = deserializeServerId(opIterator);
                            break;
                        case "user_id":
                            userId = deserializeNumberField(opIterator);
                            break;
                        case "omero_session_key":
                            omeroSessionKey =
                                deserializeStringField(opIterator, memo);
                            break;
                        case "is_public":
                            isPublic = deserializeBooleanField(opIterator);
                            break;
                        default:
                            log.warn("Unexpected field name in connector: {}",
                                     fieldName);
                    }
                } catch (Exception e) {
                    log.error("Exception while deserializing: {}", fieldName);
                    throw e;
                }
            }
            if (op.code() == PythonPickle.Opcode.SETITEMS) {
                break;
            }
        }
    }

    private Long deserializeServerId(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        Op value = opIterator.next();
        String asString = handleStringValue(value, memo, false);
        if (asString != null) {
            return Long.valueOf(asString);
        }
        Long asLong = handleNumberValue(value, false);
        if (asLong == null) {
            throw new IllegalArgumentException(
                    "Unexpected opcode for serverId: " + value.code());
        }
        return asLong;
    }

    private static void assertStoreOpCode(Iterator<Op> opIterator) {
        Op store = opIterator.next();
        if (store.code() != PythonPickle.Opcode.BINPUT
                && store.code() != PythonPickle.Opcode.LONG_BINPUT
                && store.code() != PythonPickle.Opcode.MEMOIZE
                && store.code() != PythonPickle.Opcode.PUT) {
            throw new IllegalArgumentException(
                    "Unexpected opcode: " + store.code());
        }
    }

    public static Boolean deserializeBooleanField(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        return handleBooleanValue(opIterator.next(), true);
    }

    public static Boolean handleBooleanValue(
            Op value, boolean throwOnUnexpected) {
        switch (value.code()) {
            case NEWTRUE:
                return true;
            case NEWFALSE:
                return false;
            default:
                if (throwOnUnexpected) {
                    throw new IllegalArgumentException(
                        "Unexpected opcode for boolean field: " + value.code());
                }
                return null;
        }
    }

    public static Long longFromBytes(byte[] bytesVal) {
        if (bytesVal.length == 0) {
            return Long.valueOf(0l);
        }
        byte[] revVal = Arrays.copyOf(bytesVal, bytesVal.length);
        ArrayUtils.reverse(revVal);
        BigInteger bigInt = new BigInteger(revVal);
        return Long.valueOf(bigInt.longValue());
    }

    public static Long deserializeNumberField(Iterator<Op> opIterator) {
        assertStoreOpCode(opIterator);
        return handleNumberValue(opIterator.next(), true);
    }

    public static Long handleNumberValue(Op value, boolean throwOnUnexpected) {
        switch (value.code()) {
            case BININT:
            case BININT1:
            case BININT2:
                return Long.valueOf((Integer) value.arg());
            case LONG1:
                return longFromBytes(((PythonPickle.Long1) value.arg()).val());
            default:
                if (throwOnUnexpected) {
                    throw new IllegalArgumentException(
                        "Unexpected opcode for number field: " + value.code());
                }
                return null;
        }
    }

    public static String deserializeStringField(
            Iterator<Op> opIterator, Map<Integer, String> memo) {
        assertStoreOpCode(opIterator);
        return handleStringValue(opIterator.next(), memo, true);
    }

    public static String handleStringValue(
            Op value, Map<Integer, String> memo, boolean throwOnUnexpected) {
        String v = null;
        if (STRING_TYPE_OPCODES.contains(value.code())) {
            v = toString(value.arg());
        } else if (value.code() == PythonPickle.Opcode.BINGET
                || value.code() == PythonPickle.Opcode.LONG_BINGET) {
            v = memo.get(value.arg());
            if (v == null) {
                throw new IllegalArgumentException(
                        "Failed to find key in memo: " +
                        value.arg().toString());
            }
        } else if (throwOnUnexpected){
            throw new IllegalArgumentException(
                    "Unexpected opcode for string field: " + value.code());
        }
        return v;
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
