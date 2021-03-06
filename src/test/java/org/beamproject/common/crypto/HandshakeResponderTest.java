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
import static org.beamproject.common.crypto.Handshake.MAXIMAL_SIGNATURE_LENGTH_IN_BYTES;
import static org.beamproject.common.crypto.Handshake.MINIMAL_SIGNATURE_LENGTH_IN_BYTES;
import static org.beamproject.common.crypto.Handshake.NONCE_LENGTH_IN_BYTES;
import static org.beamproject.common.message.Field.Cnt.NONCE;
import static org.beamproject.common.message.Field.Cnt.PUBLIC_KEY;
import static org.beamproject.common.message.Field.Cnt.SIGNATURE;
import static org.beamproject.common.message.Field.Cnt.TYP;
import static org.beamproject.common.message.Field.Cnt.Typ.FORWARD;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_CHALLENGE;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_RESPONSE;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_SUCCESS;
import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Message.VERSION;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class HandshakeResponderTest extends HandshakeTest {

    private HandshakeResponder responder;

    @Before
    public void setUpResponse() {
        responder = new HandshakeResponder(localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        responder = new HandshakeResponder(null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, responder.localParticipant);
    }

    @Test
    public void testConsumeChallenge() {
        assertFalse(responder.wasConsumeChallengeInvoked);

        Message challenge = getBasicChallenge();
        responder.consumeChallenge(challenge);

        assertTrue(responder.wasConsumeChallengeInvoked);
        assertEquals(remoteParticipant, responder.remoteParticipant);
        assertArrayEquals(remoteNonce, responder.remoteNonce);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnNull() {
        responder.consumeChallenge(null);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnWrongVersion() {
        Message challenge = getBasicChallenge();
        challenge.setVersion(VERSION + ".1");
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingType() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(TYP.toString());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnWrongType() {
        Message challenge = getBasicChallenge();
        challenge.putContent(TYP, FORWARD.getBytes());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingPhase() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(TYP.toString());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnWrongPhase() {
        Message challenge = getBasicChallenge();
        challenge.putContent(TYP, HS_SUCCESS.getBytes());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingPublicKey() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(PUBLIC_KEY.toString());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnEmptyPublicKey() {
        Message challenge = getBasicChallenge();
        challenge.putContent(PUBLIC_KEY, new byte[]{});
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingNonce() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(NONCE.toString());
        responder.consumeChallenge(challenge);
    }

    @Test
    public void testConsumeChallengeOnWrongNonceLength() {
        Message challenge = getBasicChallenge();
        ArrayList<Byte> nonce = new ArrayList<>();

        for (int length = 0; length < NONCE_LENGTH_IN_BYTES * 2; length++) {
            challenge.putContent(NONCE, toBytes(nonce));

            try {
                responder.wasConsumeChallengeInvoked = false;
                responder.consumeChallenge(challenge);

                if (length != NONCE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrown.");
                }
            } catch (HandshakeException ex) {
            }

            nonce.add((byte) length);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeChallengeOnInvokingMethodTwice() {
        Message challenge = getBasicChallenge();

        assertFalse(responder.wasConsumeChallengeInvoked);
        responder.consumeChallenge(challenge);
        assertTrue(responder.wasConsumeChallengeInvoked);
        responder.consumeChallenge(challenge); // should throw the exception
    }

    private Message getBasicChallenge() {
        remoteNonce = generateNonce();
        Message challenge = new Message(HS_CHALLENGE, localParticipant);
        challenge.putContent(PUBLIC_KEY, remoteParticipant.getPublicKeyAsBytes());
        challenge.putContent(NONCE, remoteNonce);

        return challenge;
    }

    @Test
    public void testProduceResponse() {
        testConsumeChallenge(); // To set the response into the needed state.
        Message response = responder.produceResponse();

        assertEquals(VERSION, response.getVersion());
        assertEquals(remoteParticipant, response.getRecipient());
        assertArrayEquals(HS_RESPONSE.getBytes(), response.getContent(TYP));
        assertArrayEquals(localParticipant.getPublicKeyAsBytes(), response.getContent(PUBLIC_KEY));
        assertEquals(NONCE_LENGTH_IN_BYTES, responder.localNonce.length);
        assertTrue(responder.localSignature.length >= MINIMAL_SIGNATURE_LENGTH_IN_BYTES);
        assertTrue(responder.localSignature.length <= MAXIMAL_SIGNATURE_LENGTH_IN_BYTES);

        byte[] localDigest = digest(localParticipant, responder.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, response.getContent(SIGNATURE), localParticipant.getPublicKey()));
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceResponseOnInvokingMethodBeforeConsumeChallenge() {
        assertFalse(responder.wasConsumeChallengeInvoked);
        assertFalse(responder.wasProduceResponseInvoked);
        responder.produceResponse();
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceResponseOnInvokingMethodTwice() {
        testConsumeChallenge(); // to set responder into needed state

        assertFalse(responder.wasProduceResponseInvoked);
        responder.produceResponse();
        assertTrue(responder.wasProduceResponseInvoked);
        responder.produceResponse(); // should throw the exception
    }

    @Test
    public void testConsumeSuccess() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.putContent(SIGNATURE, calculateRemoteSignature());
        responder.consumeSuccess(success);

        byte[] sessionKey = calculateSessionKey(remoteNonce, responder.localNonce);
        assertArrayEquals(sessionKey, responder.getSessionKey());
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnNull() {
        testProduceResponse(); // Set the responder into the needed state.

        responder.consumeSuccess(null);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnWrongVersion() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.setVersion(VERSION + ".1");
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnMissingType() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.getContent().remove(TYP.toString());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnWrongType() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.putContent(TYP, FORWARD.getBytes());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnMissingPhase() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.getContent().remove(TYP.toString());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnWrongPhase() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.putContent(TYP, HS_CHALLENGE.getBytes());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnMissingPublicKey() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.getContent().remove(PUBLIC_KEY.toString());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnEmptyPublicKey() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.putContent(PUBLIC_KEY, new byte[]{});
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnMissingSignature() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.getContent().remove(SIGNATURE.toString());
        responder.consumeSuccess(success);
    }

    /**
     * This tests if the signature length can be too far away from the usual
     * length. Since the length can change for about a few bytes (depending on
     * randomness in {@link EccSigner}), this can't be tested on one specific
     * length.
     */
    @Test
    public void testConsumeSuccessOnWrongSignatureLength() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        ArrayList<Byte> signature = new ArrayList<>();

        for (int length = 0; length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES * 2; length++) {
            success.putContent(SIGNATURE, toBytes(signature));

            try {
                responder.wasConsumeSuccessInvoked = false;
                responder.consumeSuccess(success);

                if (length > MINIMAL_SIGNATURE_LENGTH_IN_BYTES
                        && length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrown.");
                }
            } catch (HandshakeException ex) {
            }

            signature.add((byte) length);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeSuccessOnInvokingMethodTwice() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.putContent(SIGNATURE, calculateRemoteSignature());

        assertFalse(responder.wasConsumeSuccessInvoked);
        responder.consumeSuccess(success);
        assertTrue(responder.wasConsumeSuccessInvoked);
        responder.consumeSuccess(success); // should throw the exception
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeSuccessOnInvokingMethodBeforeConsumeChallenge() {
        Message success = getBasicSuccess();
        responder.consumeSuccess(success);
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeSuccessOnInvokingMethodBeforeProduceResponse() {
        testConsumeChallenge();

        Message success = getBasicSuccess();
        responder.consumeSuccess(success);
    }

    private Message getBasicSuccess() {
        Message success = new Message(HS_SUCCESS, localParticipant);
        success.putContent(PUBLIC_KEY, localParticipant.getPublicKeyAsBytes());
        success.putContent(SIGNATURE, new byte[MINIMAL_SIGNATURE_LENGTH_IN_BYTES]);

        return success;
    }

    private byte[] calculateRemoteSignature() {
        remoteSignature = sign(fullRemoteParticipant, remoteNonce, responder.localNonce);
        return remoteSignature;
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        responder.getSessionKey();
    }

    @Test
    public void testGetSessionKey() {
        byte[] testKey = new byte[]{1, 2, 3};
        responder.sessionKey = testKey;
        assertSame(testKey, responder.getSessionKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRemoteParticipantWhenNotAvailable() {
        responder.remoteParticipant = null;
        responder.getRemoteParticipant();
    }

    @Test
    public void testGetRemoteParticipant() {
        responder.remoteParticipant = remoteParticipant;
        assertSame(remoteParticipant, responder.getRemoteParticipant());
    }

}
