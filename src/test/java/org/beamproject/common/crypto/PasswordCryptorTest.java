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

import java.security.Security;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class PasswordCryptorTest {

    private PasswordCryptor cryptor;
    private final char[] PASSWORD = "12345678".toCharArray();
    private byte[] plaintext;
    private byte[] salt;
    private byte[] ciphertext;

    @Before
    public void setUp() {
        plaintext = "hello".getBytes();
        salt = PasswordCryptor.generateSalt();
        cryptor = new PasswordCryptor(PASSWORD, salt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        cryptor = new PasswordCryptor(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPassword() {
        cryptor = new PasswordCryptor(null, salt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullSalt() {
        cryptor = new PasswordCryptor(PASSWORD, null);
    }

    @Test
    public void testConstructor() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);
        cryptor = new PasswordCryptor(PASSWORD, salt);
        assertSame(PASSWORD, cryptor.password);
        assertSame(salt, cryptor.salt);
        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
        assertNotNull(cryptor.aesKey);
        assertNotNull(cryptor.cipher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNull() {
        ciphertext = cryptor.encrypt(null);
    }

    @Test
    public void testEncryptAndDecryptWithEmptyPlaintext() {
        ciphertext = cryptor.encrypt(new byte[0]);
        assertTrue(ciphertext.length > 0);

        plaintext = cryptor.decrypt(ciphertext);
        assertTrue(plaintext.length == 0);
    }

    @Test
    public void testEncryptAndDecrypt() {
        ciphertext = cryptor.encrypt(plaintext);
        assertEquals(16, ciphertext.length);

        byte[] decryptedCiphertext = cryptor.decrypt(ciphertext);
        assertArrayEquals(plaintext, decryptedCiphertext);
    }

    @Test(expected = CryptoException.class)
    public void testEncryptAndDecryptOnManipulatedBytes() {
        ciphertext = cryptor.encrypt(plaintext);
        ciphertext = "not really encrypted public key".getBytes();
        cryptor.decrypt(ciphertext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNull() {
        plaintext = cryptor.decrypt(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangePasswordOnNull() {
        cryptor.changePassword(null);
    }

    @Test
    public void testChangePasswordOnZeroingOldPasswordInstance() {
        char[] oldPasswordInstance = cryptor.password;
        cryptor.changePassword("something new".toCharArray());
        
        for (char c : oldPasswordInstance) {
            assertEquals(0, c);
        }
    }

    @Test
    public void testChangePassword() {
        char[] newPassword = "new pass".toCharArray();
        cryptor.changePassword(newPassword);
        assertSame(newPassword, cryptor.password);
    }

    @Test
    public void testGenrateSalt() {
        salt = PasswordCryptor.generateSalt();
        assertEquals(PasswordCryptor.SALT_LENGTH_IN_BYTES, salt.length);
    }

}
