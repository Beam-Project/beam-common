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

import org.beamproject.common.Message;
import org.beamproject.common.MessageField;
import org.beamproject.common.Participant;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class CryptoPackerTest {

    private CryptoPacker localPacker;
    private CryptoPacker remotePacker;
    private Participant localParticipant;
    private Participant reomteParticipant;
    private Message plaintext;
    private byte[] ciphertext;

    @Before
    public void setUp() {
        localParticipant = new Participant(EccKeyPairGenerator.generate());
        reomteParticipant = new Participant(EccKeyPairGenerator.generate());

        plaintext = new Message();
        plaintext.setVersion("1.0");
        plaintext.setParticipant(localParticipant);
        plaintext.appendContent(MessageField.CNT_MSG, "hello world".getBytes());

        localPacker = new CryptoPacker();
        remotePacker = new CryptoPacker();
    }

    @Test
    public void testConstructorOnAssignment() {
        assertNotNull(localPacker.messagePack);
        assertNotNull(localPacker.eccCipher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPackAndEncryptOnNulls() {
        localPacker.packAndEncrypt(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPackAndEncryptOnNullPlaintext() {
        localPacker.packAndEncrypt(null, localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPackAndEncryptOnNullParticipant() {
        localPacker.packAndEncrypt(plaintext, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPackAndEncryptOnNotSetParticipant() {
        plaintext = new Message();
        localPacker.packAndEncrypt(plaintext, reomteParticipant);
    }

    @Test
    public void testPackAndEncryptOnParticipant() {
        plaintext.setParticipant(localParticipant);
        localPacker.packAndEncrypt(plaintext, reomteParticipant);
    }

    @Test
    public void testPackAndEncryptAndAlsoDecryptAndUnpack() {
        int expectedCiphertextLength = 304;

        ciphertext = localPacker.packAndEncrypt(plaintext, reomteParticipant);
        assertEquals(expectedCiphertextLength, ciphertext.length);

        Message decryptedCiphertext = remotePacker.decryptAndUnpack(ciphertext, reomteParticipant);
        assertEquals(plaintext.getVersion(), decryptedCiphertext.getVersion());
        assertArrayEquals(plaintext.getContent(MessageField.CNT_MSG), decryptedCiphertext.getContent(MessageField.CNT_MSG));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNulls() {
        remotePacker.decryptAndUnpack(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNullCiphertext() {
        remotePacker.decryptAndUnpack(null, reomteParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNullParticipant() {
        remotePacker.decryptAndUnpack("".getBytes(), null);
    }

}
