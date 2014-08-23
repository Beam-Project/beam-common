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

import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Field.Cnt.*;
import static org.beamproject.common.message.Field.Cnt.Typ.*;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class CryptoPackerTest {

    private final byte[] MESSAGE = "hello world".getBytes();
    private final int EXPECTED_CPHERTEXT_LENGTH_IN_BYTES = 177;
    private CryptoPacker localPacker;
    private CryptoPacker remotePacker;
    private Participant participantWithBothKeys;
    private Participant participantWithPublicKey;
    private Message plaintext;
    private byte[] ciphertext;

    @Before
    public void setUp() {
        participantWithBothKeys = Participant.generate();
        participantWithPublicKey = new Participant(fromPublicKey(participantWithBothKeys.getPublicKeyAsBytes()));
        plaintext = new Message(FORWARD, participantWithPublicKey);
        plaintext.putContent(MSG, MESSAGE);

        localPacker = new CryptoPacker();
        remotePacker = new CryptoPacker();
    }

    @Test
    public void testConstructorOnAssignment() {
        assertNotNull(localPacker.messagePack);
        assertNotNull(localPacker.eccCipher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPackAndEncryptOnNull() {
        localPacker.packAndEncrypt(null);
    }

    @Test
    public void testPackAndEncryptOnParticipant() {
        plaintext.setRecipient(participantWithPublicKey);
        localPacker.packAndEncrypt(plaintext);
    }

    @Test
    public void testPackAndEncryptAndAlsoDecryptAndUnpack() {
        ciphertext = localPacker.packAndEncrypt(plaintext);
        assertEquals(EXPECTED_CPHERTEXT_LENGTH_IN_BYTES, ciphertext.length);

        Message decryptedCiphertext = remotePacker.decryptAndUnpack(ciphertext, participantWithBothKeys);
        assertEquals(plaintext.getVersion(), decryptedCiphertext.getVersion());
        assertEquals(plaintext.getType(), decryptedCiphertext.getType());
        assertSame(participantWithBothKeys, decryptedCiphertext.getRecipient());
        assertArrayEquals(plaintext.getContent(MSG), decryptedCiphertext.getContent(MSG));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNulls() {
        remotePacker.decryptAndUnpack(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNullCiphertext() {
        remotePacker.decryptAndUnpack(null, participantWithPublicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecryptAndUnPackOnNullParticipant() {
        remotePacker.decryptAndUnpack("".getBytes(), null);
    }

}
