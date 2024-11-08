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

import java.util.*;
import java.util.function.Consumer;
import java.lang.annotation.*;
import java.lang.reflect.*;


public class OCache{
    public static final int OD_REM = 0;
    public static final int OD_MOVE = 1;
    public static final int OD_RES = 2;
    public static final int OD_LINBEG = 3;
    public static final int OD_LINSTEP = 4;
    public static final int OD_SPEECH = 5;
    public static final int OD_COMPOSE = 6;
    public static final int OD_ZOFF = 7;
    public static final int OD_LUMIN = 8;
    public static final int OD_AVATAR = 9;
    public static final int OD_FOLLOW = 10;
    public static final int OD_HOMING = 11;
    public static final int OD_OVERLAY = 12;
    /* public static final int OD_AUTH = 13; -- Removed */
    public static final int OD_HEALTH = 14;
    /* public static final int OD_BUDDY = 15; -- Removed */
    public static final int OD_CMPPOSE = 16;
    public static final int OD_CMPMOD = 17;
    public static final int OD_CMPEQU = 18;
    public static final int OD_ICON = 19;
    public static final int OD_RESATTR = 20;
    public static final int OD_END = 255;
    public static final int[] compodmap = {OD_REM, OD_RESATTR, OD_FOLLOW, OD_MOVE, OD_RES, OD_LINBEG, OD_LINSTEP, OD_HOMING};
    public static final Coord2d posres = Coord2d.of(0x1.0p-10, 0x1.0p-10).mul(11, 11);



    public static class ObjDelta {
        public int fl, frame;
        public int initframe;
        public long id;
        public final List<AttrDelta> attrs = new LinkedList<>();
        public boolean rem = false;

        public ObjDelta(int fl, long id, int frame) {
            this.fl = fl;
            this.id = id;
            this.frame = frame;
        }

        public ObjDelta() {
        }

        @Override
        public String toString() {
            return "ObjDelta{" +
                    "fl=" + fl +
                    ", frame=" + frame +
                    ", initframe=" + initframe +
                    ", id=" + id +
                    ", attrs=" + attrs +
                    ", rem=" + rem +
                    '}';
        }
    }

    public static class AttrDelta extends PMessage {
        public boolean old;

        public AttrDelta(ObjDelta od, int type, Message blob, int len) {
            super(type, blob, len);
            this.old = ((od.fl & 4) != 0);
        }

        public AttrDelta(AttrDelta from) {
            super(from);
            this.old = from.old;
        }

        @Override
        public String toString() {
            return "AttrDelta{" +
                    "old=" + old +
                    ", type=" + type +
                    ", rh=" + rh +
                    ", rt=" + rt +
                    ", wh=" + wh +
                    ", wt=" + wt +
                    ", rbuf=" + Arrays.toString(rbuf) +
                    ", wbuf=" + Arrays.toString(wbuf) +
                    '}';
        }

        public AttrDelta clone() {
            return (new AttrDelta(this));
        }
    }

}
