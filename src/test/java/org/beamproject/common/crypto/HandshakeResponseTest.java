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
import static org.beamproject.common.Message.DEFAUTL_VERSION;
import static org.beamproject.common.MessageField.*;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeResponseTest extends HandshakeTest {

    private HandshakeResponse responder;

    @Before
    public void setUpResponse() {
        responder = new HandshakeResponse(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        responder = new HandshakeResponse(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, responder.localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeChallengeOnNull() {
        responder.consumeChallenge(null);
    }

    @Test
    public void testConsumeChallenge() {
        remoteNonce = generateNonce();
        Message challenge = new Message();
        challenge.setVersion(DEFAUTL_VERSION);
        challenge.setParticipant(localParticipant);
        challenge.appendContent(CNT_CRPUBKEY, remoteParticipant.getPublicKeyAsBytes());
        challenge.appendContent(CNT_CRNONCE, remoteNonce);

        responder.consumeChallenge(challenge);

        assertEquals(remoteParticipant, responder.remoteParticipant);
        assertArrayEquals(remoteNonce, responder.remoteNonce);
    }

    @Test
    public void testProduceResponse() {
        testConsumeChallenge(); // To set the response into the correct state.
        Message response = responder.produceResponse();

        assertEquals(DEFAUTL_VERSION, response.getVersion());
        assertEquals(RESPONSE.toString(), new String(response.getContent(CNT_CRPHASE)));
        assertEquals(remoteParticipant, response.getParticipant());

        byte[] localDigest = digest(localParticipant, responder.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, response.getContent(CNT_CRSIG), localParticipant.getPublicKey()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeSuccessOnNull() {
        responder.consumeSuccess(null);
    }

    @Test
    public void testConsumeSuccess() {
        testProduceResponse(); // Set the response into correct state.
        remoteSignature = sign(fullRemoteParticipant, remoteNonce, responder.localNonce);
        byte[] sessionKey = calculateSessionKey(remoteNonce, responder.localNonce);

        Message responseDone = new Message();
        responseDone.setVersion(DEFAUTL_VERSION);
        responseDone.setParticipant(localParticipant);
        responseDone.appendContent(CNT_CRSIG, remoteSignature);
        responder.consumeSuccess(responseDone);

        assertArrayEquals(sessionKey, responder.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        responder.getSessionKey();
    }

    public void testGetSessionKey() {
        byte[] testKey = new byte[]{1, 2, 3};
        responder.sessionKey = testKey;
        assertSame(testKey, responder.getSessionKey());
    }

}
