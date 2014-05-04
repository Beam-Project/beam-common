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
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class EccCipherTest {

    private final int NUMBER_OF_ENCRYPTIONS = 50;
    private EccCipher cipher;
    private KeyPair keyPair;

    @Before
    public void setUp() {
        cipher = new EccCipher();
        keyPair = EccKeyPairGenerator.generate();
    }

    @Test
    public void testConstructorOnCreatingCipher() {
        assertNotNull(cipher.engine);
        assertNotNull(cipher.parameterSpec);
        assertEquals(EccCipher.MAC_KEY_SIZE_IN_BITS, cipher.parameterSpec.getMacKeySize());
        assertEquals(EccCipher.AES_KEY_SIZE_IN_BITS, cipher.parameterSpec.getCipherKeySize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNulls() {
        cipher.encrypt(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullPlaintext() {
        cipher.encrypt(null, keyPair.getPublic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNullPublicKey() {
        cipher.encrypt("".getBytes(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNulls() {
        cipher.decrypt(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNullCiphertext() {
        cipher.decrypt(null, keyPair.getPrivate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNullPrivateKey() {
        cipher.decrypt("".getBytes(), null);
    }

    @Test
    public void testEncryptionAndDecryption() {
        String workingText = "";
        String textAddition = "Text_";
        byte[] plaintext;
        byte[] ciphertext;
        byte[] output;

        for (int i = 0; i < NUMBER_OF_ENCRYPTIONS; i++) {
            plaintext = workingText.getBytes();

            ciphertext = cipher.encrypt(plaintext, keyPair.getPublic());
            assertThat(workingText, not(equalTo(new String(ciphertext))));

            output = cipher.decrypt(ciphertext, keyPair.getPrivate());
            assertArrayEquals(plaintext, output);

            workingText += textAddition + textAddition;
        }
    }

}
