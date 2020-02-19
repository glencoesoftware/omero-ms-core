package com.glencoesoftware.omero.ms.core;

import org.testng.annotations.*;

import com.glencoesoftware.omero.ms.core.PythonPickle.Op;

import io.kaitai.struct.ByteBufferKaitaiStream;

import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;;


public class PickledSessionConnectorTest {


String fullLongData = "gAJ9cQEoVQd1c2VyX2lkigBVBnNoYXJlc31VCWNvbm5lY3RvcmNvb"
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

String dbSessionData = "ZmNjODgyNGVhNTgzODcyODVkMWQ5ZGI1NzVhYWU1ODgxZjA1NzI4YzqAAn"
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

    @Test
    public void testUnpickling() {
        byte[] b64bytes = Base64.getDecoder().decode(dbSessionData);
        int idx = ArrayUtils.indexOf(b64bytes, (byte)':');
        byte[] sessionData = Arrays.copyOfRange(b64bytes, idx + 1, b64bytes.length);
        IConnector testConnector = new PickledSessionConnector(sessionData);
        Assert.assertEquals(testConnector.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(testConnector.getServerId(), Long.valueOf(1l));
        Assert.assertEquals(testConnector.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(testConnector.getOmeroSessionKey(), "dcf4afb6-fae9-4b71-90b0-67262e1d48c4");
        Assert.assertEquals(testConnector.getUserId(), Long.valueOf(0l));
    }

    @Test
    public void testUnpicklingLong() {
        byte[] b64bytes = Base64.getDecoder().decode(fullLongData);
        int idx = ArrayUtils.indexOf(b64bytes, (byte)':');
        byte[] sessionData = Arrays.copyOfRange(b64bytes, idx + 1, b64bytes.length);
        IConnector testConnector = new PickledSessionConnector(sessionData);
        Assert.assertEquals(testConnector.getIsSecure(), Boolean.FALSE);
        Assert.assertEquals(testConnector.getServerId(), Long.valueOf(1l));
        Assert.assertEquals(testConnector.getIsPublic(), Boolean.FALSE);
        Assert.assertEquals(testConnector.getOmeroSessionKey(), "dcf4afb6-fae9-4b71-90b0-67262e1d48c4");
        Assert.assertEquals(testConnector.getUserId(), Long.valueOf(123456672L));
    }
}
;