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

import java.util.Base64;

import org.testng.Assert;
import org.testng.annotations.Test;


public class PickledSessionConnectorTest {

    private static final String FULL_LONG_DATA =
        "gAJ9cQEoVQd1c2VyX2lkigBVBnNoYXJlc31VCWNvbm5lY3RvcmNvb"
        + "WVyb3dlYi5jb25uZWN0b3IKQ29ubmVjdG9yCnECKYFxA31xBChVCWlzX3NlY3VyZX"
        + "EFiVUJc2VydmVyX2lkcQZYAQAAADFVB3VzZXJfaWRxB4oEoMxbB1URb21lcm9fc2V"
        + "zc2lvbl9rZXlxCFUkZGNmNGFmYjYtZmFlOS00YjcxLTkwYjAtNjcyNjJlMWQ0OGM0"
        + "cQlVCWlzX3B1YmxpY3EKiXViVQhjYWxsYmFja31VD3NlcnZlcl9zZXR0aW5nc31xC"
        + "yhVA3dlYn1xDFUEaG9zdFUAc1UGdmlld2VyfXENKFUSaW50ZXJwb2xhdGVfcGl4ZW"
        + "xziFUJcm9pX2xpbWl0TdAHVRJpbml0aWFsX3pvb21fbGV2ZWxLAHVVC2Rvd25sb2F"
        + "kX2FzfXEOVQhtYXhfc2l6ZUoARJUIc1UCdWl9cQ8oVQRtZW51fXEQVQhkcm9wZG93"
        + "bn1xEShVCmNvbGxlYWd1ZXN9cRIoVQdlbmFibGVkiFUFbGFiZWxVB01lbWJlcnN1V"
        + "QhldmVyeW9uZX1xEyhVB2VuYWJsZWSIVQVsYWJlbFULQWxsIE1lbWJlcnN1VQdsZW"
        + "FkZXJzfXEUKFUHZW5hYmxlZIhVBWxhYmVsVQZPd25lcnN1dXNVBHRyZWV9cRUoVQd"
        + "vcnBoYW5zfXEWKFUHZW5hYmxlZIhVBG5hbWVVD09ycGhhbmVkIEltYWdlc1ULZGVz"
        + "Y3JpcHRpb25VgVRoaXMgaXMgYSB2aXJ0dWFsIGNvbnRhaW5lciB3aXRoIG9ycGhhb"
        + "mVkIGltYWdlcy4gVGhlc2UgaW1hZ2VzIGFyZSBub3QgbGlua2VkIGFueXdoZXJlLi"
        + "BKdXN0IGRyYWcgdGhlbSB0byB0aGUgc2VsZWN0ZWQgY29udGFpbmVyLnVVCnR5cGV"
        + "fb3JkZXJVOXRhZ3NldCx0YWcscHJvamVjdCxkYXRhc2V0LHNjcmVlbixwbGF0ZSxh"
        + "Y3F1aXNpdGlvbixpbWFnZXV1VRFzY3JpcHRzX3RvX2lnbm9yZVXuL29tZXJvL2ZpZ"
        + "3VyZV9zY3JpcHRzL01vdmllX0ZpZ3VyZS5weSwvb21lcm8vZmlndXJlX3NjcmlwdH"
        + "MvU3BsaXRfVmlld19GaWd1cmUucHksL29tZXJvL2ZpZ3VyZV9zY3JpcHRzL1RodW1"
        + "ibmFpbF9GaWd1cmUucHksL29tZXJvL2ZpZ3VyZV9zY3JpcHRzL1JPSV9TcGxpdF9G"
        + "aWd1cmUucHksL29tZXJvL2V4cG9ydF9zY3JpcHRzL01ha2VfTW92aWUucHksL29tZ"
        + "XJvL2ltcG9ydF9zY3JpcHRzL1BvcHVsYXRlX1JPSS5weVUFZW1haWyJVQdicm93c2"
        + "VyfXEXVRJ0aHVtYl9kZWZhdWx0X3NpemVLYHN1VQpjYW5fY3JlYXRliHUu";

