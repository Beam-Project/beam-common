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

import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;

public class EncryptedKeyPairTest {

    private final byte[] PUBLIC_KEY = "publicKey".getBytes();
    private final byte[] PRIVATE_KEY = "privateKey".getBytes();
    private final byte[] SALT = "salt".getBytes();
    private EncryptedKeyPair pair;

    @Test
    public void testConstructorOnNulls() {
        pair = new EncryptedKeyPair(PUBLIC_KEY, PRIVATE_KEY, SALT);
        assertArrayEquals(PUBLIC_KEY, pair.encryptedPublicKey);
        assertArrayEquals(PRIVATE_KEY, pair.encryptedPrivateKey);
        assertArrayEquals(SALT, pair.salt);

        pair = new EncryptedKeyPair(null, PRIVATE_KEY, SALT);
        assertArrayEquals(new byte[0], pair.encryptedPublicKey);
        assertArrayEquals(PRIVATE_KEY, pair.encryptedPrivateKey);
        assertArrayEquals(SALT, pair.salt);

        pair = new EncryptedKeyPair(PUBLIC_KEY, null, SALT);
        assertArrayEquals(PUBLIC_KEY, pair.encryptedPublicKey);
        assertArrayEquals(new byte[0], pair.encryptedPrivateKey);
        assertArrayEquals(SALT, pair.salt);

        pair = new EncryptedKeyPair(PRIVATE_KEY, PUBLIC_KEY, null);
        assertArrayEquals(PRIVATE_KEY, pair.encryptedPublicKey);
        assertArrayEquals(PUBLIC_KEY, pair.encryptedPrivateKey);
        assertArrayEquals(new byte[0], pair.salt);
    }

    @Test
    public void testGetters() {
        pair = new EncryptedKeyPair(PUBLIC_KEY, PRIVATE_KEY, SALT);

        assertArrayEquals(PUBLIC_KEY, pair.getEncryptedPublicKeyAsBytes());
        assertArrayEquals(PRIVATE_KEY, pair.getEncryptedPrivateKeyAsBytes());
        assertArrayEquals(SALT, pair.getSaltAsBytes());

        String publicKeyAsBase58 = Base58.encode(PUBLIC_KEY);
        String privateKeyAsBase58 = Base58.encode(PRIVATE_KEY);
        String saltAsBase58 = Base58.encode(SALT);

        assertEquals(publicKeyAsBase58, pair.getEncryptedPublicKey());
        assertEquals(privateKeyAsBase58, pair.getEncryptedPrivateKey());
        assertEquals(saltAsBase58, pair.getSalt());
    }

}
