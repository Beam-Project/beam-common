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
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.msgpack.MessagePack;

public class UserTest {

    private final MessagePack PACK = new MessagePack();
    private final String USERNAME = "Homer";
    private final KeyPair KEY_PAIR = EccKeyPairGenerator.generate();
    private User user;
    private Server server;
    private Map<String, byte[]> addressMap;
    private String address;

    @Before
    public void setUp() throws IOException {
        server = Server.generate();
        user = new User(USERNAME, KEY_PAIR, server);

        createAddress();
    }

    private void createAddress() throws IOException {
        addressMap = new LinkedHashMap<>();
        addressMap.put(User.ADDRESS_PUBLIC_KEY_IDENTIFIER, user.getPublicKeyAsBytes());
        addressMap.put(User.ADDRESS_USERNAME_IDENTIFIER, user.username.getBytes());
        packAddressMap();
    }

    private void packAddressMap() {
        try {
            address = server.getAddress() + "." + Base58.encode(PACK.write(addressMap));
        } catch (IOException ex) {
            throw new RuntimeException("Could not pack.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        user = new User(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUsername() {
        user = new User(null, KEY_PAIR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnEmptyUsername() {
        user = new User("", KEY_PAIR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullKeyPair() {
        user = new User(USERNAME, null);
    }

    @Test
    public void testConstructor() {
        user = new User(USERNAME, KEY_PAIR);
        assertEquals(USERNAME, user.username);
        assertSame(KEY_PAIR, user.keyPair);
        assertNotNull(user.pack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithServerOnNullServer() {
        user = new User(USERNAME, KEY_PAIR, null);
    }

    @Test
    public void testConstructorWithServer() {
        user = new User(USERNAME, KEY_PAIR, server);
        assertEquals(USERNAME, user.username);
        assertSame(KEY_PAIR, user.keyPair);
        assertSame(server, user.server);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnNull() {
        user = new User(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnEmptyString() {
        user = new User("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnInvalidAddress() {
        user = new User("beam:longTextLongTexturl=asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnIllegalChracters() {
        user = new User("beam\0\":)(*&^%$#@?url~~~!!@äåé®äåé®þäå®éáßðg\0asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingPublicKey() {
        addressMap.remove(User.ADDRESS_PUBLIC_KEY_IDENTIFIER);
        packAddressMap();
        user = new User(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnNullPublicKey() {
        addressMap.put(User.ADDRESS_PUBLIC_KEY_IDENTIFIER, null);
        packAddressMap();
        user = new User(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingUsername() {
        addressMap.remove(User.ADDRESS_USERNAME_IDENTIFIER);
        packAddressMap();
        user = new User(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnNullUsername() {
        addressMap.put(User.ADDRESS_USERNAME_IDENTIFIER, null);
        packAddressMap();
        user = new User(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnEmptyUsername() {
        addressMap.put(User.ADDRESS_USERNAME_IDENTIFIER, "".getBytes());
        packAddressMap();
        user = new User(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMissingServer() throws IOException {
        address = "beam:" + Base58.encode(PACK.write(addressMap));
        server = new Server(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddressContstructorOnMalformedPublicKey() {
        address = address.substring(0, 250) + address.substring(270);
        user = new User(address);
    }

    @Test
    public void testAddressContstructor() {
        user = new User(address);
        assertEquals(address, user.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUsernameOnNull() {
        user.setUsername(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUsernameOnEmptyString() {
        user.setUsername("");
    }

    @Test
    public void testSetUsername() {
        user.setUsername("Bob");
        assertEquals("Bob", user.username);
    }

    @Test
    public void testGetUsername() {
        assertEquals(USERNAME, user.getUsername());
        user.username = null;
        assertNull(user.getUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetServerOnNull() {
        user.setServer(null);
    }

    @Test
    public void testSetServer() {
        user.server = null;
        user.setServer(server);
        assertSame(server, user.server);
    }

    @Test
    public void testGetServer() {
        user.setServer(server);
        assertSame(server, user.server);
        user.server = null;
        assertNull(user.getServer());
    }

    @Test
    public void testIsServerSet() {
        assertTrue(user.isServerSet());
        user.server = null;
        assertFalse(user.isServerSet());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetAddressOnMissingServer() {
        user.server = null;
        user.getAddress();
    }

    @Test
    public void testGetAddress() {
        assertEquals(address, user.getAddress());
    }

    @Test
    public void testGenerate() {
        user = User.generate();
        assertEquals(User.DEFAULT_USERNAME, user.username);
        assertNotNull(user.getKeyPair().getPublic().getEncoded());
        assertNotNull(user.getKeyPair().getPrivate().getEncoded());
    }

    @Test
    public void testEquals() {
        User other = null;
        assertFalse(user.equals(null));
        assertFalse(user.equals(other));

        other = User.generate();
        assertFalse(user.equals(other));

        other.keyPair = EccKeyPairGenerator.fromPublicKey(user.getPublicKeyAsBytes());
        assertFalse(user.equals(other));

        other.username = user.username;
        other.keyPair = EccKeyPairGenerator.fromBothKeys(user.getPublicKeyAsBytes(), user.getPrivateKeyAsBytes());
        assertTrue(user.equals(other));

        other.keyPair = user.keyPair;
        assertTrue(user.equals(other));

        other = new User(user.username, KEY_PAIR);
        assertTrue(user.equals(other));

        other.username = "";
        assertFalse(user.equals(other));

        other.username = "asdlfj";
        assertFalse(user.equals(other));

        other.username = USERNAME.toLowerCase();
        assertFalse(user.equals(other));

        user.keyPair = EccKeyPairGenerator.fromPublicKey(KEY_PAIR.getPublic().getEncoded());
        other.username = user.username;
        other.keyPair = KEY_PAIR;
        assertFalse(user.equals(other));

        other.keyPair = EccKeyPairGenerator.fromPublicKey(KEY_PAIR.getPublic().getEncoded());
        assertTrue(user.equals(other));

        assertTrue(user.equals(user));
    }

}
