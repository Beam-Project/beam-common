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
import static org.beamproject.common.Message.VERSION;
import static org.beamproject.common.MessageField.*;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeChallengeTest extends HandshakeTest {

    private HandshakeChallenge challenger;

    @Before
    public void setUpChallenge() {
        challenger = new HandshakeChallenge(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        challenger = new HandshakeChallenge(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, challenger.localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduceChallengeOnNull() {
        challenger.produceChallenge(null);
    }

    @Test
    public void testProduceChallenge() {
        Message challenge = challenger.produceChallenge(remoteParticipant);

        assertEquals(VERSION, challenge.getVersion());
        assertEquals(CHALLENGE.toString(), new String(challenge.getContent(CNT_CRPHASE)));
        assertEquals(remoteParticipant, challenge.getRecipient());
        assertArrayEquals(localParticipant.getPublicKeyAsBytes(), challenge.getContent(CNT_CRPUBKEY));
        assertArrayEquals(challenger.localNonce, challenge.getContent(CNT_CRNONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeResponseOnNull() {
        challenger.consumeResponse(null);
    }

    @Test
    public void testConsumeResponse() {
        testProduceChallenge(); // To generate localNonce in challenge.
        remoteNonce = generateNonce();
        remoteSignature = sign(fullRemoteParticipant, remoteNonce, challenger.localNonce);

        Message response = new Message();
        response.setVersion(VERSION);
        response.setRecipient(localParticipant);
        response.putContent(CNT_CRNONCE, remoteNonce);
        response.putContent(CNT_CRSIG, remoteSignature);

        challenger.consumeResponse(response);

        assertArrayEquals(remoteNonce, challenger.remoteNonce);
        assertEquals(remoteParticipant, challenger.remoteParticipant);
        assertArrayEquals(remoteSignature, challenger.remoteSignature);
    }

    @Test
    public void testProduceSuccess() {
        testConsumeResponse(); // To set the challenge into the correct state.
        Message success = challenger.produceSuccess();

        assertEquals(VERSION, success.getVersion());
        assertEquals(SUCCESS.toString(), new String(success.getContent(CNT_CRPHASE)));
        assertEquals(remoteParticipant, success.getRecipient());

        byte[] localDigest = digest(localParticipant, challenger.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, success.getContent(CNT_CRSIG), localParticipant.getPublicKey()));

        byte[] sessionKey = calculateSessionKey(challenger.localNonce, remoteNonce);
        assertArrayEquals(sessionKey, challenger.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        challenger.getSessionKey();
    }

    @Test
    public void testGetSessionKey() {
        byte[] testKey = new byte[]{1, 2, 3};
        challenger.sessionKey = testKey;
        assertSame(testKey, challenger.getSessionKey());
    }

}