    private static final String DB_SESSION_DATA =
        "ZmNjODgyNGVhNTgzODcyODVkMWQ5ZGI1NzVhYWU1ODgxZjA1NzI4YzqAAn"
        + "1xAShVB3VzZXJfaWSKAFUGc2hhcmVzfVUJY29ubmVjdG9yY29tZXJvd2ViLmNvbm5"
        + "lY3RvcgpDb25uZWN0b3IKcQIpgXEDfXEEKFUJaXNfc2VjdXJlcQWJVQlzZXJ2ZXJf"
        + "aWRxBlgBAAAAMVUHdXNlcl9pZHEHigBVEW9tZXJvX3Nlc3Npb25fa2V5cQhVJGRjZ"
        + "jRhZmI2LWZhZTktNGI3MS05MGIwLTY3MjYyZTFkNDhjNHEJVQlpc19wdWJsaWNxCo"
        + "l1YlUIY2FsbGJhY2t9VQ9zZXJ2ZXJfc2V0dGluZ3N9cQsoVQN3ZWJ9cQxVBGhvc3R"
        + "VAHNVBnZpZXdlcn1xDShVEmludGVycG9sYXRlX3BpeGVsc4hVCXJvaV9saW1pdE3Q"
        + "B1USaW5pdGlhbF96b29tX2xldmVsSwB1VQtkb3dubG9hZF9hc31xDlUIbWF4X3Npe"
        + "mVKAESVCHNVAnVpfXEPKFUEbWVudX1xEFUIZHJvcGRvd259cREoVQpjb2xsZWFndW"
        + "VzfXESKFUHZW5hYmxlZIhVBWxhYmVsVQdNZW1iZXJzdVUIZXZlcnlvbmV9cRMoVQd"
        + "lbmFibGVkiFUFbGFiZWxVC0FsbCBNZW1iZXJzdVUHbGVhZGVyc31xFChVB2VuYWJs"
        + "ZWSIVQVsYWJlbFUGT3duZXJzdXVzVQR0cmVlfXEVKFUHb3JwaGFuc31xFihVB2VuY"
        + "WJsZWSIVQRuYW1lVQ9PcnBoYW5lZCBJbWFnZXNVC2Rlc2NyaXB0aW9uVYFUaGlzIG"
        + "lzIGEgdmlydHVhbCBjb250YWluZXIgd2l0aCBvcnBoYW5lZCBpbWFnZXMuIFRoZXN"
        + "lIGltYWdlcyBhcmUgbm90IGxpbmtlZCBhbnl3aGVyZS4gSnVzdCBkcmFnIHRoZW0g"
        + "dG8gdGhlIHNlbGVjdGVkIGNvbnRhaW5lci51VQp0eXBlX29yZGVyVTl0YWdzZXQsd"
        + "GFnLHByb2plY3QsZGF0YXNldCxzY3JlZW4scGxhdGUsYWNxdWlzaXRpb24saW1hZ2"
        + "V1dVURc2NyaXB0c190b19pZ25vcmVV7i9vbWVyby9maWd1cmVfc2NyaXB0cy9Nb3Z"
        + "pZV9GaWd1cmUucHksL29tZXJvL2ZpZ3VyZV9zY3JpcHRzL1NwbGl0X1ZpZXdfRmln"
        + "dXJlLnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9UaHVtYm5haWxfRmlndXJlLnB5L"
        + "C9vbWVyby9maWd1cmVfc2NyaXB0cy9ST0lfU3BsaXRfRmlndXJlLnB5LC9vbWVyby"
        + "9leHBvcnRfc2NyaXB0cy9NYWtlX01vdmllLnB5LC9vbWVyby9pbXBvcnRfc2NyaXB"
        + "0cy9Qb3B1bGF0ZV9ST0kucHlVBWVtYWlsiVUHYnJvd3Nlcn1xF1USdGh1bWJfZGVm"
        + "YXVsdF9zaXplS2BzdVUKY2FuX2NyZWF0ZYh1Lg==";

