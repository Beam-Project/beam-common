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
import org.beamproject.common.MessageField;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

public class HandshakeChallenge extends Handshake {

    Message challenge;

    /**
     * Allows to negotiate authentication between the given
     * {@link Participant}s. The {@link HandshakeChallenge} is used by the one
     * who initializes the authentication process.
     *
     * @param localParticipant The local {@link Participant} with both
     * {@link PublicKey} and {@link PrivateKey}.
     * @throws IllegalArgumentException If the argument is null.
     */
    public HandshakeChallenge(Participant localParticipant) {
        super(localParticipant);
    }

    public Message produceChallenge(Participant remoteParticipant) {
        Exceptions.verifyArgumentsNotNull(remoteParticipant);

        this.remoteParticipant = remoteParticipant;

        generateLocalNonce();
        createChallenge();

        return challenge;
    }

    private void createChallenge() {
        challenge = new Message();
        challenge.setVersion(Message.DEFAUTL_VERSION);
        challenge.setParticipant(remoteParticipant);
        challenge.appendContent(MessageField.CNT_CRPHASE, Phase.CHALLENGE.getBytes());
        challenge.appendContent(MessageField.CNT_CRPUBKEY, localParticipant.getPublicKeyAsBytes());
        challenge.appendContent(MessageField.CNT_CRNONCE, localNonce);
    }

    public void consumeResponse(Message challenge) {
        Exceptions.verifyArgumentsNotNull(challenge);

        if (!challenge.containsContent(MessageField.CNT_CRNONCE)
                || !challenge.containsContent(MessageField.CNT_CRSIG)) {
            throw new IllegalStateException("At this state, the response has to contain CRNONCE and CRSIG of the ohter side.");
        }

        remoteNonce = challenge.getContent(MessageField.CNT_CRNONCE);
        remoteSignature = challenge.getContent(MessageField.CNT_CRSIG);

        verifyRemoteSignature();
    }

    public Message produceSuccess() {
        calculateLocalSignature();
        createSuccess();
        calculateSessionKey();

        return responseDone;
    }

    private void createSuccess() {
        responseDone = new Message();
        responseDone.setVersion(Message.DEFAUTL_VERSION);
        responseDone.setParticipant(remoteParticipant);
        responseDone.appendContent(MessageField.CNT_CRPHASE, Phase.SUCCESS.getBytes());
        responseDone.appendContent(MessageField.CNT_CRSIG, localSignature);
    }

    @Override
    protected void calculateSessionKey() {
        byte[] merged = Arrays.mergeArrays(localNonce, remoteNonce);
        sessionKey = Digest.digestWithSha256(merged);
    }
}
