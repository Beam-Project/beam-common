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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.crypto;

import java.security.Security;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class AesCipherTest {

    private final byte[] CIPHERTEXT = new byte[]{(byte) -30, (byte) 79,
        (byte) 98, (byte) 5, (byte) 79, (byte) 94, (byte) -75, (byte) -8,
        (byte) 123, (byte) 25, (byte) 4, (byte) 23, (byte) 109, (byte) 19,
        (byte) 89, (byte) 38, (byte) 94, (byte) -104, (byte) 94, (byte) 50,
        (byte) -110, (byte) 93, (byte) -98, (byte) -36, (byte) 109, (byte) -79,
        (byte) 40, (byte) 72, (byte) -50, (byte) 52, (byte) -92, (byte) 25};
    private AesCipher cipher;
    private AesCipher initializedCipher;
    private final byte[] plaintext = "this is the plaintext".getBytes();
    private byte[] key;
    private byte[] output;

    @Before
    public void setUp() {
        key = fillByteArray(32);
        initializedCipher = new AesCipher(key);
    }

    private byte[] fillByteArray(int length) {
        byte[] array = new byte[length];

        for (int i = 0; i < length; i++) {
            array[i] = (byte) i;
        }

        return array;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        cipher = new AesCipher(null);
    }

    @Test
    public void testConstructorOnKeyLength() {
        int correctKeyLengths[] = {16, 24, 32};

        for (int i = 0; i < 50; i++) {
            try {
                cipher = new AesCipher(fillByteArray(i));

                for (int length : correctKeyLengths) {
                    if (i == length) {
                        throw new IllegalArgumentException("Go 'manually' to the cacht block.");
                    }
                }

                fail("The key of the length of " + i + " bytes should lead to an IllegalArgumentException.");
            } catch (IllegalArgumentException ex) {
            }
        }
    }

    @Test
    public void testConstructorOnAssignments() {
        cipher = new AesCipher(key);
        assertNotNull(cipher.parameters);
    }

    @Test
    public void testConstructorOnCipherInit() {
        // The Bouncy Caste Provider should not be known now.
        assertNull(Security.getProperty(PROVIDER_NAME));

        cipher = new AesCipher(key);

        // Now, the Bouncy Castle Provider should be installed.
        assertNotNull(Security.getProvider(PROVIDER_NAME));
        assertNotNull(cipher.cipher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncryptOnNull() {
        output = initializedCipher.encrypt(null);
    }

    /**
     * This test checks if a specific plaintext (form {@code plaintext}) can be
     * encrypted to the exact same ciphertext. Of course, the same Key has to be
     * used.
     */
    @Test
    public void testEncrypt() {
        output = initializedCipher.encrypt(plaintext);
        assertArrayEquals(CIPHERTEXT, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptOnNull() {
        output = initializedCipher.decrypt(null);
    }

    /**
     * This test checks if a specific ciphertext (form {@code CIPHERTEXT}) can
     * be decrypted to the exact same plaintext. Of course, the same IV and Key
     * have to be used.
     */
    @Test
    public void testDecrypt() {
        output = initializedCipher.decrypt(CIPHERTEXT);
        assertArrayEquals(plaintext, output);
    }

}
