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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.msgpack.MessagePack;

public class ServerTest {

    private final MessagePack PACK = new MessagePack();
    private Map<String, byte[]> addressMap;
    private String address;
    private Server server;
    private URL url;
    private KeyPair keyPair;

    @Before
    public void setUp() throws MalformedURLException, IOException {
        server = Server.generate();
        url = server.url;
        keyPair = server.keyPair;
        addressMap = new LinkedHashMap<>();

        createAddress();
    }

    private void createAddress() throws IOException {
        addressMap.put(Server.ADDRESS_PUBLIC_KEY_IDENTIFIER, server.getPublicKeyAsBytes());
        addressMap.put(Server.ADDRESS_URL_IDENTIFIER, url.toString().getBytes());
        packAddressMap();
    }

    private void packAddressMap() {
        try {
            address = "beam:" + Base58.encode(PACK.write(addressMap));
        } catch (IOException ex) {
            throw new RuntimeException("Could not pack.");
        }
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
        assertNotNull(server.pack);
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
        addressMap.remove(Server.ADDRESS_PUBLIC_KEY_IDENTIFIER);
        packAddressMap();
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingUrl() {
        addressMap.remove(Server.ADDRESS_URL_IDENTIFIER);
        packAddressMap();
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMalformedPublicKey() {
        address = address.substring(0, 20) + address.substring(30);
        server = new Server(address);
    }

    @Test
    public void testAddressContstructor() {
        server = new Server(address);
        assertEquals(address, server.getAddress());
    }

    @Test
    public void testGetUrl() {
        assertSame(url, server.url);
    }

    @Test
    public void testGetAddress() {
        String extractedAddress = server.getAddress();
        assertTrue(extractedAddress.matches("beam:[0-9a-zA-Z]{204}"));
        assertEquals(address, extractedAddress);
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
        addressMap.remove(Server.ADDRESS_URL_IDENTIFIER);
        packAddressMap();

        server = Server.generate();
        assertEquals("http://example.com", server.url.toString());
        assertNotNull(server.getPublicKey().getEncoded());
        assertNotNull(server.getPrivateKey().getEncoded());
    }

}
