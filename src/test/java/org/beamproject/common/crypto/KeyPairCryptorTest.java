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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class KeyPairCryptorTest {

    private final String PASSWORD = "12345678";
    private KeyPair keyPair;
    private EncryptedKeyPair encryptedPublicKey;

    @Before
    public void setUp() {
        keyPair = EccKeyPairGenerator.generate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNulls() {
        encryptedPublicKey = KeyPairCryptor.encrypt(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullPassword() {
        encryptedPublicKey = KeyPairCryptor.encrypt(null, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullKeyPair() {
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, null);
    }

    @Test
    public void testEncryptAndDecryptWithEmptyPrivateKey() {
        KeyPair publicKeyOnly = EccKeyPairGenerator.fromPublicKey(keyPair.getPublic().getEncoded());
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, publicKeyOnly);

        assertFalse(encryptedPublicKey.getEncryptedPublicKey().isEmpty());
        assertTrue(encryptedPublicKey.getEncryptedPrivateKey().isEmpty());

        KeyPair decryptedPublicKey = KeyPairCryptor.decrypt(PASSWORD, encryptedPublicKey);

        assertArrayEquals(keyPair.getPublic().getEncoded(), decryptedPublicKey.getPublic().getEncoded());
        assertNull(decryptedPublicKey.getPrivate());
    }

    @Test
    public void testEncryptAndDecrypt() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, keyPair);

        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
        assertEquals(172, encryptedPublicKey.getEncryptedPublicKey().length());
        assertEquals(280, encryptedPublicKey.getEncryptedPrivateKey().length());
        assertEquals(24, encryptedPublicKey.getSalt().length());

        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);
        KeyPair decryptedKeyPair = KeyPairCryptor.decrypt(PASSWORD, encryptedPublicKey);

        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
        assertArrayEquals(keyPair.getPublic().getEncoded(), decryptedKeyPair.getPublic().getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), decryptedKeyPair.getPrivate().getEncoded());
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPublicKey() {
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedPublicKey.encryptedPublicKey = "not really encrypted public key";
        KeyPairCryptor.decrypt(PASSWORD, encryptedPublicKey);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPrivateKey() {
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedPublicKey.encryptedPrivateKey = "not really encrypted private key";
        KeyPairCryptor.decrypt(PASSWORD, encryptedPublicKey);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedSalt() {
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedPublicKey.salt = "not really the salt";
        KeyPairCryptor.decrypt(PASSWORD, encryptedPublicKey);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnWrongPassword() {
        encryptedPublicKey = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        KeyPairCryptor.decrypt("wrong password", encryptedPublicKey);
    }

    @Test
    public void testOverwritePassword() {
        char[] passwordToOverwrite = PASSWORD.toCharArray();
        KeyPairCryptor.overwritePassword(passwordToOverwrite);

        for (int i = 0; i < passwordToOverwrite.length; i++) {
            assertEquals((char) 0, passwordToOverwrite[i]);
        }
    }

}
