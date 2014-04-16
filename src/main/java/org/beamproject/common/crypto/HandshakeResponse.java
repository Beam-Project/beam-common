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
import java.util.Map;
import org.beamproject.common.Message;
import org.beamproject.common.MessageField;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

public class HandshakeResponse extends Handshake {

    Message responseChallenge;

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

    public void consumeInitChallenge(Message initChallenge) {
        Exceptions.verifyArgumentNotNull(initChallenge);

        Map<String, byte[]> initChallengeContent = initChallenge.getContent();
        KeyPair remoteKeyPair = EccKeyPairGenerator.restoreFromPublicKeyBytes(initChallengeContent.get(MessageField.CNT_CRPUBKEY.toString()));

        remoteParticipant = new Participant(remoteKeyPair);
        remoteNonce = initChallengeContent.get(MessageField.CNT_CRNONCE.toString());
    }

    public Message produceResponseChallenge() {
        generateLocalNonce();
        calculateLocalSignature();
        createResponseChallenge();

        return responseChallenge;
    }

    private void createResponseChallenge() {
        responseChallenge = new Message();
        responseChallenge.setVersion(Message.DEFAUTL_VERSION);
        responseChallenge.setParticipant(remoteParticipant);
        responseChallenge.appendContent(MessageField.CNT_CRPHASE, Handshake.Phase.RESPONSE_CHALLENGE.getBytes());
        responseChallenge.appendContent(MessageField.CNT_CRNONCE, localNonce);
        responseChallenge.appendContent(MessageField.CNT_CRSIG, localSignature);
    }

    public void consumeResponseDone(Message responseDone) {
        Exceptions.verifyArgumentNotNull(responseDone);

        Map<String, byte[]> responseDoneContent = responseDone.getContent();
        remoteSignature = responseDoneContent.get(MessageField.CNT_CRSIG.toString());

        verifyRemoteSignature();
        calculateSessionKey();
    }

    @Override
    protected void calculateSessionKey() {
        byte[] merged = Arrays.mergeArrays(remoteNonce, localNonce);
        sessionKey = Digest.digestWithSha256(merged);
    }

}
