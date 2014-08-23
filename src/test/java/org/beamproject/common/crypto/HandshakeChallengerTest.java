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

import java.util.ArrayList;
import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Message.VERSION;
import static org.beamproject.common.message.Field.Cnt.*;
import static org.beamproject.common.message.Field.Cnt.Typ.*;
import static org.beamproject.common.crypto.HandshakeResponder.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeChallengerTest extends HandshakeTest {

    private HandshakeChallenger challenger;

    @Before
    public void setUpChallenge() {
        challenger = new HandshakeChallenger(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        challenger = new HandshakeChallenger(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, challenger.localParticipant);
    }

    @Test
    public void testProduceChallenge() {
        assertFalse(challenger.wasProduceChallengeInvoked);

        Message challenge = challenger.produceChallenge(remoteParticipant);
        assertTrue(challenger.wasProduceChallengeInvoked);
        assertEquals(VERSION, challenge.getVersion());
        assertEquals(remoteParticipant, challenge.getRecipient());
        assertArrayEquals(HS_CHALLENGE.getBytes(), challenge.getContent(TYP));
        assertArrayEquals(localParticipant.getPublicKeyAsBytes(), challenge.getContent(HS_PUBKEY));
        assertArrayEquals(challenger.localNonce, challenge.getContent(HS_NONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduceChallengeOnNull() {
        challenger.produceChallenge(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceChallengeOnInvokingMethodTwice() {
        assertFalse(challenger.wasProduceChallengeInvoked);
        challenger.produceChallenge(remoteParticipant);
        assertTrue(challenger.wasProduceChallengeInvoked);
        challenger.produceChallenge(remoteParticipant); // should throw the exception
    }

    @Test
    public void testConsumeResponse() {
        testProduceChallenge(); // Set the challenger into needed state.

        remoteNonce = generateNonce();
        remoteSignature = sign(fullRemoteParticipant, remoteNonce, challenger.localNonce);

        Message response = new Message(HS_RESPONSE, localParticipant);
        response.setVersion(VERSION);
        response.putContent(HS_PUBKEY, localParticipant.getPublicKeyAsBytes());
        response.putContent(HS_NONCE, remoteNonce);
        response.putContent(HS_SIG, remoteSignature);
        assertFalse(challenger.wasConsumeResponseInvoked);

        challenger.consumeResponse(response);

        assertTrue(challenger.wasConsumeResponseInvoked);
        assertArrayEquals(remoteNonce, challenger.remoteNonce);
        assertEquals(remoteParticipant, challenger.remoteParticipant);
        assertArrayEquals(remoteSignature, challenger.remoteSignature);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnNull() {
        testProduceChallenge(); // Set the challenger into needed state.

        challenger.consumeResponse(null);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnWrongVersion() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.setVersion(VERSION + ".1");
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnMissingType() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.getContent().remove(TYP.toString());
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnWrongType() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.putContent(TYP, FORWARD.getBytes());
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnMissingPhase() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.getContent().remove(TYP.toString());
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnWrongPhase() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.putContent(TYP, HS_SUCCESS.getBytes());
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnMissingNonce() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.getContent().remove(HS_NONCE.toString());
        challenger.consumeResponse(response);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnMissingPublicKey() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.getContent().remove(HS_PUBKEY.toString());
        challenger.consumeResponse(response);
    }

    @Test
    public void testConsumeResponseOnWrongNonceLength() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message challenge = getBasicResponse();
        ArrayList<Byte> nonce = new ArrayList<>();

        for (int length = 0; length < NONCE_LENGTH_IN_BYTES * 2; length++) {
            challenge.putContent(HS_NONCE, toBytes(nonce));

            try {
                challenger.wasConsumeResponseInvoked = false;
                challenger.consumeResponse(challenge);

                if (length != NONCE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrown.");
                }
            } catch (HandshakeException ex) {
            }

            nonce.add((byte) length);
        }
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeResponseOnMissingSignature() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        response.getContent().remove(HS_SIG.toString());
        challenger.consumeResponse(response);
    }

    /**
     * This tests if the signature length can be too far away from the usual
     * length. Since the length can change for about a few bytes (depending on
     * randomness in {@link EccSigner}), this can't be tested on one specific
     * length.
     */
    @Test
    public void testConsumeResponseOnWrongSignatureLength() {
        testProduceChallenge(); // Set the challenger into needed state.

        Message response = getBasicResponse();
        ArrayList<Byte> signature = new ArrayList<>();

        for (int length = 0; length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES * 2; length++) {
            response.putContent(HS_SIG, toBytes(signature));

            try {
                challenger.wasConsumeResponseInvoked = false;
                challenger.consumeResponse(response);

                if (length > MINIMAL_SIGNATURE_LENGTH_IN_BYTES
                        && length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrown.");
                }
            } catch (HandshakeException ex) {
            }

            signature.add((byte) length);
        }
    }

    private Message getBasicResponse() {
        remoteNonce = generateNonce();
        calculateRemoteSignature();

        Message challenge = new Message(HS_RESPONSE, localParticipant);
        challenge.putContent(HS_SIG, remoteSignature);
        challenge.putContent(HS_NONCE, remoteNonce);

        return challenge;
    }

    private byte[] calculateRemoteSignature() {
        remoteSignature = sign(fullRemoteParticipant, remoteNonce, challenger.localNonce);
        return remoteSignature;
    }

    @Test
    public void testProduceSuccess() {
        testConsumeResponse(); // To set the challenge into needed state.
        assertFalse(challenger.wasProduceSuccessInvoked);

        Message success = challenger.produceSuccess();

        assertTrue(challenger.wasProduceSuccessInvoked);
        assertEquals(VERSION, success.getVersion());
        assertArrayEquals(HS_SUCCESS.getBytes(), success.getContent(TYP));
        assertArrayEquals(localParticipant.getPublicKeyAsBytes(), success.getContent(HS_PUBKEY));
        assertEquals(remoteParticipant, success.getRecipient());

        byte[] localDigest = digest(localParticipant, challenger.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, success.getContent(HS_SIG), localParticipant.getPublicKey()));

        byte[] sessionKey = calculateSessionKey(challenger.localNonce, remoteNonce);
        assertArrayEquals(sessionKey, challenger.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceSuccessOnInvokingMethodBeforeConsumeRespose() {
        assertFalse(challenger.wasConsumeResponseInvoked);
        challenger.produceSuccess();
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceSuccessOnInvokingMethodTwice() {
        testConsumeResponse(); // to set responder into needed state

        assertFalse(challenger.wasProduceSuccessInvoked);
        challenger.produceSuccess();
        assertTrue(challenger.wasProduceSuccessInvoked);
        challenger.produceSuccess(); // should throw the exception
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
