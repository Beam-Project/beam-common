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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Message.VERSION;
import static org.beamproject.common.message.Field.Cnt.*;
import static org.beamproject.common.message.Field.Cnt.Typ.*;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;

/**
 * Allows to negotiate authentication between {@link Participant}s. The
 * {@link HandshakeResponder} is used by the one who reacts to an incoming
 * {@link HandshakeChallenger}.
 *
 * @see Handshake
 * @see HandshakeChallenger
 */
public class HandshakeResponder extends Handshake {

    Message response;
    boolean wasConsumeChallengeInvoked = false;
    boolean wasProduceResponseInvoked = false;
    boolean wasConsumeSuccessInvoked = false;

    /**
     * Allows to negotiate authentication between {@link Participant}s. The
     * {@link HandshakeResponder} is used by the one who reacts to an incoming
     * {@link HandshakeChallenger}.
     *
     * @param localParticipant The local {@link Participant} with both
     * {@link PublicKey} and {@link PrivateKey}.
     * @throws IllegalArgumentException If the argument is null.
     */
    public HandshakeResponder(Participant localParticipant) {
        super(localParticipant);
    }

    /**
     * Consumes the {@code CHALLENGE}, generated with
     * {@link HandshakeChallenger}.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeResponder} for security reasons since authentication is
     * critical.
     *
     * @param challenge The challenge to consume.
     * @throws IllegalStateException If the method is invoked more than once.
     * @throws HandshakeException If the challenge is null, does not contain all
     * needed fields, if there are invalid fields or if this method is invoked
     * more than once.
     */
    public void consumeChallenge(Message challenge) {
        verifyChallengeCusumptionAuthorization();
        verifyChallengeValidity(challenge);

        KeyPair remoteKeyPair = EccKeyPairGenerator.fromPublicKey(challenge.getContent(HS_PUBKEY));
        remoteParticipant = new Participant(remoteKeyPair);
        remoteNonce = challenge.getContent(HS_NONCE);
    }

    private void verifyChallengeCusumptionAuthorization() {
        if (wasConsumeChallengeInvoked) {
            throw new IllegalStateException("This method can only be invoked once on the same instance.");
        }

        wasConsumeChallengeInvoked = true;
    }

    private void verifyChallengeValidity(Message challenge) {
        String exceptionMessage = "The challenge is invalid: ";

        if (challenge == null) {
            exceptionMessage += "challenge is null";
        } else if (challenge.getVersion() == null
                || !challenge.getVersion().equals(VERSION)) {
            exceptionMessage += "version not set or unknown";
        } else if (challenge.getRecipient() == null) {
            exceptionMessage += "participant not set";
        } else if (!challenge.containsContent(TYP)
                || challenge.getContent(TYP) == null
                || !HS_CHALLENGE.toString().equals(new String(challenge.getContent(TYP)))) {
            exceptionMessage += "phase not set or an unexpected one";
        } else if (!challenge.containsContent(HS_PUBKEY)
                || challenge.getContent(HS_PUBKEY) == null
                || challenge.getContent(HS_PUBKEY).length == 0) {
            exceptionMessage += "challenger public key not set";
        } else if (!challenge.containsContent(HS_NONCE)
                || challenge.getContent(HS_NONCE) == null
                || challenge.getContent(HS_NONCE).length != NONCE_LENGTH_IN_BYTES) {
            exceptionMessage += "challenger nonce not set or has invalid length";
        } else {
            return;
        }

        throw new HandshakeException(exceptionMessage);
    }

    /**
     * Produces the {@code RESPONSE} to {@code CHALLENGE}.
     * <p>
     * Before this method, {@code consumeChallenge(..)} has to be invoked.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeResponder} for security reasons since authentication is
     * critical.
     *
     * @return The {@code RESPONSE} message.
     * @throws IllegalStateException If the method {@code consumeChallenge} was
     * not invoked before or this method is invoked more than once.
     */
    public Message produceResponse() {
        verifyResponseProductionAuthorization();

        generateLocalNonce();
        calculateLocalSignature();
        assembleResponseMessage();

        return response;
    }

