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
import lombok.Getter;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

/**
 * This handshake protocol uses the involved {@link Participant} signatures to
 * authenticate both sides. During this process, a session id is generated which
 * can be used for later communication.
 * <p>
 * {@link EccSigner} is used for signing.
 *
 * @see HandshakeChallenger
 * @see HandshakeResponder
 * @see EccSigner
 */
public abstract class Handshake {

    /**
     * Lists all the allowed different phases in the handshake protocol. Every
     * {@link Message} has to contain exactly one {@link Phase} value in its
     * content.
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
         * identification and encryption when the the other side responds) and
         * as challenge a nonce as bytes of the length of
         * {@link Handshake.NONCE_LENGTH_IN_BYTES}.
         */
        CHALLENGE,
        /**
         * PHASE 2 OF 3: The following is described from the point of view of
         * the participant who has been contacted by an unidentified participant
         * who wants to establish an authenticated session.
         * <p>
         * {@code RESPONSE} tells the other side that the challenge was accepted
         * and this side therefore is able to send a challenge to which the
         * other side has to response to.
         * <p>
         * At this time, this side knows the other sides {@link PublicKey} since
         * it was sent as part of phase 1, {@code CHALLENGE}. Also, this side
         * knows the challenge sent by the other side as a part of phase 1.
         * <p>
         * This side has to send: An own challenge (nonce as bytes) of the
         * length of {@link Handshake.NONCE_LENGTH_IN_BYTES}. Further, this side
         * has to calculate a digest of [the own public key + the own nonce +
         * the other sides nonce], so the own public key followed by the own
         * nonce followed by the other sides nonce have to be concatenated to
         * one large array of bytes. This large array is then used to calculate
         * the digest. This digest has to be signed with this sides private key.
         * The resulting signature has to be sent with the own nonce.
         */
        RESPONSE,
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
         * + the own nonce + the other sides nonce], so the own public key
         * followed by the own nonce followed by the other sides nonce have to
         * be concatenated to one large array of bytes. This large array is then
         * used to calculate the digest. (This sides nonce was generated in
         * phase 1.) This digest has to be signed with this sides private key.
         * The resulting signature has to be sent.
         */
        SUCCESS,
        /**
         * {@code FAILURE} tells the other side that something went wrong during
         * the authentication process. This could be a wrong key, a wrong phase,
         * a too long delay, etc..
         * <p>
         * When a FAILURE is sent, the authentication process has to be
         * restarted from the side how wants to establish authenticity. All
         * values (nonces, etc.) have to be generated again.
         */
        FAILURE,
        /**
         * {@code INVALIDATE} tells the other side that an already established
         * session has to be invalidated an that it will no longer be valid.
         * <p>
         * This side has to send the session key to the other side.
         */
        INVALIDATE;

        /**
         * Returns the {@link Phase} constant with the specified name. The bytes
         * must match exactly the identifiers bytes.
         *
         *
         * @param value The byte representation of the requested {@link Phase}
         * constant.
         * @return The {@link Phase} constant with the specified name.
         * @throws IllegalArgumentException If there is no constant with the
         * specified name.
         */
        public static Phase valueOf(byte[] value) {
            return valueOf(new String(value));
        }

        /**
         * @return The bytes of the string representation of this value.
         */
        public byte[] getBytes() {
            return toString().getBytes();
        }
    }
    public final static int NONCE_LENGTH_IN_BYTES = 128;
    /**
     * The minimal length of the signature. Since the length varies depending on
     * randomness in, a spectrum has to be defined.
     */
    final static int MINIMAL_SIGNATURE_LENGTH_IN_BYTES = 96;
    /**
     * The maximal length of the signature. Since the length varies depending on
     * randomness in, a spectrum has to be defined.
     */
    final static int MAXIMAL_SIGNATURE_LENGTH_IN_BYTES = 128;
    @Getter
    Participant localParticipant;
    @Getter
    Participant remoteParticipant;
    byte[] localNonce;
    byte[] remoteNonce;
    byte[] localSignature;
    byte[] remoteSignature;
    EccSigner signer;
    byte[] sessionKey;

    protected Handshake(Participant localParticipant) {
        Exceptions.verifyArgumentsNotNull(localParticipant);

        this.localParticipant = localParticipant;
        signer = new EccSigner();
    }

    protected void generateLocalNonce() {
        localNonce = new byte[NONCE_LENGTH_IN_BYTES];
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
        boolean isRemoteSignatureVerified = false;

        try {
            isRemoteSignatureVerified = signer.verify(digest, remoteSignature, remoteParticipant.getPublicKey());
        } catch (IllegalStateException ex) {
            throw new HandshakeException("Could not verify the correctness of "
                    + "the remote signature since an error occurred: " + ex.getMessage());
        }

        if (!isRemoteSignatureVerified) {
            throw new HandshakeException("Could not verify the correctness of "
                    + "the remote signature.");
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
     * @throws IllegalStateException If this method is invoked before the
     * handshake is completed.
     */
    public byte[] getSessionKey() {
        if (sessionKey == null) {
            throw new IllegalStateException("The authentication process has to be completed first.");
        }

        return sessionKey;
    }

    /**
     * Creates an {@code INVALIDATE} {@link Message} with the given session key.
     *
     * @param remoteParticipant The participant with whom the session was
     * established.
     * @param sessionKey The established session key.
     * @return A message to invalidate the session.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public static Message getInvalidate(Participant remoteParticipant, byte[] sessionKey) {
        Exceptions.verifyArgumentsNotNull(remoteParticipant, sessionKey);
        Message message = new Message(HANDSHAKE, remoteParticipant);
        message.putContent(HSPHASE, Phase.INVALIDATE);
        message.putContent(HSKEY, sessionKey);
        return message;
    }

}
