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

import org.junit.Test;
import static org.junit.Assert.*;

public class ArraysTest {

    private final byte[] input1 = "hello".getBytes();
    private final byte[] input2 = " ".getBytes();
    private final byte[] input3 = "world".getBytes();
    private byte[] merge;

    @Test(expected = IllegalArgumentException.class)
    public void testMergeArraysOnNulls() {
        Arrays.mergeArrays(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeArraysOnNull() {
        Arrays.mergeArrays(null);
    }

    @Test
    public void testMergeArrays() {
        merge = Arrays.mergeArrays(input1, input2, input3);
        assertArrayEquals("hello world".getBytes(), merge);
    }

    @Test
    public void testMergeArraysOnNoArguments() {
        merge = Arrays.mergeArrays();
        assertEquals(0, merge.length);
    }

    @Test
    public void testMergeArraysOnOneArgument() {
        merge = Arrays.mergeArrays(input1);
        assertArrayEquals("hello".getBytes(), merge);
    }

    @Test
    public void testMergeArraysOnEmptyArgument() {
        merge = Arrays.mergeArrays(new byte[0]);
        assertEquals(0, merge.length);
    }

}
