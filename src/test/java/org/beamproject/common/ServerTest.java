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
package org.beamproject.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ServerTest {

    private Server server;
    private URL url;
    private KeyPair keyPair;

    @Before
    public void setUp() throws MalformedURLException {
        server = Server.generate();
        url = server.url;
        keyPair = server.keyPair;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        server = new Server(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUrl() {
        server = new Server(null, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullKeyPair() {
        server = new Server(url, null);
    }

    @Test
    public void testConstructor() {
        server = new Server(url, keyPair);
        assertSame(url, server.url);
        assertSame(keyPair, server.keyPair);
    }

    @Test
    public void testGetUrl() {
        assertSame(url, server.url);
    }

    @Test
    public void testGenerate() {
        server = Server.generate();
        assertEquals("http://example.com", server.url.toString());
        assertNotNull(server.getPublicKey().getEncoded());
        assertNotNull(server.getPrivateKey().getEncoded());
    }

}
