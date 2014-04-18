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

import java.security.PublicKey;
import java.security.SecureRandom;
import org.beamproject.common.Message;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

/**
 * This challenge response protocol uses the involved {@link Participant}
 * signatures to authenticate both sides. During this process, a session id is
 * generated which can be used for later communication.
 * <p>
 * {@link EccSigner} is used for signing.
 *
 * @see HandshakeChallenge
 * @see HandshakeResponse
 * @see EccSigner
 */
public abstract class Handshake {

    public final static int NUMBER_OF_CHALLENGE_BYTES = 128;

    /**
     * Lists all the allowed different phases in the Challenge-Response
     * protocol. Every {@link Message} has to contain exactly one {@link Phase}
     * value in its content.
     */
    public enum Phase {

        /**
         * PHASE 1 OF 3: The following is described from the point of view of
         * the participant who wants to establish an authenticated session.
         * <p>
         * {@code CHALLENGE} tells the other side that an authenticated session
         * should be established.
         * <p>
         * At this time, this side knows the other sides {@link PublicKey} and
         * therefore already can encrypt the first message.
         * <p>
         * This side has to send: The own {@link PublicKey} as bytes (for
         * identification and encryption when the the other side responses) and
         * as challenge a nonce as bytes of the length of
         * {@link AuthenticationSequence.NUMBER_OF_CHALLENGE_BYTES}.
         */
        CHALLENGE("CHALLENGE"),
        /**
         * PHASE 2 OF 3: The following is described from the point of view of
         * the participant who has been contacted by an unidentified participant
         * who wants to establish an authenticated session.
         * <p>
         * {@code RESPONSE} tells the other side that the challenge is accepted
         * and this side therefore is able to encryptedResponseChallenge.
         * <p>
         * At this time, this side knows the other sides {@link PublicKey} since
         * it was sent as part of phase 1, {@code CHALLENGE}. Also, this side
         * knows the challenge sent by the other side as a part of phase 1.
         * <p>
         * This side has to send: An own challenge (nonce as bytes) of the
         * length of {@link AuthenticationSequence.NUMBER_OF_CHALLENGE_BYTES}.
         * Further, this side has to calculate a digest of [the own public key +
         * the own nonce + the other sides nonce]. This digest has to be signed
         * with this sides private key. The resulting signature has to be sent
         * with the own nonce.
         */
        RESPONSE("RESPONSE"),
        /**
         * PHASE 3 OF 3: The following is described from the point of view of
         * the participant who wants to establish an authenticated session.
         * <p>
         * {@code SUCCESS} tells the other side that its during phase 1 sent
         * challenge is verified and the challenge, sent by the other side as
         * part of phase 2, is accepted and this side therefore is able to
         * respond.
         * <p>
         * At this time, this side knows the other sides {@link PublicKey}, the
         * own nonce (generated in phase 1) and the other sides nonce since it
         * was sent in phase 2. This side also knows the other sides signature,
         * created and sent in phase 2.
         * <p>
         * This side has to send: The response to the challenge from phase 2.
         * Therefore, this side has to calculate a digest of [the own public key
         * + the own nonce + the other sides nonce]. (This sides nonce was
         * generated in phase 1.) This digest has to be signed with this sides
         * private key. The resulting signature has to be sent.
         */
        SUCCESS("SUCCESS"),
        /**
         * {@code FAILURE} tells the other side that something went wrong during
         * the authentication process. This could be a wrong key, a wrong phase,
         * a delay, etc..
         * <p>
         * When a FAILURE is sent, the authentication process has to be
         * restarted from the side how wants to establish authenticity.
         */
        FAILURE("FAILURE");

        private final String value;

        private Phase(String value) {
            this.value = value;
        }

        public byte[] getBytes() {
            return value.getBytes();
        }

        public static Phase valueOf(byte[] value) {
            return valueOf(new String(value));
        }
    }
    Participant localParticipant;
    Participant remoteParticipant;
    byte[] localNonce;
    byte[] remoteNonce;
    byte[] localSignature;
    byte[] remoteSignature;
    EccSigner signer;
    Message responseDone;
    byte[] sessionKey;

    protected Handshake(Participant localParticipant) {
        Exceptions.verifyArgumentsNotNull(localParticipant);

        this.localParticipant = localParticipant;
        signer = new EccSigner();
    }

    protected void generateLocalNonce() {
        localNonce = new byte[NUMBER_OF_CHALLENGE_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(localNonce);
    }

    protected void calculateLocalSignature() {
        byte[] merged = Arrays.mergeArrays(localParticipant.getPublicKeyAsBytes(), localNonce, remoteNonce);
        byte[] digest = Digest.digestWithSha256(merged);
        localSignature = signer.sign(digest, localParticipant.getPrivateKey());
    }

    protected void verifyRemoteSignature() {
        byte[] merged = Arrays.mergeArrays(remoteParticipant.getPublicKeyAsBytes(), remoteNonce, localNonce);
        byte[] digest = Digest.digestWithSha256(merged);

        if (!signer.verify(digest, remoteSignature, remoteParticipant.getPublicKey())) {
            throw new ChallengeResponseException("Could not verify the correctness of the remote signature.");
        }
    }

    protected abstract void calculateSessionKey();

    /**
     * Returns the negotiated session key. This should only be invoked after the
     * authentication process succeeded.
     * <p>
     * The session key is a SHA-256 {@link Digest} of [localNonce +
     * remoteNonce].
     *
     * @return The negotiated session key.
     * @throws IllegalStateException If this method is invoked before
     * {@link ChallengeResponse.authenticate()}.
     */
    public byte[] getSessionKey() {
        if (sessionKey == null) {
            throw new IllegalStateException("The authentication process has to be completed first.");
        }

        return sessionKey;
    }

}
