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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.glencoesoftware.omero.ms.core.PythonPickle.Op;

import io.kaitai.struct.ByteBufferKaitaiStream;


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

    private static final String REDIS_SESSION_DATA_PY27 =
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

    private static final String REDIS_SESSION_DATA_PY3 =
        "gAN9cQAoWAgAAABjYWxsYmFja3EBfXECWA8AAABzZXJ2ZXJfc2V0dGluZ3NxA31"
        + "xBChYCwAAAGRvd25sb2FkX2FzcQV9cQZYCAAAAG1heF9zaXplcQdKAESVCHNY"
        + "BgAAAHZpZXdlcnEIfXEJKFgSAAAAaW50ZXJwb2xhdGVfcGl4ZWxzcQqIWBIAA"
        + "ABpbml0aWFsX3pvb21fbGV2ZWxxC0sAWAkAAAByb2lfbGltaXRxDE3QB3VYAg"
        + "AAAHVpcQ19cQ4oWAQAAABtZW51cQ99cRBYCAAAAGRyb3Bkb3ducRF9cRIoWAo"
        + "AAABjb2xsZWFndWVzcRN9cRQoWAUAAABsYWJlbHEVWAcAAABNZW1iZXJzcRZY"
        + "BwAAAGVuYWJsZWRxF4h1WAcAAABsZWFkZXJzcRh9cRkoWAUAAABsYWJlbHEaW"
        + "AYAAABPd25lcnNxG1gHAAAAZW5hYmxlZHEciHVYCAAAAGV2ZXJ5b25lcR19cR"
        + "4oWAUAAABsYWJlbHEfWAsAAABBbGwgTWVtYmVyc3EgWAcAAABlbmFibGVkcSG"
        + "IdXVzWAQAAAB0cmVlcSJ9cSMoWAcAAABvcnBoYW5zcSR9cSUoWAsAAABkZXNj"
        + "cmlwdGlvbnEmWIEAAABUaGlzIGlzIGEgdmlydHVhbCBjb250YWluZXIgd2l0a"
        + "CBvcnBoYW5lZCBpbWFnZXMuIFRoZXNlIGltYWdlcyBhcmUgbm90IGxpbmtlZC"
        + "Bhbnl3aGVyZS4gSnVzdCBkcmFnIHRoZW0gdG8gdGhlIHNlbGVjdGVkIGNvbnR"
        + "haW5lci5xJ1gHAAAAZW5hYmxlZHEoiFgEAAAAbmFtZXEpWA8AAABPcnBoYW5l"
        + "ZCBJbWFnZXNxKnVYCgAAAHR5cGVfb3JkZXJxK1g5AAAAdGFnc2V0LHRhZyxwc"
        + "m9qZWN0LGRhdGFzZXQsc2NyZWVuLHBsYXRlLGFjcXVpc2l0aW9uLGltYWdlcS"
        + "x1dVgFAAAAZW1haWxxLYhYEQAAAHNjcmlwdHNfdG9faWdub3JlcS5Y7gAAAC9"
        + "vbWVyby9maWd1cmVfc2NyaXB0cy9Nb3ZpZV9GaWd1cmUucHksL29tZXJvL2Zp"
        + "Z3VyZV9zY3JpcHRzL1NwbGl0X1ZpZXdfRmlndXJlLnB5LC9vbWVyby9maWd1c"
        + "mVfc2NyaXB0cy9UaHVtYm5haWxfRmlndXJlLnB5LC9vbWVyby9maWd1cmVfc2"
        + "NyaXB0cy9ST0lfU3BsaXRfRmlndXJlLnB5LC9vbWVyby9leHBvcnRfc2NyaXB"
        + "0cy9NYWtlX01vdmllLnB5LC9vbWVyby9pbXBvcnRfc2NyaXB0cy9Qb3B1bGF0"
        + "ZV9ST0kucHlxL1gDAAAAd2VicTB9cTFYBAAAAGhvc3RxMlgAAAAAcTNzWAcAA"
        + "ABicm93c2VycTR9cTVYEgAAAHRodW1iX2RlZmF1bHRfc2l6ZXE2S2BzdVgMAA"
        + "AAYWN0aXZlX2dyb3VwcTdN8QJYCQAAAGNvbm5lY3RvcnE4Y29tZXJvd2ViLmN"
        + "vbm5lY3RvcgpDb25uZWN0b3IKcTkpgXE6fXE7KFgRAAAAb21lcm9fc2Vzc2lv"
        + "bl9rZXlxPFgkAAAAZGIyYzkyOGMtNzcyYS00MjJmLTlkZDQtODBmMWI1ODYwO"
        + "Tc2cT1YBwAAAHVzZXJfaWRxPk1IDlgJAAAAaXNfc2VjdXJlcT+JWAkAAABzZX"
        + "J2ZXJfaWRxQFgBAAAAMXFBWAkAAABpc19wdWJsaWNxQol1YlgHAAAAdXNlcl9"
        + "pZHFDTUgOWAYAAABzaGFyZXNxRH1xRVgKAAAAY2FuX2NyZWF0ZXFGiHUu";

    private static String US_DEMO_TEST = "gASVZgQAAAAAAAB9lCiMCWNvbm5lY3"
            + "RvcpSMEm9tZXJvd2ViLmNvbm5lY3RvcpSMCUNvbm5lY3RvcpSTlCmBlH2"
            + "UKIwJc2VydmVyX2lklEsBjAlpc19zZWN1cmWUiYwJaXNfcHVibGljlIiM"
            + "EW9tZXJvX3Nlc3Npb25fa2V5lIwkNDc2ODc5ZTMtMWZhMy00MTQyLWIyM"
            + "jAtNTE0YmM2ZDQ5YWY3lIwHdXNlcl9pZJRN1gd1YowIY2FsbGJhY2uUfZ"
            + "SMBnNoYXJlc5R9lIwKY2FuX2NyZWF0ZZSIjA9zZXJ2ZXJfc2V0dGluZ3O"
            + "UfZQojAJ1aZR9lCiMBHRyZWWUfZQojAdvcnBoYW5zlH2UKIwEbmFtZZSM"
            + "D09ycGhhbmVkIEltYWdlc5SMB2VuYWJsZWSUiIwLZGVzY3JpcHRpb26Uj"
            + "IFUaGlzIGlzIGEgdmlydHVhbCBjb250YWluZXIgd2l0aCBvcnBoYW5lZC"
            + "BpbWFnZXMuIFRoZXNlIGltYWdlcyBhcmUgbm90IGxpbmtlZCBhbnl3aGV"
            + "yZS4gSnVzdCBkcmFnIHRoZW0gdG8gdGhlIHNlbGVjdGVkIGNvbnRhaW5l"
            + "ci6UdYwKdHlwZV9vcmRlcpSMOXRhZ3NldCx0YWcscHJvamVjdCxkYXRhc"
            + "2V0LHNjcmVlbixwbGF0ZSxhY3F1aXNpdGlvbixpbWFnZZR1jARtZW51lH"
            + "2UjAhkcm9wZG93bpR9lCiMCmNvbGxlYWd1ZXOUfZQojAVsYWJlbJSMB01"
            + "lbWJlcnOUjAdlbmFibGVklIh1jAdsZWFkZXJzlH2UKIwHZW5hYmxlZJSI"
            + "jAVsYWJlbJSMBk93bmVyc5R1jAhldmVyeW9uZZR9lCiMBWxhYmVslIwLQ"
            + "WxsIE1lbWJlcnOUjAdlbmFibGVklIh1dXN1jAdicm93c2VylH2UjBJ0aH"
            + "VtYl9kZWZhdWx0X3NpemWUS2BzjAZ2aWV3ZXKUfZQojBJpbml0aWFsX3p"
            + "vb21fbGV2ZWyUSwCMCXJvaV9saW1pdJRN0AeMEmludGVycG9sYXRlX3Bp"
            + "eGVsc5SIdYwLZG93bmxvYWRfYXOUfZSMCG1heF9zaXpllEoARJUIc4wDd"
            + "2VilH2UjARob3N0lIwAlHOMEXNjcmlwdHNfdG9faWdub3JllIzuL29tZX"
            + "JvL2ZpZ3VyZV9zY3JpcHRzL01vdmllX0ZpZ3VyZS5weSwvb21lcm8vZml"
            + "ndXJlX3NjcmlwdHMvU3BsaXRfVmlld19GaWd1cmUucHksL29tZXJvL2Zp"
            + "Z3VyZV9zY3JpcHRzL1RodW1ibmFpbF9GaWd1cmUucHksL29tZXJvL2ZpZ"
            + "3VyZV9zY3JpcHRzL1JPSV9TcGxpdF9GaWd1cmUucHksL29tZXJvL2V4cG"
            + "9ydF9zY3JpcHRzL01ha2VfTW92aWUucHksL29tZXJvL2ltcG9ydF9zY3J"
            + "pcHRzL1BvcHVsYXRlX1JPSS5weZSMBWVtYWlslIl1jAd1c2VyX2lklE3W"
            + "B3Uu";

    /**
     * Full user session which uses LONG_BINPUT opcodes and has two "user_id"
     * dictionary keys.
     */
    private static final String LONG_BINPUT =
            "gAN9cQAoWAgAAABjYWxsYmFja3EBfXECKFhiAAAAUHJvY2Vzc0NhbGxiYWNr"
            + "LzQzOWNhMTBhLTcyY2ItNDFlZS05YjkwLTk3NjQwNGUwM2U0NyAtdCAtZSAx"
            + "LjE6dGNwIC1oIDEwLjQ3Ljk4LjcgLXAgMzY5NTAgLXQgNjAwMDBxA31xBChY"
            + "CAAAAGpvYl90eXBlcQVYBgAAAHNjcmlwdHEGWAgAAABqb2JfbmFtZXEHWBMA"
            + "AABFeHBvcnQgQ2FwYWJpbGl0aWVzcQhYCgAAAHN0YXJ0X3RpbWVxCWNkYXRl"
            + "dGltZQpkYXRldGltZQpxCkMKB+YMDA8bKwaz+3ELhXEMUnENWAYAAABzdGF0"
            + "dXNxDlgIAAAAZmluaXNoZWRxD1gHAAAAcmVzdWx0c3EQfXERWAcAAABPcHRp"
            + "b25zcRJYCAAAAFRJRkYgUE5HcRNzdVhiAAAAUHJvY2Vzc0NhbGxiYWNrLzFl"
            + "NThkOWMzLTJmM2ItNDY3OS04MzExLTJmNWEwNzk4MTc1MiAtdCAtZSAxLjE6"
            + "dGNwIC1oIDEwLjQ3Ljk4LjcgLXAgMzY5NTAgLXQgNjAwMDBxFH1xFShYCAAA"
            + "AGpvYl90eXBlcRZYBgAAAHNjcmlwdHEXWAgAAABqb2JfbmFtZXEYWBMAAABF"
            + "eHBvcnQgQ2FwYWJpbGl0aWVzcRlYCgAAAHN0YXJ0X3RpbWVxGmgKQwoH5gwM"
            + "DycDAKClcRuFcRxScR1YBgAAAHN0YXR1c3EeWAgAAABmaW5pc2hlZHEfWAcA"
            + "AAByZXN1bHRzcSB9cSFYBwAAAE9wdGlvbnNxIlgIAAAAVElGRiBQTkdxI3N1"
            + "WGIAAABQcm9jZXNzQ2FsbGJhY2svMGU1N2FkMDYtNWFmMC00NDBjLTgyYWEt"
            + "ZWM2ZjI0YzY5ODA1IC10IC1lIDEuMTp0Y3AgLWggMTAuNDcuOTguNyAtcCAz"
            + "Njk1MCAtdCA2MDAwMHEkfXElKFgIAAAAam9iX3R5cGVxJlgGAAAAc2NyaXB0"
            + "cSdYCAAAAGpvYl9uYW1lcShYEwAAAEV4cG9ydCBDYXBhYmlsaXRpZXNxKVgK"
            + "AAAAc3RhcnRfdGltZXEqaApDCgfmDAwPKQQD1NhxK4VxLFJxLVgGAAAAc3Rh"
            + "dHVzcS5YCAAAAGZpbmlzaGVkcS9YBwAAAHJlc3VsdHNxMH1xMVgHAAAAT3B0"
            + "aW9uc3EyWAgAAABUSUZGIFBOR3Ezc3VYfgAAAGQ2ZThjYTc1LWEzY2QtNDU5"
            + "Yi04NzA1LTYxZGRkMTY3MjAxNi9JSGFuZGxlZDBmMTFjZWYtZDhmOS00YWJm"
            + "LWJmNDYtZDkyZjYzMDNlM2NhIC10IC1lIDEuMTp0Y3AgLWggMTAuNDcuOTgu"
            + "NyAtcCAzNjk1MCAtdCA2MDAwMHE0fXE1KFgIAAAAam9iX3R5cGVxNlgGAAAA"
            + "ZGVsZXRlcTdYCgAAAHN0YXJ0X3RpbWVxOGgKQwoH5gwMDywEBEXncTmFcTpS"
            + "cTtYBgAAAHN0YXR1c3E8WAgAAABmaW5pc2hlZHE9WAUAAABlcnJvcnE+SwBY"
            + "BwAAAGRyZXBvcnRxP05YBQAAAGR0eXBlcUBYBwAAAERhdGFzZXRxQVgHAAAA"
            + "ZGVsbWFueXFCSwpYAwAAAGRpZHFDXXFEKFgEAAAAMjQwMHFFWAQAAAAyMzk5"
            + "cUZYBAAAADIzOTRxR1gEAAAAMjM5M3FIWAQAAAAyMzk3cUlYBAAAADIzOTZx"
            + "SlgEAAAAMjQwM3FLWAQAAAAyNDAycUxYBAAAADI0MDZxTVgEAAAAMjQwNXFO"
            + "ZXVYfgAAAGQ2ZThjYTc1LWEzY2QtNDU5Yi04NzA1LTYxZGRkMTY3MjAxNi9J"
            + "SGFuZGxlY2RlODlmNTUtZGVhMy00ZGRjLWFlYzctMjY1NjUwNDhmYWY5IC10"
            + "IC1lIDEuMTp0Y3AgLWggMTAuNDcuOTguNyAtcCAzNjk1MCAtdCA2MDAwMHFP"
            + "fXFQKFgIAAAAam9iX3R5cGVxUVgGAAAAZGVsZXRlcVJYCgAAAHN0YXJ0X3Rp"
            + "bWVxU2gKQwoH5gwMDzIEB/y7cVSFcVVScVZYBgAAAHN0YXR1c3FXWAgAAABm"
            + "aW5pc2hlZHFYWAUAAABlcnJvcnFZSwBYBwAAAGRyZXBvcnRxWk5YBQAAAGR0"
            + "eXBlcVtYBwAAAERhdGFzZXRxXFgHAAAAZGVsbWFueXFdSwpYAwAAAGRpZHFe"
            + "XXFfKFgEAAAAMjM5OHFgWAQAAAAyMzg2cWFYBAAAADIzOTJxYlgEAAAAMjM4"
            + "OXFjWAQAAAAyMzk1cWRYBAAAADI0MDFxZVgEAAAAMjQwNHFmWAQAAAAyNDA3"
            + "cWdYBAAAADIzODBxaFgEAAAAMjM4M3FpZXVYfgAAAGZlZmU2Y2ZiLThlNmIt"
            + "NDM0Yy1hOGU2LTYyZTJhNTk4NjM4ZC9JSGFuZGxlZThjODc3YjMtOGRmZi00"
            + "M2E1LTg4MzAtMzg3NzY3M2I3OGEwIC10IC1lIDEuMTp0Y3AgLWggMTAuNDcu"
            + "OTguNyAtcCAzNjk1MCAtdCA2MDAwMHFqfXFrKFgIAAAAam9iX3R5cGVxbFgG"
            + "AAAAZGVsZXRlcW1YCgAAAHN0YXJ0X3RpbWVxbmgKQwoH5gwNCAUqAzEecW+F"
            + "cXBScXFYBgAAAHN0YXR1c3FyWAgAAABmaW5pc2hlZHFzWAUAAABlcnJvcnF0"
            + "SwBYBwAAAGRyZXBvcnRxdU5YBQAAAGR0eXBlcXZYBwAAAERhdGFzZXRxd1gH"
            + "AAAAZGVsbWFueXF4SzZYAwAAAGRpZHF5XXF6KFgEAAAAMjQxMHF7WAQAAAAy"
            + "NDEzcXxYBAAAADI0MTZxfVgEAAAAMjQxOXF+WAQAAAAyNDIycX9YBAAAADI0"
            + "MjVxgFgEAAAAMjQyOHGBWAQAAAAyNDMxcYJYBAAAADI0MzRxg1gEAAAAMjQz"
            + "N3GEWAQAAAAyNDQwcYVYBAAAADI0NDNxhlgEAAAAMjQ0NnGHWAQAAAAyNDQ5"
            + "cYhYBAAAADI0NTJxiVgEAAAAMjQ1NXGKWAQAAAAyNDU4cYtYBAAAADI0NjFx"
            + "jFgEAAAAMjQ2NHGNWAQAAAAyNDY3cY5YBAAAADI0NzBxj1gEAAAAMjQ3M3GQ"
            + "WAQAAAAyNDc2cZFYBAAAADI0NzlxklgEAAAAMjQ4MnGTWAQAAAAyNDg1cZRY"
            + "BAAAADI0ODhxlVgEAAAAMjQ5MXGWWAQAAAAyNDk0cZdYBAAAADI0OTdxmFgE"
            + "AAAAMjUwMHGZWAQAAAAyNTAzcZpYBAAAADI1MDZxm1gEAAAAMjUwOXGcWAQA"
            + "AAAyNTEycZ1YBAAAADI1MTVxnlgEAAAAMjUxOHGfWAQAAAAyNTIxcaBYBAAA"
            + "ADI1MjRxoVgEAAAAMjUyN3GiWAQAAAAyNTMwcaNYBAAAADI1MzNxpFgEAAAA"
            + "MjUzNnGlWAQAAAAyNTM5caZYBAAAADI1NDJxp1gEAAAAMjU0NXGoWAQAAAAy"
            + "NTQ4calYBAAAADI1NTFxqlgEAAAAMjU1NHGrWAQAAAAyNTU3caxYBAAAADI1"
            + "NjBxrVgEAAAAMjU2M3GuWAQAAAAyNTY2ca9YBAAAADI1NjlxsGV1WGIAAABQ"
            + "cm9jZXNzQ2FsbGJhY2svYzZkMzQ1N2YtZDViNy00YTAxLWI1YmQtMDM4NjM0"
            + "YmI1YzViIC10IC1lIDEuMTp0Y3AgLWggMTAuNDcuOTguNyAtcCAzNjk1MCAt"
            + "dCA2MDAwMHGxfXGyKFgIAAAAam9iX3R5cGVxs1gGAAAAc2NyaXB0cbRYCAAA"
            + "AGpvYl9uYW1lcbVYEwAAAEV4cG9ydCBDYXBhYmlsaXRpZXNxtlgKAAAAc3Rh"
            + "cnRfdGltZXG3aApDCgfmDA0IGioHYBRxuIVxuVJxulgGAAAAc3RhdHVzcbtY"
            + "CAAAAGZpbmlzaGVkcbxYBwAAAHJlc3VsdHNxvX1xvlgHAAAAT3B0aW9uc3G/"
            + "WAgAAABUSUZGIFBOR3HAc3VYYgAAAFByb2Nlc3NDYWxsYmFjay83ZTJhM2Vi"
            + "OS02YzQ3LTQzNDYtOTM1Yi01NDQxZWJiOGNiMzEgLXQgLWUgMS4xOnRjcCAt"
            + "aCAxMC40Ny45OC43IC1wIDM2OTUwIC10IDYwMDAwccF9ccIoWAgAAABqb2Jf"
            + "dHlwZXHDWAYAAABzY3JpcHRxxFgIAAAAam9iX25hbWVxxVgTAAAARXhwb3J0"
            + "IENhcGFiaWxpdGllc3HGWAoAAABzdGFydF90aW1lccdoCkMKB+YMDQgeBQ1g"
            + "gnHIhXHJUnHKWAYAAABzdGF0dXNxy1gIAAAAZmluaXNoZWRxzFgHAAAAcmVz"
            + "dWx0c3HNfXHOWAcAAABPcHRpb25zcc9YCAAAAFRJRkYgUE5HcdBzdVhiAAAA"
            + "UHJvY2Vzc0NhbGxiYWNrL2Q3YzEyODZjLTdjMWUtNDZjYy04MDUzLTFiMmVi"
            + "MWE0ZTIzZiAtdCAtZSAxLjE6dGNwIC1oIDEwLjQ3Ljk4LjcgLXAgMzY5NTAg"
            + "LXQgNjAwMDBx0X1x0ihYCAAAAGpvYl90eXBlcdNYBgAAAHNjcmlwdHHUWAgA"
            + "AABqb2JfbmFtZXHVWBMAAABFeHBvcnQgQ2FwYWJpbGl0aWVzcdZYCgAAAHN0"
            + "YXJ0X3RpbWVx12gKQwoH5gwNCB8qChWScdiFcdlScdpYBgAAAHN0YXR1c3Hb"
            + "WAgAAABmaW5pc2hlZHHcWAcAAAByZXN1bHRzcd19cd5YBwAAAE9wdGlvbnNx"
            + "31gIAAAAVElGRiBQTkdx4HN1WH4AAABkMzJiMTBhMi1mNjdlLTRlZjItOTgz"
            + "MC1lMDk3MzJiMWIwNmMvSUhhbmRsZTk2MmJkYTEzLTYzN2UtNGI2Mi1hNDMy"
            + "LWYxNzE1NDdjNjMwYiAtdCAtZSAxLjE6dGNwIC1oIDEwLjQ3Ljk4LjcgLXAg"
            + "MzY5NTAgLXQgNjAwMDBx4X1x4ihYCAAAAGpvYl90eXBlceNYBgAAAGRlbGV0"
            + "ZXHkWAoAAABzdGFydF90aW1lceVoCkMKB+YMDggeMQwTV3HmhXHnUnHoWAYA"
            + "AABzdGF0dXNx6VgIAAAAZmluaXNoZWRx6lgFAAAAZXJyb3Jx60sAWAcAAABk"
            + "cmVwb3J0cexOWAUAAABkdHlwZXHtWAcAAABEYXRhc2V0ce5YBwAAAGRlbG1h"
            + "bnlx70tQWAMAAABkaWRx8F1x8ShYBAAAADI1ODRx8lgEAAAAMjY2OHHzWAQA"
            + "AAAyNTkwcfRYBAAAADI2NzFx9VgEAAAAMjU5NnH2WAQAAAAyNjc0cfdYBAAA"
            + "ADI2ODNx+FgEAAAAMjY4NnH5WAQAAAAyNjAycfpYBAAAADI2ODlx+1gEAAAA"
            + "MjYwNXH8WAQAAAAyNjkycf1YBAAAADI2MTFx/lgEAAAAMjY5NXH/WAQAAAAy"
            + "NjE3cgABAABYBAAAADI2OThyAQEAAFgEAAAAMjYyMHICAQAAWAQAAAAyNzAx"
            + "cgMBAABYBAAAADI2MjNyBAEAAFgEAAAAMjcwNHIFAQAAWAQAAAAyNjI2cgYB"
            + "AABYBAAAADI3MDdyBwEAAFgEAAAAMjYyOXIIAQAAWAQAAAAyNzEzcgkBAABY"
            + "BAAAADI2MzVyCgEAAFgEAAAAMjcxNnILAQAAWAQAAAAyNjM4cgwBAABYBAAA"
            + "ADI3MTlyDQEAAFgEAAAAMjcyMnIOAQAAWAQAAAAyNzI1cg8BAABYBAAAADI3"
            + "MjhyEAEAAFgEAAAAMjczMXIRAQAAWAQAAAAyNzM0chIBAABYBAAAADI3Mzdy"
            + "EwEAAFgEAAAAMjc0MHIUAQAAWAQAAAAyNzQzchUBAABYBAAAADI3NDZyFgEA"
            + "AFgEAAAAMjc0OXIXAQAAWAQAAAAyNzYxchgBAABYBAAAADI3NjRyGQEAAFgE"
            + "AAAAMjc3NnIaAQAAWAQAAAAyNzgychsBAABYBAAAADI3ODhyHAEAAFgEAAAA"
            + "Mjc5N3IdAQAAWAQAAAAyODA2ch4BAABYBAAAADI4MDlyHwEAAFgEAAAAMjU3"
            + "MnIgAQAAWAQAAAAyNTc1ciEBAABYBAAAADI1NzhyIgEAAFgEAAAAMjU4MXIj"
            + "AQAAWAQAAAAyNTg3ciQBAABYBAAAADI1OTNyJQEAAFgEAAAAMjU5OXImAQAA"
            + "WAQAAAAyNjA4cicBAABYBAAAADI2MTRyKAEAAFgEAAAAMjYzMnIpAQAAWAQA"
            + "AAAyNjQxcioBAABYBAAAADI2NDRyKwEAAFgEAAAAMjY0N3IsAQAAWAQAAAAy"
            + "NjUwci0BAABYBAAAADI2NTNyLgEAAFgEAAAAMjY1NnIvAQAAWAQAAAAyNjU5"
            + "cjABAABYBAAAADI2NjJyMQEAAFgEAAAAMjY2NXIyAQAAWAQAAAAyNjc3cjMB"
            + "AABYBAAAADI2ODByNAEAAFgEAAAAMjcxMHI1AQAAWAQAAAAyNzUycjYBAABY"
            + "BAAAADI3NTVyNwEAAFgEAAAAMjc1OHI4AQAAWAQAAAAyNzY3cjkBAABYBAAA"
            + "ADI3NzByOgEAAFgEAAAAMjc3M3I7AQAAWAQAAAAyNzc5cjwBAABYBAAAADI3"
            + "ODVyPQEAAFgEAAAAMjc5MXI+AQAAWAQAAAAyNzk0cj8BAABYBAAAADI4MDBy"
            + "QAEAAFgEAAAAMjgwM3JBAQAAZXVYYgAAAFByb2Nlc3NDYWxsYmFjay81YmNm"
            + "NGU0OC1lODA0LTQwZDctYjgyOC03OGQzYzYwNGM0ODAgLXQgLWUgMS4xOnRj"
            + "cCAtaCAxMC40Ny45OC43IC1wIDM2OTUwIC10IDYwMDAwckIBAAB9ckMBAAAo"
            + "WAgAAABqb2JfdHlwZXJEAQAAWAYAAABzY3JpcHRyRQEAAFgIAAAAam9iX25h"
            + "bWVyRgEAAFgTAAAARXhwb3J0IENhcGFiaWxpdGllc3JHAQAAWAoAAABzdGFy"
            + "dF90aW1lckgBAABoCkMKB+YMDgggHAkuKHJJAQAAhXJKAQAAUnJLAQAAWAYA"
            + "AABzdGF0dXNyTAEAAFgIAAAAZmluaXNoZWRyTQEAAFgHAAAAcmVzdWx0c3JO"
            + "AQAAfXJPAQAAWAcAAABPcHRpb25zclABAABYCAAAAFRJRkYgUE5HclEBAABz"
            + "dVhiAAAAUHJvY2Vzc0NhbGxiYWNrL2EzYmRkMGZlLWYxZTUtNDY3MS1iN2Zi"
            + "LTNjNzczYTkwMWRiNSAtdCAtZSAxLjE6dGNwIC1oIDEwLjQ3Ljk4LjcgLXAg"
            + "MzY5NTAgLXQgNjAwMDByUgEAAH1yUwEAAChYCAAAAGpvYl90eXBlclQBAABY"
            + "BgAAAHNjcmlwdHJVAQAAWAgAAABqb2JfbmFtZXJWAQAAWBMAAABFeHBvcnQg"
            + "Q2FwYWJpbGl0aWVzclcBAABYCgAAAHN0YXJ0X3RpbWVyWAEAAGgKQwoH5gwO"
            + "CCAgBjrTclkBAACFcloBAABSclsBAABYBgAAAHN0YXR1c3JcAQAAWAgAAABm"
            + "aW5pc2hlZHJdAQAAWAcAAAByZXN1bHRzcl4BAAB9cl8BAABYBwAAAE9wdGlv"
            + "bnNyYAEAAFgIAAAAVElGRiBQTkdyYQEAAHN1dVgGAAAAc2hhcmVzcmIBAAB9"
            + "cmMBAABYCgAAAGNhbl9jcmVhdGVyZAEAAIhYCQAAAGNvbm5lY3RvcnJlAQAA"
            + "Y29tZXJvd2ViLmNvbm5lY3RvcgpDb25uZWN0b3IKcmYBAAApgXJnAQAAfXJo"
            + "AQAAKFgJAAAAc2VydmVyX2lkcmkBAABYAQAAADFyagEAAFgJAAAAaXNfc2Vj"
            + "dXJlcmsBAACJWAkAAABpc19wdWJsaWNybAEAAIlYEQAAAG9tZXJvX3Nlc3Np"
            + "b25fa2V5cm0BAABYJAAAADMzYTU2NjE4LTE1ZmUtNGQ4ZC1iODg2LTk0Yzc4"
            + "ZGM5YmU4OHJuAQAAWAcAAAB1c2VyX2lkcm8BAABLA3ViWA8AAABzZXJ2ZXJf"
            + "c2V0dGluZ3NycAEAAH1ycQEAAChYAgAAAHVpcnIBAAB9cnMBAAAoWAQAAAB0"
            + "cmVlcnQBAAB9cnUBAAAoWAcAAABvcnBoYW5zcnYBAAB9cncBAAAoWAQAAABu"
            + "YW1lcngBAABYDwAAAE9ycGhhbmVkIEltYWdlc3J5AQAAWAcAAABlbmFibGVk"
            + "cnoBAACIWAsAAABkZXNjcmlwdGlvbnJ7AQAAWIEAAABUaGlzIGlzIGEgdmly"
            + "dHVhbCBjb250YWluZXIgd2l0aCBvcnBoYW5lZCBpbWFnZXMuIFRoZXNlIGlt"
            + "YWdlcyBhcmUgbm90IGxpbmtlZCBhbnl3aGVyZS4gSnVzdCBkcmFnIHRoZW0g"
            + "dG8gdGhlIHNlbGVjdGVkIGNvbnRhaW5lci5yfAEAAHVYCgAAAHR5cGVfb3Jk"
            + "ZXJyfQEAAFg5AAAAdGFnc2V0LHRhZyxwcm9qZWN0LGRhdGFzZXQsc2NyZWVu"
            + "LHBsYXRlLGFjcXVpc2l0aW9uLGltYWdlcn4BAAB1WAQAAABtZW51cn8BAAB9"
            + "coABAABYCAAAAGRyb3Bkb3ducoEBAAB9coIBAAAoWAoAAABjb2xsZWFndWVz"
            + "coMBAAB9coQBAAAoWAUAAABsYWJlbHKFAQAAWAcAAABNZW1iZXJzcoYBAABY"
            + "BwAAAGVuYWJsZWRyhwEAAIh1WAcAAABsZWFkZXJzcogBAAB9cokBAAAoWAcA"
            + "AABlbmFibGVkcooBAACIWAUAAABsYWJlbHKLAQAAWAYAAABPd25lcnNyjAEA"
            + "AHVYCAAAAGV2ZXJ5b25lco0BAAB9co4BAAAoWAUAAABsYWJlbHKPAQAAWAsA"
            + "AABBbGwgTWVtYmVyc3KQAQAAWAcAAABlbmFibGVkcpEBAACIdXVzdVgHAAAA"
            + "YnJvd3NlcnKSAQAAfXKTAQAAWBIAAAB0aHVtYl9kZWZhdWx0X3NpemVylAEA"
            + "AEtgc1gGAAAAdmlld2VycpUBAAB9cpYBAAAoWBIAAABpbml0aWFsX3pvb21f"
            + "bGV2ZWxylwEAAEsAWAkAAAByb2lfbGltaXRymAEAAE3QB1gSAAAAaW50ZXJw"
            + "b2xhdGVfcGl4ZWxzcpkBAACIdVgLAAAAZG93bmxvYWRfYXNymgEAAH1ymwEA"
            + "AFgIAAAAbWF4X3NpemVynAEAAEoARJUIc1gDAAAAd2Vicp0BAAB9cp4BAABY"
            + "BAAAAGhvc3RynwEAAFgAAAAAcqABAABzWBEAAABzY3JpcHRzX3RvX2lnbm9y"
            + "ZXKhAQAAWO4AAAAvb21lcm8vZmlndXJlX3NjcmlwdHMvTW92aWVfRmlndXJl"
            + "LnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9TcGxpdF9WaWV3X0ZpZ3VyZS5w"
            + "eSwvb21lcm8vZmlndXJlX3NjcmlwdHMvVGh1bWJuYWlsX0ZpZ3VyZS5weSwv"
            + "b21lcm8vZmlndXJlX3NjcmlwdHMvUk9JX1NwbGl0X0ZpZ3VyZS5weSwvb21l"
            + "cm8vZXhwb3J0X3NjcmlwdHMvTWFrZV9Nb3ZpZS5weSwvb21lcm8vaW1wb3J0"
            + "X3NjcmlwdHMvUG9wdWxhdGVfUk9JLnB5cqIBAABYBQAAAGVtYWlscqMBAACJ"
            + "dVgHAAAAdXNlcl9pZHKkAQAASv////91Lgo=";

    /**
     * Full user session which uses a BINGET opcode to refer to a previously
     * encountered instance of the string "1" for "server_id".
     */
    private static String BINGET = "gASVcQgAAAAAAAB9lCiMCGNhbGxiYWNrlH2UKI"
            + "xhUHJvY2Vzc0NhbGxiYWNrL2RhMzljNzE5LTExMGItNDc3Ny1iZjQ4LWExO"
            + "WU4MWY1NmQ1MCAtdCAtZSAxLjE6dGNwIC1oIDEwLjAuMTMuNSAtcCA0MzY0"
            + "OSAtdCA2MDAwMJR9lCiMCGpvYl90eXBllIwGc2NyaXB0lIwIam9iX25hbWW"
            + "UjBNFeHBvcnQgQ2FwYWJpbGl0aWVzlIwKc3RhcnRfdGltZZSMCGRhdGV0aW"
            + "1llIwIZGF0ZXRpbWWUk5RDCgfnBhMUIRICKeKUhZRSlIwGc3RhdHVzlIwIZ"
            + "mluaXNoZWSUjAdyZXN1bHRzlH2UjAdPcHRpb25zlIwIVElGRiBQTkeUc3WM"
            + "fTViYWI2Y2RhLTJkNzgtNDE1ZS1hMDgwLTk1ODU4NjgwMTY5Ni9JSGFuZGx"
            + "lMjI1YzBhZWYtOWM0NS00NDdhLWE2NjEtM2YzY2RiNWRhNjkzIC10IC1lID"
            + "EuMTp0Y3AgLWggMTAuMC4xMy41IC1wIDQzNjQ5IC10IDYwMDAwlH2UKIwIa"
            + "m9iX3R5cGWUjAZkZWxldGWUjApzdGFydF90aW1llGgMQwoH5wYTFQQEC1Fz"
            + "lIWUUpSMBnN0YXR1c5SMCGZpbmlzaGVklIwFZXJyb3KUSwCMB2RyZXBvcnS"
            + "UTowFZHR5cGWUjAdEYXRhc2V0lIwHZGVsbWFueZSJjANkaWSUjAExlHWMfT"
            + "ViYWI2Y2RhLTJkNzgtNDE1ZS1hMDgwLTk1ODU4NjgwMTY5Ni9JSGFuZGxlM"
            + "TRlZTRiYzYtYjAwMy00NjY5LTgwY2EtZDIxMDJmZDU1MTVmIC10IC1lIDEu"
            + "MTp0Y3AgLWggMTAuMC4xMy41IC1wIDQzNjQ5IC10IDYwMDAwlH2UKIwIam9"
            + "iX3R5cGWUjAZkZWxldGWUjApzdGFydF90aW1llGgMQwoH5wYTFQQJB5w4lI"
            + "WUUpSMBnN0YXR1c5SMCGZpbmlzaGVklIwFZXJyb3KUSwCMB2RyZXBvcnSUT"
            + "owFZHR5cGWUjAdEYXRhc2V0lIwHZGVsbWFueZSJjANkaWSUjAEylHWMfTVi"
            + "YWI2Y2RhLTJkNzgtNDE1ZS1hMDgwLTk1ODU4NjgwMTY5Ni9JSGFuZGxlODc"
            + "zYTU5NjUtMTU3Ni00MjVjLTg4NTQtYjA2ZGQ0OTkxZDA0IC10IC1lIDEuMT"
            + "p0Y3AgLWggMTAuMC4xMy41IC1wIDQzNjQ5IC10IDYwMDAwlH2UKIwIam9iX"
            + "3R5cGWUjAZkZWxldGWUjApzdGFydF90aW1llGgMQwoH5wYTFQQPA8z8lIWU"
            + "UpSMBnN0YXR1c5SMCGZpbmlzaGVklIwFZXJyb3KUSwCMB2RyZXBvcnSUTow"
            + "FZHR5cGWUjAdEYXRhc2V0lIwHZGVsbWFueZSJjANkaWSUjAEzlHV1jAZzaG"
            + "FyZXOUfZSMCmNhbl9jcmVhdGWUiIwMYWN0aXZlX2dyb3VwlEsDjAljb25uZ"
            + "WN0b3KUfZQojAlzZXJ2ZXJfaWSUaCaMCWlzX3NlY3VyZZSJjAlpc19wdWJs"
            + "aWOUiYwRb21lcm9fc2Vzc2lvbl9rZXmUjCQyOTA3MTAxMy1hOGM0LTQ2NWE"
            + "tOWJkZi0yNjc0MjIyY2E4OWKUjAd1c2VyX2lklEsCdYwPc2VydmVyX3NldH"
            + "RpbmdzlH2UKIwCdWmUfZQojAR0cmVllH2UKIwHb3JwaGFuc5R9lCiMBG5hb"
            + "WWUjA9PcnBoYW5lZCBJbWFnZXOUjAdlbmFibGVklIiMC2Rlc2NyaXB0aW9u"
            + "lIyBVGhpcyBpcyBhIHZpcnR1YWwgY29udGFpbmVyIHdpdGggb3JwaGFuZWQ"
            + "gaW1hZ2VzLiBUaGVzZSBpbWFnZXMgYXJlIG5vdCBsaW5rZWQgYW55d2hlcm"
            + "UuIEp1c3QgZHJhZyB0aGVtIHRvIHRoZSBzZWxlY3RlZCBjb250YWluZXIul"
            + "HWMCnR5cGVfb3JkZXKUjDl0YWdzZXQsdGFnLHByb2plY3QsZGF0YXNldCxz"
            + "Y3JlZW4scGxhdGUsYWNxdWlzaXRpb24saW1hZ2WUdYwEbWVudZR9lIwIZHJ"
            + "vcGRvd26UfZQojApjb2xsZWFndWVzlH2UKIwFbGFiZWyUjAdNZW1iZXJzlI"
            + "wHZW5hYmxlZJSIdYwHbGVhZGVyc5R9lCiMB2VuYWJsZWSUiIwFbGFiZWyUj"
            + "AZPd25lcnOUdYwIZXZlcnlvbmWUfZQojAVsYWJlbJSMC0FsbCBNZW1iZXJz"
            + "lIwHZW5hYmxlZJSIdXVzdYwHYnJvd3NlcpR9lIwSdGh1bWJfZGVmYXVsdF9"
            + "zaXpllEtgc4wGdmlld2VylH2UKIwSaW5pdGlhbF96b29tX2xldmVslEsAjA"
            + "lyb2lfbGltaXSUTdAHjBJpbnRlcnBvbGF0ZV9waXhlbHOUiHWMC2Rvd25sb"
            + "2FkX2FzlH2UjAhtYXhfc2l6ZZRKAESVCHOMA3dlYpR9lIwEaG9zdJSMAJRz"
            + "jBFzY3JpcHRzX3RvX2lnbm9yZZSM7i9vbWVyby9maWd1cmVfc2NyaXB0cy9"
            + "Nb3ZpZV9GaWd1cmUucHksL29tZXJvL2ZpZ3VyZV9zY3JpcHRzL1NwbGl0X1"
            + "ZpZXdfRmlndXJlLnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9UaHVtYm5ha"
            + "WxfRmlndXJlLnB5LC9vbWVyby9maWd1cmVfc2NyaXB0cy9ST0lfU3BsaXRf"
            + "RmlndXJlLnB5LC9vbWVyby9leHBvcnRfc2NyaXB0cy9NYWtlX01vdmllLnB"
            + "5LC9vbWVyby9pbXBvcnRfc2NyaXB0cy9Qb3B1bGF0ZV9ST0kucHmUjAVlbW"
            + "FpbJSJdYwHdXNlcl9pZJRLAnUu";

    private static String LONG_NONZERO = "gAJ9cQCKAtIESwFzLg==";
    //python2.7>>> base64.b64encode(pickle.dumps({1234L:1},2))

    private static String LONG_ZERO = "gAJ9cQCKAEsBcy4=";
    //python2.7>>> base64.b64encode(pickle.dumps({0L:1},2))

    private static String BININT1_TEST = "gAJ9cQBL/0sCcy4=";
    //python2.7>>> base64.b64encode(pickle.dumps({255:2},2))

    private static String BININT2_TEST = "gAJ9cQBN//9LAnMu";
    //python2.7>>> base64.b64encode(pickle.dumps({65535:2},2))

    private static String BININT_TEST = "gAJ9cQBKAAABAEsCcy4=";
    //python2.7>>> base64.b64encode(pickle.dumps({65536:2},2))

    private static String SHORT_BINUNICODE_TEST = "gASVEgAAAAAAAAB9lIwEdGVzdJSMBGRpY3SUcy4=";
    //python3>>> base64.b64encode(pickle.dumps({"test":"dict"},4))

    private static String PY3_STRING_TEST = "gAJ9cQBYBAAAAHRlc3RxAVgEAAAAZGljdHECcy4=";
    //python3>>> base64.b64encode(pickle.dumps({"test": "dict"},2))

    private static String UNICODE_TEST = "KGRwMApWdGVzdApwMQpWZGljdApwMgpzLg==";
    //python2.7>>> base64.b64encode(pickle.dumps({u'test':u'dict'}, 0))

    private static String BINUNICODE8_TEST = "gASVEgAAAAAAAAB9lI0EAAAAAAAAAHRlc3SUjARkaWN0lHMu";
    //python3>>> base64.b64encode(b'\x80\x04\x95\x12\x00\x00\x00\x00\x00\x00\x00}\x94\x8d\x04\x00\x00\x00\x00\x00\x00\x00test\x94\x8c\x04dict\x94s.')

    private static String STRING_USER_ID = "gASVeAAAAAAAAAB9lCiMBmJhbmFuYZRLFo"
            + "wJY29ubmVjdG9ylH2UKIwJc2VydmVyX2lklIwCLTGUjAlpc19zZWN1cmWUiIwHd"
            + "XNlcl9pZJSMAzEyM5SMEW9tZXJvX3Nlc3Npb25fa2V5lIwGYWJjMTIzlIwJaXNf"
            + "cHVibGljlIl1dS4=";

    private static String MEMOIZED_SESSION_ID = "gASVfwAAAAAAAAB9lCiMBmJhbmFuY"
            + "ZRLFowJY29ubmVjdG9ylH2UKIwGa2V5ZHVwlIwGYWJjMTIzlIwJc2VydmVyX2lk"
            + "lIwCLTGUjAlpc19zZWN1cmWUiIwHdXNlcl9pZJRLe4wRb21lcm9fc2Vzc2lvbl9"
            + "rZXmUaAWMCWlzX3B1YmxpY5SJdXUu";

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

    private void assertRedisSessionData(IConnector v) {
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "db2c928c-772a-422f-9dd4-80f1b5860976");
        Assert.assertEquals(v.getUserId(), Long.valueOf(3656L));
    }

    @Test
    public void testUnpicklingRedisPy27() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(REDIS_SESSION_DATA_PY27));
        assertRedisSessionData(v);
    }

    @Test
    public void testUnpicklingRedisPy3() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(REDIS_SESSION_DATA_PY3));
        assertRedisSessionData(v);
    }

    @Test
    public void testUnpicklingRedisWithNumericalServerId() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(US_DEMO_TEST));
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.TRUE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "476879e3-1fa3-4142-b220-514bc6d49af7");
        Assert.assertEquals(v.getUserId(), Long.valueOf(2006l));
    }

    @Test
    public void testUnpicklingRedisWithLongBinput() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(LONG_BINPUT));
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "33a56618-15fe-4d8d-b886-94c78dc9be88");
        Assert.assertEquals(v.getUserId(), Long.valueOf(3L));
    }

    @Test
    public void testUnpicklingRedisWithBinget() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(BINGET));
        Assert.assertEquals(v.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(v.getServerId(), Long.valueOf(1L));
        Assert.assertEquals(v.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(
            v.getOmeroSessionKey(), "29071013-a8c4-465a-9bdf-2674222ca89b");
        Assert.assertEquals(v.getUserId(), Long.valueOf(2L));
    }

    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testUnpicklingRedisWithStringUserId() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(STRING_USER_ID));
    }

    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testUnpicklingRedisWithSessionIdMemoizedInConnector() {
        IConnector v = new PickledSessionConnector(
                Base64.getDecoder().decode(MEMOIZED_SESSION_ID));
    }

    @Test
    public void nonzeroLongTest() {
        byte[] data = Base64.getDecoder().decode(LONG_NONZERO);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Assert.assertEquals(
                Long.valueOf(1234L),
                PickledSessionConnector.deserializeNumberField(opIterator));
    }

    @Test
    public void zeroLongTest() {
        byte[] data = Base64.getDecoder().decode(LONG_ZERO);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Assert.assertEquals(
                Long.valueOf(0L),
                PickledSessionConnector.deserializeNumberField(opIterator));
    }

    @Test
    public void binInt1Test() {
        byte[] data = Base64.getDecoder().decode(BININT1_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Assert.assertEquals(
                Long.valueOf(255L),
                PickledSessionConnector.deserializeNumberField(opIterator));
    }

    @Test
    public void binInt2Test() {
        byte[] data = Base64.getDecoder().decode(BININT2_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Assert.assertEquals(
                Long.valueOf(65535L),
                PickledSessionConnector.deserializeNumberField(opIterator));
    }

    @Test
    public void binIntTest() {
        byte[] data = Base64.getDecoder().decode(BININT_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Assert.assertEquals(
                Long.valueOf(65536L),
                PickledSessionConnector.deserializeNumberField(opIterator));
    }

    @Test
    public void py3StringTest() {
        byte[] data = Base64.getDecoder().decode(PY3_STRING_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Map<Integer, String> memo = new HashMap<Integer, String>();
        Assert.assertEquals(
                "test", PickledSessionConnector.deserializeStringField(
                        opIterator, memo));
    }

    @Test
    public void shortBinUnicodeTest() {
        byte[] data = Base64.getDecoder().decode(SHORT_BINUNICODE_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Map<Integer, String> memo = new HashMap<Integer, String>();
        Assert.assertEquals(
                "test", PickledSessionConnector.deserializeStringField(
                        opIterator, memo));
    }

    @Test
    public void UnicodeStringtTest() {
        byte[] data = Base64.getDecoder().decode(UNICODE_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.DICT) {}
        Map<Integer, String> memo = new HashMap<Integer, String>();
        Assert.assertEquals(
                "test", PickledSessionConnector.deserializeStringField(
                        opIterator, memo));
    }

    @Test
    public void Unicode8StringtTest() {
        byte[] data = Base64.getDecoder().decode(BINUNICODE8_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Map<Integer, String> memo = new HashMap<Integer, String>();
        Assert.assertEquals(
                "test", PickledSessionConnector.deserializeStringField(
                        opIterator, memo));
    }

    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testBooleanUnexpectedOpCode() {
        byte[] data = Base64.getDecoder().decode(UNICODE_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.DICT) {}
        PickledSessionConnector.deserializeBooleanField((opIterator));
    }

    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testNumberUnexpectedOpCode() {
        byte[] data = Base64.getDecoder().decode(UNICODE_TEST);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.DICT) {}
        PickledSessionConnector.deserializeNumberField((opIterator));
    }

    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testStringUnexpectedOpCode() {
        byte[] data = Base64.getDecoder().decode(LONG_NONZERO);
        ByteBufferKaitaiStream bbks = new ByteBufferKaitaiStream(data);
        PythonPickle pickleData = new PythonPickle(bbks);
        List<Op> ops = pickleData.ops();
        Iterator<Op> opIterator = ops.iterator();
        while(opIterator.next().code() != PythonPickle.Opcode.EMPTY_DICT) {}
        Map<Integer, String> memo = new HashMap<Integer, String>();
        PickledSessionConnector.deserializeStringField(opIterator, memo);
    }
}
