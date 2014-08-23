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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.beamproject.common.Server.ADDRESS_HTTP_URL_IDENTIFIER;
import static org.beamproject.common.Server.ADDRESS_MQTT_HOST_IDENTIFIER;
import static org.beamproject.common.Server.ADDRESS_MQTT_PORT_IDENTIFIER;
import static org.beamproject.common.Server.ADDRESS_PUBLIC_KEY_IDENTIFIER;
import static org.beamproject.common.Server.MQTT_DEFAULT_PORT;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromBothKeys;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
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
    private InetSocketAddress mqttAddress;
    private URL httpUrl;
    private KeyPair keyPair;

    @Before
    public void setUp() throws MalformedURLException, IOException {
        server = Server.generate();
        mqttAddress = server.getMqttAddress();
        httpUrl = server.httpUrl;
        keyPair = server.keyPair;
        addressMap = new LinkedHashMap<>();

        createAddress();
    }

    private void createAddress() throws IOException {
        addressMap.put(ADDRESS_PUBLIC_KEY_IDENTIFIER, server.getPublicKeyAsBytes());
        addressMap.put(ADDRESS_HTTP_URL_IDENTIFIER, httpUrl.toString().getBytes());
        addressMap.put(ADDRESS_MQTT_HOST_IDENTIFIER, mqttAddress.getHostString().getBytes());
        addressMap.put(ADDRESS_MQTT_PORT_IDENTIFIER, String.valueOf(mqttAddress.getPort()).getBytes());
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
        server = new Server(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNullMqttAddress() {
        server = new Server(null, httpUrl, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNullHttpUrl() {
        server = new Server(mqttAddress, null, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlKeyPairConstructorOnNullKeyPair() {
        server = new Server(mqttAddress, httpUrl, null);
    }

    @Test
    public void testUrlKeyPairConstructor() {
        server = new Server(mqttAddress, httpUrl, keyPair);
        assertSame(mqttAddress, server.mqttAddress);
        assertSame(httpUrl, server.httpUrl);
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
    public void testAddressContstructorOnMissingMqttHost() {
        addressMap.remove(ADDRESS_MQTT_HOST_IDENTIFIER);
        packAddressMap();
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingMqttPort() {
        addressMap.remove(ADDRESS_MQTT_PORT_IDENTIFIER);
        packAddressMap();
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingHttpUrl() {
        addressMap.remove(ADDRESS_HTTP_URL_IDENTIFIER);
        packAddressMap();
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingPublicKey() {
        addressMap.remove(ADDRESS_PUBLIC_KEY_IDENTIFIER);
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
        assertEquals(mqttAddress, server.getMqttAddress());
    }

    @Test
    public void testGetAddress() {
        String extractedAddress = server.getAddress();
        assertTrue(extractedAddress.matches("beam:[0-9a-zA-Z]{200,240}")); // length may vary
        assertEquals(address, extractedAddress);
    }

    @Test
    public void testGetAndUseAddress() {
        String address = server.getAddress();
        Server reconstruction = new Server(address);

        assertEquals(server.getHttpUrl(), reconstruction.getHttpUrl());
        assertArrayEquals(server.getPublicKeyAsBytes(), reconstruction.getPublicKeyAsBytes());
    }

    @Test
    public void testEquals() {
        Server other = null;
        assertFalse(server.equals(null)); // this has to be tested this way
        assertFalse(server.equals(other));

        other = Server.generate();
        assertFalse(server.equals(other));

        other.keyPair = fromPublicKey(server.getPublicKeyAsBytes());
        assertFalse(server.equals(other));

        other.httpUrl = server.httpUrl;
        other.keyPair = fromBothKeys(server.getPublicKeyAsBytes(), server.getPrivateKeyAsBytes());
        assertTrue(server.equals(other));

        other.keyPair = server.keyPair;
        assertTrue(server.equals(other));

        other = new Server(server.mqttAddress, server.httpUrl, keyPair);
        assertTrue(server.equals(other));

        other.httpUrl = null;
        assertFalse(server.equals(other));

        server.keyPair = fromPublicKey(keyPair.getPublic().getEncoded());
        other.httpUrl = server.httpUrl;
        other.keyPair = keyPair;
        assertFalse(server.equals(other));

        other.keyPair = fromPublicKey(keyPair.getPublic().getEncoded());
        assertTrue(server.equals(other));

        assertTrue(server.equals(server));
    }

    @Test
    public void testGenerate() {
        addressMap.remove(ADDRESS_HTTP_URL_IDENTIFIER);
        packAddressMap();

        server = Server.generate();
        assertEquals("example.com", server.mqttAddress.getHostString());
        assertEquals(MQTT_DEFAULT_PORT, server.mqttAddress.getPort());
        assertEquals("http://example.com", server.httpUrl.toString());
        assertNotNull(server.getPublicKey().getEncoded());
        assertNotNull(server.getPrivateKey().getEncoded());
    }

}
