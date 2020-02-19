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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.glencoesoftware.omero.ms.core.PythonPickle.Op;

import io.kaitai.struct.ByteBufferKaitaiStream;

public class PickledSessionConnector implements IConnector {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(OmeroWebRedisSessionStore.class);

    private Long serverId;

    private Boolean isSecure;

    private Boolean isPublic;

    private String omeroSessionKey;

    Long userId;

    public PickledSessionConnector(byte[] sessionData) {
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(sessionData);
        System.out.println(bbks.toString());
        PythonPickle pickleData = new PythonPickle(bbks);
        ArrayList<Op> ops = pickleData.ops();

        for(int i = 0; i < ops.size(); i++) {
            Op op = ops.get(i);
            if(op.code() == PythonPickle.Opcode.SHORT_BINSTRING) {
                PythonPickle.String1 str1 = (PythonPickle.String1) op.arg();
                String javaStr = new String(str1.val(), StandardCharsets.US_ASCII);
                if (javaStr.contentEquals("connector")) {
                    for(int j = i; j < ops.size(); j++) {
                        op = ops.get(j);
                        if(op.code() == PythonPickle.Opcode.SETITEMS) {
                            break;
                        }
                        if(op.code() == PythonPickle.Opcode.SHORT_BINSTRING) {
                            PythonPickle.String1 innerStr1 = (PythonPickle.String1) op.arg();
                            String innerJavaStr = new String(innerStr1.val(), StandardCharsets.US_ASCII);
                            if (innerJavaStr.equals("is_secure")) {
                                Op valueOp = ops.get(j+2);
                                if (valueOp.code() == PythonPickle.Opcode.NEWTRUE)
                                    isSecure = true;
                                else if (valueOp.code().equals(PythonPickle.Opcode.NEWFALSE))
                                    isSecure = false;
                                else {
                                    log.error("Incorrect code for value of is_secure: " + valueOp.code().toString());
                                }
                            }
                            else if (innerJavaStr.equals("server_id")) {
                                Op valueOp = ops.get(j+2);
                                if (valueOp.code() == PythonPickle.Opcode.BINUNICODE) {
                                    PythonPickle.Unicodestring4 idStr = (PythonPickle.Unicodestring4) valueOp.arg();
                                    serverId = Long.parseLong(idStr.val());
                                    System.out.println("Setting ServerID: " + serverId.toString());
                                } else {
                                    log.error("Incorrect code for value of server_id: " + valueOp.code().toString());
                                }
                            }
                            else if (innerJavaStr.equals("user_id")) {
                                Op valueOp = ops.get(j+2);
                                if (valueOp.code() == PythonPickle.Opcode.LONG1) {
                                    PythonPickle.Long1 userIdL1 = (PythonPickle.Long1) valueOp.arg();
                                    userId = userIdL1.longVal();
                                } else {
                                    log.error("Incorrect code for value of user_id: " + valueOp.code().toString());
                                }
                            }
                            else if (innerJavaStr.equals("omero_session_key")) {
                                Op valueOp = ops.get(j+2);
                                if (valueOp.code() == PythonPickle.Opcode.SHORT_BINSTRING) {
                                    PythonPickle.String1 sessionStr1 = (PythonPickle.String1) valueOp.arg();
                                    omeroSessionKey = new String(sessionStr1.val(), StandardCharsets.US_ASCII);
                                    System.out.println("Sessing session key: " + omeroSessionKey);
                                } else {
                                    log.error("Incorrect code for value of omero_session_key: " + valueOp.code().toString());
                                }
                            }
                            else if (innerJavaStr.equals("is_public")) {
                                Op valueOp = ops.get(j+2);
                                if (valueOp.code() == PythonPickle.Opcode.NEWTRUE)
                                    isPublic = true;
                                else if (valueOp.code().equals(PythonPickle.Opcode.NEWFALSE))
                                    isPublic = false;
                                else {
                                    log.error("Incorrect code for value of is_secure: " + valueOp.code().toString());
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
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
