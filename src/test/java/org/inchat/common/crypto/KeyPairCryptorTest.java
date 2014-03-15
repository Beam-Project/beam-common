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
import java.security.Security;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class KeyPairCryptorTest {

    private final String PASSWORD = "12345678";
    private final String FILENAME = "keystore";
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
    public void testEncryptAndDecrypt() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);

        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
        assertEquals(172, encryptedKeyPair.getEncryptedPublicKey().length());
        assertEquals(280, encryptedKeyPair.getEncryptedPrivateKey().length());
        assertEquals(12, encryptedKeyPair.getSalt().length());

        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        KeyPair decryptedKeyPair = KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);

        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
        assertArrayEquals(keyPair.getPublic().getEncoded(), decryptedKeyPair.getPublic().getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), decryptedKeyPair.getPrivate().getEncoded());
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPublicKey() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.encryptedPublicKey = "not really encrypted public key";
        KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedPrivateKey() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.encryptedPrivateKey = "not really encrypted private key";
        KeyPairCryptor.decrypt(PASSWORD, encryptedKeyPair);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedSalt() {
        encryptedKeyPair = KeyPairCryptor.encrypt(PASSWORD, keyPair);
        encryptedKeyPair.salt = "not really the salt";
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
            assertEquals((char) 0, passwordToOverwrite[i]);
        }

    }

}
