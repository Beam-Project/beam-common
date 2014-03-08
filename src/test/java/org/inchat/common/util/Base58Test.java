/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-common.
 *
 * inchat-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.common.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base58Test {

    String encoded;
    byte[] decoded;

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeOnNull() {
        Base58.encode(null);
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
        Base64.decode(null);
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