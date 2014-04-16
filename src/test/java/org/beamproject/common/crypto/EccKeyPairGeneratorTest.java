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

import java.security.KeyPair;
import org.beamproject.common.Participant;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EccKeyPairGeneratorTest {

    private KeyPair keyPair;
    private KeyPair restored;
    private Participant originalFull;
    private Participant originalPublicOnly;
    private Participant restoredFull;
    private Participant restoredPublicOnly;

    @Before
    public void setUp() {
        keyPair = EccKeyPairGenerator.generate();
        originalFull = new Participant(keyPair);
        originalPublicOnly = new Participant(EccKeyPairGenerator.fromPublicKey(keyPair.getPublic().getEncoded()));
    }

    @Test
    public void testInstantiation() {
        EccKeyPairGenerator generator = new EccKeyPairGenerator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromPublicKeyOnNull() {
        EccKeyPairGenerator.fromPublicKey(null);
    }

    @Test
    public void testFromPublicKey() {
        restored = EccKeyPairGenerator.fromPublicKey(keyPair.getPublic().getEncoded());
        restoredPublicOnly = new Participant(restored);

        assertEquals(originalPublicOnly, restoredPublicOnly);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromPublicKeyOnManipulatedPublicKey() {
        byte[] publicKey = keyPair.getPublic().getEncoded();
        publicKey[20] = 123;
        publicKey[50] = 123;
        restored = EccKeyPairGenerator.fromPublicKey(publicKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromPublicKeyOnWrongArgument() {
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        restored = EccKeyPairGenerator.fromPublicKey(privateKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNulls() {
        EccKeyPairGenerator.fromBothKeys(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNullPublicKey() {
        EccKeyPairGenerator.fromBothKeys(null, new byte[]{1, 2, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNullPrivateKey() {
        EccKeyPairGenerator.fromBothKeys(new byte[]{1, 2, 3}, null);
    }

    @Test
    public void testFromBothKeys() {
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        restored = EccKeyPairGenerator.fromBothKeys(publicKey, privateKey);
        restoredFull = new Participant(restored);

        assertEquals(originalFull, restoredFull);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnManipulatedPublicKey() {
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        publicKey[10] = 123;
        publicKey[20] = 123;

        restored = EccKeyPairGenerator.fromBothKeys(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnManipulatedPrivateKey() {
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        privateKey[10] = 123;
        privateKey[20] = 123;

        restored = EccKeyPairGenerator.fromBothKeys(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnWrongArguments() {
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        restored = EccKeyPairGenerator.fromBothKeys(privateKey, publicKey);
    }
}
