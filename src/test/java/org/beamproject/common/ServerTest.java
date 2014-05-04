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
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ServerTest {

    private Server server;
    private URL url;
    private KeyPair keyPair;
    private String urlAsBase58;

    @Before
    public void setUp() throws MalformedURLException {
        server = Server.generate();
        url = server.url;
        keyPair = server.keyPair;
        urlAsBase58 = Base58.encode(url.toString().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNulls() {
        server = new Server(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNullUrl() {
        server = new Server(null, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNullKeyPair() {
        server = new Server(url, null);
    }

    @Test
    public void testUrlKeyPairConstructor() {
        server = new Server(url, keyPair);
        assertSame(url, server.url);
        assertSame(keyPair, server.keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnNull() {
        server = new Server(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnEmptyString() {
        server = new Server("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnInvalidAddress() {
        server = new Server("beam:longTextLongTexturl=asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnIllegalChracters() {
        server = new Server("beam\0\":)(*&^%$#@?url~~~!!@äåé®äåé®þäå®éáßðg\0asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingPublicKey() {
        server = new Server("beam:?url=" + urlAsBase58);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingUrl() {
        server = new Server("beam:" + server.getPublicKeyAsBase58());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMalformedPublicKey() {
        String malformedPublicKey = server.getPublicKeyAsBase58().replaceAll("a", "X");
        server = new Server("beam:" + malformedPublicKey + "?url=" + urlAsBase58);
    }

    @Test
    public void testAddressContstructor() {
        Server created = new Server("beam:" + server.getPublicKeyAsBase58() + "?url=" + urlAsBase58);
        assertEquals(server.getAddress(), created.getAddress());
    }

    @Test
    public void testGetUrl() {
        assertSame(url, server.url);
    }

    @Test
    public void testGetAddress() {
        String address = server.getAddress();
        assertTrue(address.matches("beam:[0-9a-zA-Z]{164}\\?url=[0-9a-zA-Z]{25}"));
    }

    @Test
    public void testEquals() {
        Server other = null;
        assertFalse(server.equals(null));
        assertFalse(server.equals(other));

        other = Server.generate();
        assertFalse(server.equals(other));

        other.keyPair = EccKeyPairGenerator.fromPublicKey(server.getPublicKeyAsBytes());
        assertFalse(server.equals(other));

        other.url = server.url;
        other.keyPair = EccKeyPairGenerator.fromBothKeys(server.getPublicKeyAsBytes(), server.getPrivateKeyAsBytes());
        assertTrue(server.equals(other));

        other.keyPair = server.keyPair;
        assertTrue(server.equals(other));

        other = new Server(server.url, keyPair);
        assertTrue(server.equals(other));

        other.url = null;
        assertFalse(server.equals(other));

        server.keyPair = EccKeyPairGenerator.fromPublicKey(keyPair.getPublic().getEncoded());
        other.url = server.url;
        other.keyPair = keyPair;
        assertFalse(server.equals(other));

        other.keyPair = EccKeyPairGenerator.fromPublicKey(keyPair.getPublic().getEncoded());
        assertTrue(server.equals(other));

        assertTrue(server.equals(server));
    }

    @Test
    public void testGenerate() {
        server = Server.generate();
        assertEquals("http://example.com", server.url.toString());
        assertNotNull(server.getPublicKey().getEncoded());
        assertNotNull(server.getPrivateKey().getEncoded());
    }

}
