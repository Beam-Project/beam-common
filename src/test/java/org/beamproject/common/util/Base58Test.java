/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-common.
 *
 * beam-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class Base58Test {

    private String encoded;
    private byte[] decoded;

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeOnNull() {
        encoded = Base58.encode(null);
    }

    @Test
    public void testEncodeOnEmptyArray() {
        encoded = Base58.encode(new byte[0]);
        assertNotNull(encoded);
        assertEquals(encoded, "");
    }

    @Test
    public void testEncode() {
        String tester = "hello world";
        String expected = "StV1DL6CwTryKyV";
        encoded = Base58.encode(tester.getBytes());
        assertEquals(expected, encoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeOnNull() {
        Base58.decode(null);
    }

    @Test
    public void testDecodeOnEmptyString() {
        decoded = Base58.decode("");
        assertNotNull(decoded);
        assertArrayEquals(decoded, new byte[0]);
    }

    @Test
    public void testDecode() {
        String tester = "StV1DL6CwTryKyV";
        String expected = "hello world";
        decoded = Base58.decode(tester);
        assertArrayEquals(expected.getBytes(), decoded);
    }

}
