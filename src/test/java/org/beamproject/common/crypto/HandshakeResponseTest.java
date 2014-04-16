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

public class HandshakeResponseTest extends HandshakeTest {

    private HandshakeResponse response;

    @Before
    public void setUpResponse() {
        response = new HandshakeResponse(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        response = new HandshakeResponse(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, response.localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeInitChallengeOnNull() {
        response.consumeInitChallenge(null);
    }

    @Test
    public void testConsumeInitChallenge() {
        remoteNonce = generateNonce();

        Message initChallenge = new Message();
        initChallenge.setVersion(Message.DEFAUTL_VERSION);
        initChallenge.setParticipant(localParticipant);
        initChallenge.appendContent(MessageField.CNT_CRPUBKEY, remoteParticipant.getPublicKeyAsBytes());
        initChallenge.appendContent(MessageField.CNT_CRNONCE, remoteNonce);

        response.consumeInitChallenge(initChallenge);

        assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(), response.remoteParticipant.getPublicKeyAsBytes());
        assertArrayEquals(remoteNonce, response.remoteNonce);
    }

    @Test
    public void testProduceResponseChallenge() {
        testConsumeInitChallenge(); // To set the response into the correct state.
        Message responseChallenge = response.produceResponseChallenge();

        assertEquals(Message.DEFAUTL_VERSION, responseChallenge.getVersion());

        assertArrayEquals(Handshake.Phase.RESPONSE_CHALLENGE.getBytes(),
                responseChallenge.getContent().get(MessageField.CNT_CRPHASE.toString()));

        assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(),
                responseChallenge.getParticipant().getPublicKeyAsBytes());

        byte[] localDigest = digest(localParticipant, response.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, responseChallenge.getContent().get(MessageField.CNT_CRSIG.toString()), localParticipant.getPublicKey()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeResponseDoneOnNull() {
        response.consumeResponseDone(null);
    }

    @Test
    public void testConsumeResponseDone() {
        testProduceResponseChallenge(); // Set the response into correct state.
        remoteSignature = sign(remoteParticipant, remoteNonce, response.localNonce);
        byte[] sessionKey = calculateSessionKey(remoteNonce, response.localNonce);

        Message responseDone = new Message();
        responseDone.setVersion(Message.DEFAUTL_VERSION);
        responseDone.setParticipant(localParticipant);
        responseDone.appendContent(MessageField.CNT_CRSIG, remoteSignature);

        response.consumeResponseDone(responseDone);

        assertArrayEquals(sessionKey, response.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        response.getSessionKey();
    }

    public void testGetSessionKey() {
        byte[] testKey = new byte[]{1, 2, 3};
        response.sessionKey = testKey;
        assertSame(testKey, response.getSessionKey());
    }

}
