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
        keyPair = EccKeyPairGenerator.generate();

        cipher = new EccCipher(keyPair.getPrivate(), keyPair.getPublic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        cipher = new EccCipher(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPrivateKey() {
        cipher = new EccCipher(null, keyPair.getPublic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPublicKey() {
        cipher = new EccCipher(keyPair.getPrivate(), null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertEquals(keyPair.getPrivate(), cipher.localPrivateKey);
        assertEquals(keyPair.getPublic(), cipher.remotePublicKey);
    }

    @Test
    public void testConstructorOnCreatingCipher() {
        assertNotNull(cipher.engine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNull() {
        cipher.encrypt(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNull() {
        cipher.decrypt(null);
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

            ciphertext = cipher.encrypt(plaintext);
            assertThat(workingText, not(equalTo(new String(ciphertext))));

            output = cipher.decrypt(ciphertext);
            assertArrayEquals(plaintext, output);

            workingText += textAddition + textAddition;
        }
    }

}
