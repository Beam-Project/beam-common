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
package org.beamproject.common.network;

import org.beamproject.common.Message;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class TransferGraphTest {

    private TransferGraph graph;
    private Message plaintext;
    private byte[] ciphertext;

    @Before
    public void setUp() {
        plaintext = new Message();
        ciphertext = new byte[0];

        graph = new TransferGraph(plaintext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        graph = new TransferGraph(null);
    }

    @Test
    public void testConstructorOnAssignment() {
        graph = new TransferGraph(plaintext);
        assertSame(plaintext, graph.root.plaintext);
    }

}