    private static final String REDIS_SESSION_DATA =
        "gAJ9cQEoVQd1c2VyX2lkigJIDlUMYWN0aXZlX2dyb3VwTfECVQljb25uZWN0b3J"
        + "jb21lcm93ZWIuY29ubmVjdG9yCkNvbm5lY3RvcgpxAimBcQN9cQQoVQlpc19z"
        + "ZWN1cmVxBYlVCXNlcnZlcl9pZHEGWAEAAAAxVQd1c2VyX2lkcQeKAkgOVRFvb"
        + "WVyb19zZXNzaW9uX2tleXEIVSRkYjJjOTI4Yy03NzJhLTQyMmYtOWRkNC04MG"
        + "YxYjU4NjA5NzZxCVUJaXNfcHVibGljcQqJdWJVCGNhbGxiYWNrfVUPc2VydmV"
        + "yX3NldHRpbmdzfXELKFUDd2VifXEMVQRob3N0VQBzVQZ2aWV3ZXJ9cQ0oVRJp"
        + "bnRlcnBvbGF0ZV9waXhlbHOIVQlyb2lfbGltaXRN0AdVEmluaXRpYWxfem9vb"
        + "V9sZXZlbEsAdVURc2NyaXB0c190b19pZ25vcmVV7i9vbWVyby9maWd1cmVfc2"
        + "NyaXB0cy9Nb3ZpZV9GaWd1cmUucHksL29tZXJvL2ZpZ3VyZV9zY3JpcHRzL1N"
        + "wbGl0X1ZpZXdfRmlndXJlLnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9UaHVt"
        + "Ym5haWxfRmlndXJlLnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9ST0lfU3Bsa"
        + "XRfRmlndXJlLnB5LC9vbWVyby9leHBvcnRfc2NyaXB0cy9NYWtlX01vdmllLn"
        + "B5LC9vbWVyby9pbXBvcnRfc2NyaXB0cy9Qb3B1bGF0ZV9ST0kucHlVAnVpfXE"
        + "OKFUEbWVudX1xD1UIZHJvcGRvd259cRAoVQpjb2xsZWFndWVzfXERKFUHZW5h"
        + "YmxlZIhVBWxhYmVsVQdNZW1iZXJzdVUIZXZlcnlvbmV9cRIoVQdlbmFibGVki"
        + "FUFbGFiZWxVC0FsbCBNZW1iZXJzdVUHbGVhZGVyc31xEyhVB2VuYWJsZWSIVQ"
        + "VsYWJlbFUGT3duZXJzdXVzVQR0cmVlfXEUKFUHb3JwaGFuc31xFShVB2VuYWJ"
        + "sZWSIVQRuYW1lVQ9PcnBoYW5lZCBJbWFnZXNVC2Rlc2NyaXB0aW9uVYFUaGlz"
        + "IGlzIGEgdmlydHVhbCBjb250YWluZXIgd2l0aCBvcnBoYW5lZCBpbWFnZXMuI"
        + "FRoZXNlIGltYWdlcyBhcmUgbm90IGxpbmtlZCBhbnl3aGVyZS4gSnVzdCBkcm"
        + "FnIHRoZW0gdG8gdGhlIHNlbGVjdGVkIGNvbnRhaW5lci51VQp0eXBlX29yZGV"
        + "yVTl0YWdzZXQsdGFnLHByb2plY3QsZGF0YXNldCxzY3JlZW4scGxhdGUsYWNx"
        + "dWlzaXRpb24saW1hZ2V1dVULZG93bmxvYWRfYXN9cRZVCG1heF9zaXplSgBEl"
        + "QhzVQVlbWFpbIhVB2Jyb3dzZXJ9cRdVEnRodW1iX2RlZmF1bHRfc2l6ZUtgc3"
        + "VVBnNoYXJlc31VCmNhbl9jcmVhdGWIdS4=";

    @Test
    public void testUnpicklingJDBC() {
        IConnector v = new JDBCPickledSessionConnector(DB_SESSION_DATA);
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "dcf4afb6-fae9-4b71-90b0-67262e1d48c4");
        Assert.assertEquals(v.getUserId(), Long.valueOf(0l));
    }

    @Test
    public void testUnpicklingJDBCLong() {
        IConnector v = new JDBCPickledSessionConnector(FULL_LONG_DATA);
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "dcf4afb6-fae9-4b71-90b0-67262e1d48c4");
        Assert.assertEquals(v.getUserId(), Long.valueOf(123456672L));
    }

    @Test
    public void testUnpicklingRedis() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(REDIS_SESSION_DATA));
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "db2c928c-772a-422f-9dd4-80f1b5860976");
        Assert.assertEquals(v.getUserId(), Long.valueOf(3656L));
    }
}
