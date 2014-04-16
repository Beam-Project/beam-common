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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeChallengeTest extends HandshakeTest {

    private HandshakeChallenge challenge;

    @Before
    public void setUpChallenge() {
        challenge = new HandshakeChallenge(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        challenge = new HandshakeChallenge(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, challenge.localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduceInitChallengeOnNull() {
        challenge.produceInitChallenge(null);
    }

    @Test
    public void testProduceInitChallenge() {
        Message initChallenge = challenge.produceInitChallenge(remoteParticipant);

        assertEquals(Message.DEFAUTL_VERSION, initChallenge.getVersion());

        assertArrayEquals(Handshake.Phase.INIT_CHALLENGE.getBytes(),
                initChallenge.getContent().get(MessageField.CNT_CRPHASE.toString()));

        assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(),
                initChallenge.getParticipant().getPublicKeyAsBytes());

        assertArrayEquals(localParticipant.getPublicKeyAsBytes(),
                initChallenge.getContent().get(MessageField.CNT_CRPUBKEY.toString()));

        assertNotNull(challenge.localNonce);
        assertArrayEquals(challenge.localNonce,
                initChallenge.getContent().get(MessageField.CNT_CRNONCE.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeResponseChallengeOnNull() {
        challenge.consumeResponseChallenge(null);
    }

    @Test
    public void testConsumeResponseChallenge() {
        testProduceInitChallenge(); // To generate localNonce in challenge.
        remoteNonce = generateNonce();
        remoteSignature = sign(remoteParticipant, remoteNonce, challenge.localNonce);

        Message response = new Message();
        response.setVersion(Message.DEFAUTL_VERSION);
        response.setParticipant(localParticipant);
        response.appendContent(MessageField.CNT_CRNONCE, remoteNonce);
        response.appendContent(MessageField.CNT_CRSIG, remoteSignature);

        challenge.consumeResponseChallenge(response);

        assertArrayEquals(remoteNonce, challenge.remoteNonce);
        assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(), challenge.remoteParticipant.getPublicKeyAsBytes());
        assertArrayEquals(remoteSignature, challenge.remoteSignature);
    }

    @Test
    public void testProduceResponseDone() {
        testConsumeResponseChallenge(); // To set the challenge into the correct state.
        Message responseDone = challenge.produceResponseDone();

        assertEquals(Message.DEFAUTL_VERSION, responseDone.getVersion());

        assertArrayEquals(Handshake.Phase.RESPONSE_DONE.getBytes(),
                responseDone.getContent().get(MessageField.CNT_CRPHASE.toString()));

        assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(),
                responseDone.getParticipant().getPublicKeyAsBytes());

        byte[] localDigest = digest(localParticipant, challenge.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, responseDone.getContent().get(MessageField.CNT_CRSIG.toString()), localParticipant.getPublicKey()));
        
        byte[] sessionKey = calculateSessionKey(challenge.localNonce, remoteNonce);
        assertArrayEquals(sessionKey, challenge.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        challenge.getSessionKey();
    }

    public void testGetSessionKey() {
        byte[] testKey = new byte[]{1, 2, 3};
        challenge.sessionKey = testKey;
        assertSame(testKey, challenge.getSessionKey());
    }

}