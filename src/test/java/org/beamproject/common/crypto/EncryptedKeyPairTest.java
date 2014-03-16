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
package org.beamproject.common.crypto;

import org.beamproject.common.util.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

public class EncryptedKeyPairTest {

    private final String PUBLIC_KEY = "publicKey";
    private final String PRIVATE_KEY = "privateKey";
    private final String SALT = "salt";
    private EncryptedKeyPair pair;

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        pair = new EncryptedKeyPair(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPublicKey() {
        pair = new EncryptedKeyPair(null, PRIVATE_KEY, SALT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPrivateKey() {
        pair = new EncryptedKeyPair(PUBLIC_KEY, null, SALT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullSalt() {
        pair = new EncryptedKeyPair(PUBLIC_KEY, PRIVATE_KEY, null);
    }

    @Test
    public void testGetters() {
        pair = new EncryptedKeyPair(PUBLIC_KEY, PRIVATE_KEY, SALT);

        assertEquals(PUBLIC_KEY, pair.getEncryptedPublicKey());
        assertEquals(PRIVATE_KEY, pair.getEncryptedPrivateKey());
        assertEquals(SALT, pair.getSalt());

        byte[] publicKeyAsBytes = Base64.decode(PUBLIC_KEY);
        byte[] privateKeyAsBytes = Base64.decode(PRIVATE_KEY);
        byte[] saltAsBytes = Base64.decode(SALT);

        assertArrayEquals(publicKeyAsBytes, pair.getEncryptedPublicKeyAsBytes());
        assertArrayEquals(privateKeyAsBytes, pair.getEncryptedPrivateKeyAsBytes());
        assertArrayEquals(saltAsBytes, pair.getSaltAsBytes());
    }

}
