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
import org.beamproject.common.Message;
import static org.beamproject.common.Message.VERSION;
import static org.beamproject.common.MessageField.*;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;

/**
 * Allows to negotiate authentication between {@link Participant}s. The
 * {@link HandshakeResponse} is used by the one who reacts to an incoming
 * {@link HandshakeChallenge}.
 *
 * @see Handshake
 * @see HandshakeChallenge
 */
public class HandshakeResponse extends Handshake {

    /**
     * The minimal length of the signature sent by the client. Since the length
     * varies, a spectrum has to be defined.
     */
    final static int MINIMAL_SIGNATURE_LENGTH_IN_BYTES = 96;
    /**
     * The maximal length of the signature sent by the client. Since the length
     * varies, a spectrum has to be defined.
     */
    final static int MAXIMAL_SIGNATURE_LENGTH_IN_BYTES = 128;
    Message response;
    boolean wasConsumeChallengeInvoked = false;
    boolean wasProduceResponseInvoked = false;
    boolean wasConsumeSuccessInvoked = false;

    /**
     * Allows to negotiate authentication between {@link Participant}s. The
     * {@link HandshakeResponse} is used by the one who reacts to an incoming
     * {@link HandshakeChallenge}.
     *
     * @param localParticipant The local {@link Participant} with both
     * {@link PublicKey} and {@link PrivateKey}.
     * @throws IllegalArgumentException If the argument is null.
     */
    public HandshakeResponse(Participant localParticipant) {
        super(localParticipant);
    }

    /**
     * Consumes the {@code CHALLENGE}, generated with
     * {@link HandshakeChallenge}.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeResponse} for security reasons since authentication is
     * critical.
     *
     * @param challenge The challenge to consume.
     * @throws HandshakeException If the challenge is null, does not contain all
     * needed fields, if they are invalid fields or if this method is invoked
     * more than once.
     */
    public void consumeChallenge(Message challenge) {
        verifyChallengeCusumptionAuthorization();
        verifyChallengeValidity(challenge);

        KeyPair remoteKeyPair = EccKeyPairGenerator.fromPublicKey(challenge.getContent(CNT_CRPUBKEY));
        remoteParticipant = new Participant(remoteKeyPair);
        remoteNonce = challenge.getContent(CNT_CRNONCE);
    }

    private void verifyChallengeCusumptionAuthorization() {
        if (wasConsumeChallengeInvoked) {
            throw new HandshakeException("This method can only be invoked once on the same instance.");
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
        } else if (challenge.getParticipant() == null) {
            exceptionMessage += "participant not set";
        } else if (!challenge.containsContent(CNT_CRPHASE)
                || challenge.getContent(CNT_CRPHASE) == null
                || !CHALLENGE.toString().equals(new String(challenge.getContent(CNT_CRPHASE)))) {
            exceptionMessage += "phase not set or an unexpected one";
        } else if (!challenge.containsContent(CNT_CRPUBKEY)
                || challenge.getContent(CNT_CRPUBKEY) == null
                || challenge.getContent(CNT_CRPUBKEY).length == 0) {
            exceptionMessage += "challenger public key not set";
        } else if (!challenge.containsContent(CNT_CRNONCE)
                || challenge.getContent(CNT_CRNONCE) == null
                || challenge.getContent(CNT_CRNONCE).length != NONCE_LENGTH_IN_BYTES) {
            exceptionMessage += "challenger nonce not set";
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
     * {@link HandshakeResponse} for security reasons since authentication is
     * critical.
     *
     * @return The {@code RESPONSE} message.
     * @throws IllegalStateException If the method {@code consumeChallenge} was
     * not invoked before.
     * @throws HandshakeException If this method is invoked more than once.
     */
    public Message produceResponse() {
        verifyResponseProductionAuthorization();

        generateLocalNonce();
        calculateLocalSignature();
        assembleResponseMessage();

        return response;
    }

    private void verifyResponseProductionAuthorization() {
        if (!wasConsumeChallengeInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the challenge was consumed.");
        }

        if (wasProduceResponseInvoked) {
            throw new HandshakeException("This method can only be invoked once "
                    + "on the same instance.");
        }

        wasProduceResponseInvoked = true;
    }

    private void assembleResponseMessage() {
        response = new Message();
        response.setVersion(VERSION);
        response.setParticipant(remoteParticipant);
        response.appendContent(CNT_CRPHASE, RESPONSE.getBytes());
        response.appendContent(CNT_CRNONCE, localNonce);
        response.appendContent(CNT_CRSIG, localSignature);
    }

    /**
     * Consumes the {@code SUCCESS}, generated with {@link HandshakeChallenge}.
     * <p>
     * Before this method, {@code consumeChallenge(..)} and
     * {@code produceResponse(..)} have to be invoked.
     * <p>
     * This method can only be invoked once per instance of
     * {@link HandshakeResponse} for security reasons since authentication is
     * critical.
     *
     * @param success The success to consume.
     * @throws HandshakeException If the challenge is null, does not contain all
     * needed fields, if they are invalid fields or if this method is invoked
     * more than once.
     */
    public void consumeSuccess(Message success) {
        verifySuccessCusumptionAuthorization();
        verifySuccessValidity(success);

        remoteSignature = success.getContent(CNT_CRSIG);
        verifyRemoteSignature();
        calculateSessionKey();
    }

    private void verifySuccessCusumptionAuthorization() {
        if (!wasConsumeChallengeInvoked || !wasProduceResponseInvoked) {
            throw new IllegalStateException("This method has to be invoked after "
                    + "the challenge was consumed and the response produced.");
        }

        if (wasConsumeSuccessInvoked) {
            throw new HandshakeException("This method can only be invoked once "
                    + "on the same instance.");
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
        } else if (success.getParticipant() == null) {
            exceptionMessage += "participant not set";
        } else if (!success.containsContent(CNT_CRPHASE)
                || success.getContent(CNT_CRPHASE) == null
                || !SUCCESS.toString().equals(new String(success.getContent(CNT_CRPHASE)))) {
            exceptionMessage += "phase not set or an unexpected one";
        } else if (!success.containsContent(CNT_CRSIG)
                || success.getContent(CNT_CRSIG) == null
                || success.getContent(CNT_CRSIG).length > MAXIMAL_SIGNATURE_LENGTH_IN_BYTES
                || success.getContent(CNT_CRSIG).length < MINIMAL_SIGNATURE_LENGTH_IN_BYTES) {
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

}
