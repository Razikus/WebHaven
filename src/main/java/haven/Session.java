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

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.io.*;
import java.nio.*;
import java.lang.ref.*;

public class Session{

    public static final int PVER = 28;

    public static final int MSG_SESS = 0;
    public static final int MSG_REL = 1;
    public static final int MSG_ACK = 2;
    public static final int MSG_BEAT = 3;
    public static final int MSG_MAPREQ = 4;
    public static final int MSG_MAPDATA = 5;
    public static final int MSG_OBJDATA = 6;
    public static final int MSG_OBJACK = 7;
    public static final int MSG_CLOSE = 8;
    public static final int SESSERR_AUTH = 1;
    public static final int SESSERR_BUSY = 2;
    public static final int SESSERR_CONN = 3;
    public static final int SESSERR_PVER = 4;
    public static final int SESSERR_EXPR = 5;
    public static final int SESSERR_MESG = 6;

    @SuppressWarnings("serial")
    public static class MessageException extends RuntimeException {

        public Message msg;

        public MessageException(String text, Message msg) {
            super(text);
            this.msg = msg;
        }
    }

}
