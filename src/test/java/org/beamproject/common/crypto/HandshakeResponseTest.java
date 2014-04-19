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
import org.beamproject.common.Message;
import static org.beamproject.common.Message.VERSION;
import static org.beamproject.common.MessageField.*;
import static org.beamproject.common.crypto.HandshakeResponse.*;
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

    @Test
    public void testConsumeChallenge() {
        Message challenge = getBasicChallenge();
        responder.consumeChallenge(challenge);

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
    public void testConsumeChallengeOnMissingParticipant() {
        Message challenge = new Message();
        challenge.setVersion(VERSION);
        challenge.putContent(CNT_CRPHASE, CHALLENGE.getBytes());
        challenge.putContent(CNT_CRPUBKEY, remoteParticipant.getPublicKeyAsBytes());
        challenge.putContent(CNT_CRNONCE, generateNonce());

        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingPhase() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(CNT_CRPHASE.toString());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnWrongPhase() {
        Message challenge = getBasicChallenge();
        challenge.putContent(CNT_CRPHASE, SUCCESS.getBytes());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingPublicKey() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(CNT_CRPUBKEY.toString());
        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnEmptyPublicKey() {
        Message challenge = getBasicChallenge();
        challenge.putContent(CNT_CRPUBKEY, new byte[]{});

        responder.consumeChallenge(challenge);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnMissingNonce() {
        Message challenge = getBasicChallenge();
        challenge.getContent().remove(CNT_CRNONCE.toString());
        responder.consumeChallenge(challenge);
    }

    @Test
    public void testConsumeChallengeOnWrongNonceLength() {
        Message challenge = getBasicChallenge();

        ArrayList<Byte> nonce = new ArrayList<>();
        for (int length = 0; length < NONCE_LENGTH_IN_BYTES * 2; length++) {
            byte[] copy = new byte[length];

            for (Byte value : nonce) {
                copy[length] = value;
            }

            challenge.putContent(CNT_CRNONCE, copy);

            try {
                responder.consumeChallenge(challenge);

                if (length != NONCE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrwon.");
                }
            } catch (HandshakeException ex) {
            }
        }
    }

    private Message getBasicChallenge() {
        remoteNonce = generateNonce();
        Message challenge = new Message();
        challenge.setVersion(VERSION);
        challenge.setRecipient(localParticipant);
        challenge.putContent(CNT_CRPHASE, CHALLENGE.getBytes());
        challenge.putContent(CNT_CRPUBKEY, remoteParticipant.getPublicKeyAsBytes());
        challenge.putContent(CNT_CRNONCE, remoteNonce);

        return challenge;
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeChallengeOnInvokingMethodTwice() {
        Message challenge = new Message();
        challenge.setVersion(VERSION);
        challenge.setRecipient(localParticipant);
        challenge.putContent(CNT_CRPHASE, CHALLENGE.getBytes());
        challenge.putContent(CNT_CRPUBKEY, remoteParticipant.getPublicKeyAsBytes());
        challenge.putContent(CNT_CRNONCE, generateNonce());

        assertFalse(responder.wasConsumeChallengeInvoked);
        responder.consumeChallenge(challenge);
        assertTrue(responder.wasConsumeChallengeInvoked);
        responder.consumeChallenge(challenge); // should throw the exception
    }

    @Test
    public void testProduceResponse() {
        testConsumeChallenge(); // To set the response into the needed state.
        Message response = responder.produceResponse();

        assertEquals(VERSION, response.getVersion());
        assertEquals(remoteParticipant, response.getRecipient());
        assertEquals(RESPONSE.toString(), new String(response.getContent(CNT_CRPHASE)));
        assertEquals(NONCE_LENGTH_IN_BYTES, responder.localNonce.length);
        assertTrue(responder.localSignature.length >= MINIMAL_SIGNATURE_LENGTH_IN_BYTES);
        assertTrue(responder.localSignature.length <= MAXIMAL_SIGNATURE_LENGTH_IN_BYTES);

        byte[] localDigest = digest(localParticipant, responder.localNonce, remoteNonce);
        assertTrue(signer.verify(localDigest, response.getContent(CNT_CRSIG), localParticipant.getPublicKey()));
    }

    @Test(expected = IllegalStateException.class)
    public void testProduceResponseOnInvokingMethodBeforeConsumeChallenge() {
        assertFalse(responder.wasConsumeChallengeInvoked);
        assertFalse(responder.wasProduceResponseInvoked);
        responder.produceResponse();
    }

    @Test(expected = HandshakeException.class)
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

        remoteSignature = sign(fullRemoteParticipant, remoteNonce, responder.localNonce);
        byte[] sessionKey = calculateSessionKey(remoteNonce, responder.localNonce);

        Message success = getBasicSuccess();
        success.putContent(CNT_CRSIG, remoteSignature);
        responder.consumeSuccess(success);

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
        responder.consumeChallenge(success);
    }

    @Test(expected = HandshakeException.class)
    public void testSuccessChallengeOnMissingPhase() {
        testProduceResponse(); // Set the responder into needed state

        Message success = getBasicSuccess();
        success.putContent(CNT_CRPHASE, CHALLENGE.getBytes());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnMissingSignature() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.getContent().remove(CNT_CRSIG.toString());
        responder.consumeSuccess(success);
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnEmptySignature() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.putContent(CNT_CRSIG, new byte[0]);
        responder.consumeSuccess(success);
    }

    /**
     * This tests if the signature length can be too far away from the usual
     * length. Since the length can change for about a few bytes (depending on
     * the random in the {@link EccSigner}), this can't be tested on one
     * specific length.
     */
    @Test
    public void testConsumeSuccessOnWrongNonceLength() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.putContent(CNT_CRSIG, calculateRemoteSignature());
        ArrayList<Byte> signature = new ArrayList<>();

        for (int length = 0; length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES * 2; length++) {
            byte[] copy = new byte[length];

            for (Byte value : signature) {
                copy[length] = value;
            }

            success.putContent(CNT_CRSIG, copy);

            try {
                responder.consumeSuccess(success);

                if (length > MINIMAL_SIGNATURE_LENGTH_IN_BYTES
                        && length < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES) {
                    fail("An exception should have been thrwon.");
                }
            } catch (HandshakeException ex) {
            }
        }
    }

    @Test(expected = HandshakeException.class)
    public void testConsumeSuccessOnInvokingMethodTwice() {
        testProduceResponse(); // Set the responder into needed state.

        Message success = getBasicSuccess();
        success.putContent(CNT_CRSIG, calculateRemoteSignature());

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
        Message success = new Message();
        success.setVersion(VERSION);
        success.setRecipient(localParticipant);
        success.putContent(CNT_CRPHASE, SUCCESS.getBytes());
        success.putContent(CNT_CRSIG, new byte[MINIMAL_SIGNATURE_LENGTH_IN_BYTES]);

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

}
