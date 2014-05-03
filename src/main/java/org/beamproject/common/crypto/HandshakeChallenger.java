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

import java.security.PrivateKey;
import java.security.PublicKey;
import org.beamproject.common.Message;
import static org.beamproject.common.Message.VERSION;
import static org.beamproject.common.MessageField.ContentField.*;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.HandshakeResponder.*;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

/**
 * Allows to negotiate authentication between {@link Participant}s. The
 * {@link HandshakeChallenger} is used by the one who wants to establish
 * authenticity.
 *
 * @see Handshake
 * @see HandshakeResponder
 */
public class HandshakeChallenger extends Handshake {

    Message challenge;
    Message success;
    boolean wasProduceChallengeInvoked = false;
    boolean wasConsumeResponseInvoked = false;
    boolean wasProduceSuccessInvoked = false;

    /**
     * Allows to negotiate authentication between {@link Participant}s. The
     * {@link HandshakeChallenger} is used by the one who wants to establish
     * authenticity.
     *
     * @param localParticipant The local {@link Participant} with both
     * {@link PublicKey} and {@link PrivateKey}.
     * @throws IllegalArgumentException If the argument is null.
     */
    public HandshakeChallenger(Participant localParticipant) {
        super(localParticipant);
    }

    /**
     * Produces the {@code CHALLENGE}.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeChallenger} for security reasons since authentication is
     * critical.
     *
     * @param remoteParticipant The {@link Participant} between whom
     * authenticity should be established.
     * @return The {@code CHALLENGE} message.
     * @throws IllegalArgumentException If the argument is null.
     * @throws IllegalStateException If this method is invoked more than once.
     */
    public Message produceChallenge(Participant remoteParticipant) {
        Exceptions.verifyArgumentsNotNull(remoteParticipant);
        verifyChallengeProductionAuthorization();

        this.remoteParticipant = remoteParticipant;

        generateLocalNonce();
        assembleChallengeMessage();

        return challenge;
    }

    private void verifyChallengeProductionAuthorization() {
        if (wasProduceChallengeInvoked) {
            throw new IllegalStateException("This method can only be invoked once "
                    + "on the same instance.");
        }

        wasProduceChallengeInvoked = true;
    }

    private void assembleChallengeMessage() {
        challenge = new Message(remoteParticipant);
        challenge.putContent(HSPHASE, CHALLENGE.getBytes());
        challenge.putContent(HSPUBKEY, localParticipant.getPublicKeyAsBytes());
        challenge.putContent(HSNONCE, localNonce);
    }

    /**
     * Consumes the {@code RESPONSE}, generated with {@link HandshakeResponder}.
     * <p>
     * Before this method, {@code produceResponse(..)} has to be invoked.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeChallenger} for security reasons since authentication is
     * critical.
     *
     * @param challenge The response to consume.
     * @throws IllegalStateException If the method {@code produceResponce} was
     * not invoked before or this method is invoked more than once.
     * @throws HandshakeException If the response is null, does not contain all
     * needed fields or if there are invalid fields.
     */
    public void consumeResponse(Message challenge) {
        verifyResponseCusumptionAuthorization();
        verifyResponseValidity(challenge);

        remoteNonce = challenge.getContent(HSNONCE);
        remoteSignature = challenge.getContent(HSSIG);

        verifyRemoteSignature();
    }

    private void verifyResponseCusumptionAuthorization() {
        if (!wasProduceChallengeInvoked || wasConsumeResponseInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the challenge was produced and may only be once invoked "
                    + "on the same instance.");
        }

        wasConsumeResponseInvoked = true;
    }

    private void verifyResponseValidity(Message response) {
        String exceptionMessage = "The response is invalid: ";

        if (response == null) {
            exceptionMessage += "response is null";
        } else if (response.getVersion() == null
                || !response.getVersion().equals(VERSION)) {
            exceptionMessage += "version not set or unknown";
        } else if (response.getRecipient() == null) {
            exceptionMessage += "participant not set";
        } else if (!response.containsContent(HSPHASE)
                || response.getContent(HSPHASE) == null
                || !RESPONSE.toString().equals(new String(response.getContent(HSPHASE)))) {
            exceptionMessage += "phase not set or an unexpected one";
        } else if (!response.containsContent(HSNONCE)
                || response.getContent(HSNONCE) == null
                || response.getContent(HSNONCE).length != NONCE_LENGTH_IN_BYTES) {
            exceptionMessage += "responder nonce not set or has invalid length";
        } else if (!response.containsContent(HSSIG)
                || response.getContent(HSSIG) == null
                || response.getContent(HSSIG).length > MAXIMAL_SIGNATURE_LENGTH_IN_BYTES
                || response.getContent(HSSIG).length < MINIMAL_SIGNATURE_LENGTH_IN_BYTES) {
            exceptionMessage += "responder signature not set or has invalid length";
        } else {
            return;
        }

        throw new HandshakeException(exceptionMessage);
    }

    /**
     * Produces the {@code SUCCESS}.
     * <p>
     * Before this method, {@code consumeResponse(..)} has to be invoked.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeChallenger} for security reasons since authentication is
     * critical.
     *
     * @return The {@code SUCCESS} message.
     * @throws IllegalStateException If the method {@code consumeResponse} was
     * not invoked before or this method is invoked more than once.
     */
    public Message produceSuccess() {
        verifySuccessProductionAuthorization();

        calculateLocalSignature();
        assembleSuccessMessage();
        calculateSessionKey();

        return success;
    }

    private void verifySuccessProductionAuthorization() {
        if (!wasConsumeResponseInvoked || wasProduceSuccessInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the response was consumed and may only be once invoked "
                    + "on the same instance.");
        }

        wasProduceSuccessInvoked = true;
    }

    private void assembleSuccessMessage() {
        success = new Message(remoteParticipant);
        success.putContent(HSPHASE, SUCCESS.getBytes());
        success.putContent(HSSIG, localSignature);
    }

    @Override
    protected void calculateSessionKey() {
        byte[] merged = Arrays.mergeArrays(localNonce, remoteNonce);
        sessionKey = Digest.digestWithSha256(merged);
    }
}
