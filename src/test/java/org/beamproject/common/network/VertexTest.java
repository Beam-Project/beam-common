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

public class VertexTest {

    private Vertex vertex;
    private Vertex other;
    private Message plaintext;
    private byte[] ciphertext;

    @Before
    public void setUp() {
        vertex = new Vertex();
        other = new Vertex();
        plaintext = new Message();
        ciphertext = new byte[0];
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMessageConstrustorOnNull() {
        vertex = new Vertex(null);
    }

    @Test
    public void testMessageConstrustorOnAssignment() {
        vertex = new Vertex(plaintext);
        assertSame(plaintext, vertex.plaintext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChildOnNull() {
        vertex.addChild(null);
    }

    @Test
    public void testAddChild() {
        assertTrue(vertex.children.isEmpty());
        vertex.addChild(other);
        assertSame(other, vertex.children.get(0));
    }

    @Test
    public void testAddChildOnSettingParent() {
        vertex.addChild(other);
        assertSame(vertex, vertex.children.get(0).getParent());
    }

    @Test
    public void testGetChildren() {
        assertTrue(vertex.getChildren().isEmpty());
        assertSame(vertex.children, vertex.getChildren());
    }

    @Test
    public void testHasChildren() {
        assertFalse(vertex.hasChildren());
        vertex.addChild(other);
        assertTrue(vertex.hasChildren());
        vertex.getChildren().remove(0);
        assertFalse(vertex.hasChildren());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParentOnNull() {
        vertex.setParent(null);
    }

    @Test
    public void testSetParent() {
        assertNull(vertex.parent);
        vertex.setParent(other);
        assertSame(other, vertex.parent);
    }

    @Test
    public void testGetParent() {
        assertNull(vertex.getParent());
        vertex.parent = other;
        assertSame(other, vertex.getParent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPlaintextOnNull() {
        vertex.setPlaintext(null);
    }

    @Test
    public void testSetPlaintext() {
        assertNull(vertex.plaintext);
        vertex.setPlaintext(plaintext);
        assertSame(plaintext, vertex.plaintext);
    }

    @Test
    public void testGetPlaintext() {
        assertNull(vertex.getPlaintext());
        vertex.plaintext = plaintext;
        assertSame(plaintext, vertex.getPlaintext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCiphertextOnNull() {
        vertex.setCiphertext(null);
    }

    @Test
    public void testSetCiphertext() {
        assertNull(vertex.ciphertext);
        vertex.setCiphertext(ciphertext);
        assertSame(ciphertext, vertex.ciphertext);
    }

    @Test
    public void testGetCiphertext() {
        assertNull(vertex.getCiphertext());
        vertex.ciphertext = ciphertext;
        assertSame(ciphertext, vertex.getCiphertext());
    }
}
