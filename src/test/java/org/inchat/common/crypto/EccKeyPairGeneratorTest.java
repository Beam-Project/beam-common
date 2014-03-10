/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-common.
 *
 * inchat-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.common.crypto;

import java.security.KeyPair;
import static org.junit.Assert.*;
import org.junit.Test;

public class EccKeyPairGeneratorTest {

    private KeyPair keyPair;

    @Test
    public void testInstantiation() {
        EccKeyPairGenerator generator = new EccKeyPairGenerator();
    }

    @Test
    public void testGenerate() {
        keyPair = EccKeyPairGenerator.generate();
        assertNotNull(keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreFromPublicKeyBytesOnNull() {
        EccKeyPairGenerator.restoreFromPublicKeyBytes(null);
    }

    @Test
    public void testRestoreFromPublicKeyBytes() {
        keyPair = EccKeyPairGenerator.generate();
        KeyPair restore = EccKeyPairGenerator.restoreFromPublicKeyBytes(keyPair.getPublic().getEncoded());

        assertArrayEquals(keyPair.getPublic().getEncoded(), restore.getPublic().getEncoded());
        assertNull(restore.getPrivate());
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromPublicKeyBytesOnManipulatedPublicKey() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        publicKey[20] = 123;
        publicKey[50] = 123;
        KeyPair restore = EccKeyPairGenerator.restoreFromPublicKeyBytes(publicKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromPublicKeyBytesOnWrongArgument() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        KeyPair restore = EccKeyPairGenerator.restoreFromPublicKeyBytes(privateKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnNulls() {
        EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnNullPublicKey() {
        EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(null, new byte[]{1, 2, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnNullPrivateKey() {
        EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(new byte[]{1, 2, 3}, null);
    }

    @Test
    public void testRestoreFromPublicAndPrivateKeyBytes() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        KeyPair restore = EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(publicKey, privateKey);

        assertArrayEquals(keyPair.getPublic().getEncoded(), restore.getPublic().getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), restore.getPrivate().getEncoded());
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnManipulatedPublicKey() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        publicKey[10] = 123;
        publicKey[20] = 123;

        KeyPair restore = EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnManipulatedPrivateKey() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        privateKey[10] = 123;
        privateKey[20] = 123;

        KeyPair restore = EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(publicKey, privateKey);
    }

    @Test(expected = IllegalStateException.class)
    public void testRestoreFromPublicAndPrivateKeyBytesOnWrongArguments() {
        keyPair = EccKeyPairGenerator.generate();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        KeyPair restore = EccKeyPairGenerator.restoreFromPublicAndPrivateKeyBytes(privateKey, publicKey);
    }
}
