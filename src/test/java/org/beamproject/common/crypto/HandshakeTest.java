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

import java.security.SecureRandom;
import java.util.ArrayList;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.Handshake.NONCE_LENGTH_IN_BYTES;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.beamproject.common.util.Arrays;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.Test;

public class HandshakeTest {

    protected Participant localParticipant;
    protected Participant remoteParticipant;
    protected Participant fullRemoteParticipant;
    protected EccSigner signer;
    protected byte[] remoteNonce;
    protected byte[] remoteSignature;
    private byte[] sessionKey;

    @Before
    public void setUp() {
        localParticipant = Participant.generate();
        fullRemoteParticipant = Participant.generate();
        remoteParticipant = new Participant(EccKeyPairGenerator.fromPublicKey(fullRemoteParticipant.getPublicKeyAsBytes()));
        signer = new EccSigner();

        sessionKey = "hello".getBytes();
    }

    protected byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH_IN_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        return nonce;
    }

    protected byte[] sign(Participant signingParticipant, byte[] signersNonce, byte[] othersNonce) {
        byte[] digest = digest(signingParticipant, signersNonce, othersNonce);
        return signer.sign(digest, signingParticipant.getPrivateKey());
    }

    protected byte[] digest(Participant digestingParticipant, byte[] digestersNonce, byte[] othersNonce) {
        return Digest.digestWithSha256(Arrays.mergeArrays(digestingParticipant.getPublicKeyAsBytes(), digestersNonce, othersNonce));
    }

    protected byte[] calculateSessionKey(byte[] requesterNonce, byte[] requesteeNonce) {
        return Digest.digestWithSha256(Arrays.mergeArrays(requesterNonce, requesteeNonce));
    }

    protected byte[] toBytes(ArrayList<Byte> list) {
        byte[] copy = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            copy[i] = list.get(i);
        }

        return copy;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidateOnNulls() {
        Handshake.getInvalidate(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidateOnNullParticipant() {
        Handshake.getInvalidate(null, sessionKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidateOnNullKey() {
        Handshake.getInvalidate(remoteParticipant, null);
    }

    @Test
    public void testGetInvalidate() {
        Message message = Handshake.getInvalidate(remoteParticipant, sessionKey);
        assertArrayEquals(INVALIDATE.getBytes(), message.getContent(HSPHASE));
        assertArrayEquals(sessionKey, message.getContent(HSKEY));
    }

}
