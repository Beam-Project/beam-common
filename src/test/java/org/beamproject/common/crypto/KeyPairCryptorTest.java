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
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class KeyPairCryptorTest {

    private final String PASSWORD = "12345678";
    private KeyPair keyPair;
    private EncryptedKeyPair encryptedKeyPair;

    @Before
    public void setUp() {
        keyPair = EccKeyPairGenerator.generate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNulls() {
        encryptedKeyPair = KeyPairCryptor.encrypt(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullPassword() {
        encryptedKeyPair = KeyPairCryptor.encrypt(null, keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullKeyPair() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, null);
    }

    @Test
    public void testEncryptAndDecryptWithEmptyPrivateKey() {
        KeyPair publicKeyOnly = fromPublicKey(keyPair.getPublic().getEncoded());
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, publicKeyOnly);

        assertFalse(encryptedKeyPair.getEncryptedPublicKey().isEmpty());
        assertTrue(encryptedKeyPair.getEncryptedPrivateKey().isEmpty());

        KeyPair decryptedPublicKey = KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);

        assertArrayEquals(keyPair.getPublic().getEncoded(), decryptedPublicKey.getPublic().getEncoded());
        assertNull(decryptedPublicKey.getPrivate());
    }

    @Test
    public void testEncryptAndDecrypt() {
        Security.removeProvider(PROVIDER_NAME);
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);

        assertNotNull(Security.getProvider(PROVIDER_NAME));
        assertTrue(encryptedKeyPair.getEncryptedPublicKey().length() > 173);
        assertTrue(encryptedKeyPair.getEncryptedPrivateKey().length() > 282);
        assertTrue(encryptedKeyPair.getSalt().length() > 20);

        Security.removeProvider(PROVIDER_NAME);
        KeyPair decryptedKeyPair = KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);

        assertNotNull(Security.getProvider(PROVIDER_NAME));
        assertArrayEquals(keyPair.getPublic().getEncoded(), decryptedKeyPair.getPublic().getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), decryptedKeyPair.getPrivate().getEncoded());
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPublicKey() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.encryptedPublicKey = "not really encrypted public key".getBytes();
        KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPrivateKey() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.encryptedPrivateKey = "not really encrypted private key".getBytes();
        KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedSalt() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.salt = "not really the salt".getBytes();
        KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnWrongPassword() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        KeyPairCryptor.decrypt("wrong password", encryptedKeyPair);
    }

    @Test
    public void testOverwritePassword() {
        char[] passwordToOverwrite = PASSWORD.toCharArray();
        KeyPairCryptor.overwritePassword(passwordToOverwrite);

        for (int i = 0; i < passwordToOverwrite.length; i++) {
            assertEquals(0, passwordToOverwrite[i]);
        }
    }

}