    private void verifyResponseProductionAuthorization() {
        if (!wasConsumeChallengeInvoked || wasProduceResponseInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the challenge was consumed and may only be once invoked "
                    + "on the same instance.");
        }

        wasProduceResponseInvoked = true;
    }

    private void assembleResponseMessage() {
        response = new Message(HS_RESPONSE, remoteParticipant);
        response.putContent(HS_PUBKEY, localParticipant.getPublicKeyAsBytes());
        response.putContent(HS_NONCE, localNonce);
        response.putContent(HS_SIG, localSignature);
    }

    /**
     * Consumes the {@code SUCCESS}, generated with {@link HandshakeChallenger}.
     * <p>
     * Before this method, {@code consumeChallenge(..)} and
     * {@code produceResponse(..)} have to be invoked.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeResponder} for security reasons since authentication is
     * critical.
     *
     * @param success The success to consume.
     * @throws IllegalStateException If the method {@code produceResponse} was
     * not invoked before or this method is invoked more than once.
     * @throws HandshakeException If the success is null, does not contain all
     * needed fields or if there are invalid fields.
     */
    public void consumeSuccess(Message success) {
        verifySuccessCusumptionAuthorization();
        verifySuccessValidity(success);

        remoteSignature = success.getContent(HS_SIG);
        verifyRemoteSignature();
        calculateSessionKey();
    }

    private void verifySuccessCusumptionAuthorization() {
        if (!wasConsumeChallengeInvoked
                || !wasProduceResponseInvoked
                || wasConsumeSuccessInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the challenge was consumed and the response produced and "
                    + "may only be once invoked on the same instance.");
        }

        wasConsumeSuccessInvoked = true;
    }

    private void verifySuccessValidity(Message success) {
        String exceptionMessage = "The success is invalid: ";

        if (success == null) {
            exceptionMessage += "success is null";
        } else if (success.getVersion() == null
                || !success.getVersion().equals(VERSION)) {
            exceptionMessage += "version not set or unknown";
        } else if (success.getRecipient() == null) {
            exceptionMessage += "participant not set";
        } else if (!success.containsContent(TYP)
                || success.getContent(TYP) == null
                || !HS_SUCCESS.toString().equals(new String(success.getContent(TYP)))) {
            exceptionMessage += "phase not set or an unexpected one";
        } else if (!success.containsContent(HS_PUBKEY)
                || success.getContent(HS_PUBKEY) == null
                || success.getContent(HS_PUBKEY).length == 0) {
            exceptionMessage += "challenger public key not set";
        } else if (!success.containsContent(HS_SIG)
                || success.getContent(HS_SIG) == null
                || success.getContent(HS_SIG).length > MAXIMAL_SIGNATURE_LENGTH_IN_BYTES
                || success.getContent(HS_SIG).length < MINIMAL_SIGNATURE_LENGTH_IN_BYTES) {
            exceptionMessage += "challenger signature not set or has invalid length";
        } else {
            return;
        }

        throw new HandshakeException(exceptionMessage);
    }

    @Override
    protected void calculateSessionKey() {
        byte[] merged = Arrays.mergeArrays(remoteNonce, localNonce);
        sessionKey = Digest.digestWithSha256(merged);
    }

    /**
     * Gets the remote {@link Participant}, if available. It is only filled with
     * the {@link  PublicKey}.
     *
     * @return The remote {@link Participant}.
     * @throws IllegalStateException If the {@link Participant} is not available
     * at the time of invocation.
     */
    @Override
    public Participant getRemoteParticipant() {
        if (remoteParticipant == null) {
            throw new IllegalStateException("The remote participant is not available at this moment.");
        }

        return remoteParticipant;
    }

}
