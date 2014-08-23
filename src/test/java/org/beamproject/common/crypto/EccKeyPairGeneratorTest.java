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
import java.security.Security;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromBothKeys;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EccKeyPairGeneratorTest {

    private KeyPair originalKeyPair;
    private KeyPair restoredKeyPair;
    private Participant bothOriginal;
    private Participant publicOnlyOriginal;
    private Participant bothRestored;
    private Participant publicOnlyRestored;

    @Before
    public void setUp() {
        originalKeyPair = EccKeyPairGenerator.generate();
        bothOriginal = new Participant(originalKeyPair);
        publicOnlyOriginal = new Participant(fromPublicKey(originalKeyPair.getPublic().getEncoded()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromPublicKeyOnNull() {
        fromPublicKey(null);
    }

    @Test
    public void testFromPublicKey() {
        Security.removeProvider(PROVIDER_NAME);

        restoredKeyPair = fromPublicKey(originalKeyPair.getPublic().getEncoded());
        publicOnlyRestored = new Participant(restoredKeyPair);

        assertEquals(publicOnlyOriginal, publicOnlyRestored);
        assertTrue(Security.getProvider(PROVIDER_NAME) != null);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromPublicKeyOnManipulatedPublicKey() {
        byte[] publicKey = originalKeyPair.getPublic().getEncoded();
        publicKey[20] = 123;
        publicKey[50] = 123;
        restoredKeyPair = fromPublicKey(publicKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromPublicKeyOnWrongArgument() {
        byte[] privateKey = originalKeyPair.getPrivate().getEncoded();
        restoredKeyPair = fromPublicKey(privateKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNulls() {
        fromBothKeys(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNullPublicKey() {
        fromBothKeys(null, new byte[]{1, 2, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBothKeysOnNullPrivateKey() {
        fromBothKeys(new byte[]{1, 2, 3}, null);
    }

    @Test
    public void testFromBothKeys() {
        Security.removeProvider(PROVIDER_NAME);
        byte[] publicKey = originalKeyPair.getPublic().getEncoded();
        byte[] privateKey = originalKeyPair.getPrivate().getEncoded();

        restoredKeyPair = fromBothKeys(publicKey, privateKey);
        bothRestored = new Participant(restoredKeyPair);

        assertEquals(bothOriginal, bothRestored);
        assertTrue(Security.getProvider(PROVIDER_NAME) != null);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnManipulatedPublicKey() {
        byte[] publicKey = originalKeyPair.getPublic().getEncoded();
        byte[] privateKey = originalKeyPair.getPrivate().getEncoded();
        publicKey[10] = 123;
        publicKey[20] = 123;

        restoredKeyPair = fromBothKeys(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnManipulatedPrivateKey() {
        byte[] publicKey = originalKeyPair.getPublic().getEncoded();
        byte[] privateKey = originalKeyPair.getPrivate().getEncoded();
        privateKey[10] = 123;
        privateKey[20] = 123;

        restoredKeyPair = fromBothKeys(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromBothKeysOnWrongArguments() {
        byte[] publicKey = originalKeyPair.getPublic().getEncoded();
        byte[] privateKey = originalKeyPair.getPrivate().getEncoded();

        restoredKeyPair = fromBothKeys(privateKey, publicKey);
    }
}
