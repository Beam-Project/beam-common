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
import lombok.Getter;
import org.beamproject.common.message.Message;
import static org.beamproject.common.message.MessageField.ContentField.*;
import static org.beamproject.common.message.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

/**
 * This handshake protocol uses the involved {@link Participant} signatures to
 * authenticate both sides. During this process, a session id is being generated
 * which can be used for later communication.
 * <p>
 * {@link EccSigner} is used for signing.
 *
 * @see HandshakeChallenger
 * @see HandshakeResponder
 * @see EccSigner
 */
public abstract class Handshake {

    /**
     * The required length of the nonces used when doing the handshake.
     */
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
        Message message = new Message(HS_INVALIDATE, remoteParticipant);
        message.putContent(HSKEY, sessionKey);
        return message;
    }

}
