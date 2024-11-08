/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.function.*;
import java.io.*;
import java.net.*;

public class Http {
    public static final String USER_AGENT = useragent();
    public static final SslHelper ssl = sslconf();

    private static String useragent() {
	StringBuilder buf = new StringBuilder();
	buf.append("Haven/1.0");
	if(!Config.confid.equals(""))
	    buf.append(" (" + Config.confid + ")");
	String jv = Utils.getprop("java.version", null);
	if((jv != null) && !jv.equals(""))
	    buf.append(" Java/" + jv);
	return(buf.toString());
    }

    private static SslHelper sslconf() {
	SslHelper ssl = new SslHelper();
	String cert = "-----BEGIN CERTIFICATE-----\n" +
			"MIIDoDCCAoigAwIBAgIJAI+UbBvj7yxFMA0GCSqGSIb3DQEBCwUAMGQxCzAJBgNV\n" +
			"BAYTAlNFMRIwEAYDVQQHDAlTdG9ja2hvbG0xEjAQBgNVBAoMCURvbGRhMjAwMDEV\n" +
			"MBMGA1UECwwMRG9sZGEyMDAwIENBMRYwFAYDVQQDDA1kb2xkYTIwMDAuY29tMCAX\n" +
			"DTE4MDMyMDAyMzY1MVoYDzIxMTgwMzIwMDIzNjUxWjBkMQswCQYDVQQGEwJTRTES\n" +
			"MBAGA1UEBwwJU3RvY2tob2xtMRIwEAYDVQQKDAlEb2xkYTIwMDAxFTATBgNVBAsM\n" +
			"DERvbGRhMjAwMCBDQTEWMBQGA1UEAwwNZG9sZGEyMDAwLmNvbTCCASIwDQYJKoZI\n" +
			"hvcNAQEBBQADggEPADCCAQoCggEBALcR58LSbDBt/BXxNPU2VuqnR5rxc7HmWRkX\n" +
			"2DL2lGXobkv3ZeXNZNoCPIYRa5fbkGaq1YiF+iZeYE2W/r1tAHLvqbcmAsunbwPZ\n" +
			"lppR1sQI6cVVqzmb5+xyTComtSeEDFGB6KRLwKAY+uQ5hdBfxXJbPGtcB5zKf0DM\n" +
			"66o1O/1om7SS5aJdzOSAcFZ+xhgEu5u1MIrF5ml+9lrqgRlt/Nxi0YazsQU0QhFz\n" +
			"zz0kjM9+v7LlG17mIES9cTXbGT2Bq2VSxEMpcisduYn0Y7x324SV2j7Q6jQU14W5\n" +
			"/KpckJECrtK1axU3yCvhgkX4uceXMrG4JnUy6W8j/felYK7m/S0CAwEAAaNTMFEw\n" +
			"HQYDVR0OBBYEFIiV6Te6GpzlIadP70f/vDfs3y7QMB8GA1UdIwQYMBaAFIiV6Te6\n" +
			"GpzlIadP70f/vDfs3y7QMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQAD\n" +
			"ggEBAHNyXuM4SBcpkNhWAdlQ3vilL83Hh7koOsUA7lstLKphzJdIzXdIPKd6KDod\n" +
			"Ke6FiLPYS6AcV0bZgARAYqD3QCE+/55WBIAZACqj71S9Gh50q76pjh4X985HZRAI\n" +
			"FvuiVIjtoGCWfTay7xmNSo1zmC4xwosYZAjTF7kaqB8hMosSgNlenb9E9eNR20rt\n" +
			"iTiiKlK8YQeFLJZpWyAu+I/dutQLHMGULWIp3c6MuxKhrPM4TqzEvn1Yrxgx3Vsn\n" +
			"WfJ4xWHVpiKuzaS3LXy9dJdvoajBxj3KKXMbAqxBE9AXIXW8FvrSnLfz35+FpjgF\n" +
			"KegWmxtknxdlSjeGSRKuMpzUBs0=\n" +
			"-----END CERTIFICATE-----";


	InputStream toIS = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
	try {

	    ssl.trust(toIS);
	} catch(java.security.cert.CertificateException e) {
	    throw(new Error("Invalid built-in certificate", e));
	} catch(IOException e) {
	    throw(new Error(e));
	}
	ssl.ignoreName();
	return(ssl);
    }

    public static URLConnection open(URL url) throws IOException {
	URLConnection conn;
	if(url.getProtocol().equals("https"))
	    conn = ssl.connect(url);
	else
	    conn = url.openConnection();
	conn.addRequestProperty("User-Agent", USER_AGENT);
	return(conn);
    }

    public static InputStream fetch(URL url, Consumer<URLConnection> init) throws IOException {
	RetryingInputStream ret = new RetryingInputStream() {
		protected InputStream create() throws IOException {
		    URLConnection c = open(url);
		    if(init != null)
			init.accept(c);
		    return(c.getInputStream());
		}
	    };
	ret.check();
	return(ret);
    }

    public static InputStream fetch(URL url) throws IOException {
	return(fetch(url, null));
    }
}
