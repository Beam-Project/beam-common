/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharsTest {

    @Test
    public void testCharsToBytesAndBytesToChars() {
        assertMatching("");
        assertMatching("asdf;lkjasdfqoweiruqpwoeiruzxm,.cvnz,x.cmvnasdklfjdgflkjsdqweoiruq");
        assertMatching("234-iq345io1q234Q@W#$%^WE%YUE?RTYAsd.';gzsl'd;ll      a;sdklfj34-[po");
        assertMatching("Äuröpäischi Umlüüüt");
        assertMatching("!!@#$ASDF324354321");
        assertMatching("-**++679/234q53");
    }

    private void assertMatching(String text) {
        char[] chars = text.toCharArray();

        byte[] bytes = Chars.utfCharsToBytes(chars);
        char[] restored = Chars.bytesToUtfChars(bytes);

        assertArrayEquals(chars, restored);
    }

}
