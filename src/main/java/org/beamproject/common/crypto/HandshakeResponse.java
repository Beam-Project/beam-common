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
import org.beamproject.common.MessageField;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Arrays;
import org.beamproject.common.util.Exceptions;

public class HandshakeResponse extends Handshake {

    Message response;

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

    public void consumeChallenge(Message challenge) {
        Exceptions.verifyArgumentsNotNull(challenge);

        KeyPair remoteKeyPair = EccKeyPairGenerator.fromPublicKey(challenge.getContent(MessageField.CNT_CRPUBKEY));
        remoteParticipant = new Participant(remoteKeyPair);
        remoteNonce = challenge.getContent(MessageField.CNT_CRNONCE);
    }

    public Message produceResponse() {
        generateLocalNonce();
        calculateLocalSignature();
        createResponse();

        return response;
    }

    private void createResponse() {
        response = new Message();
        response.setVersion(Message.DEFAUTL_VERSION);
        response.setParticipant(remoteParticipant);
        response.appendContent(MessageField.CNT_CRPHASE, Handshake.Phase.RESPONSE.getBytes());
        response.appendContent(MessageField.CNT_CRNONCE, localNonce);
        response.appendContent(MessageField.CNT_CRSIG, localSignature);
    }

    public void consumeSuccess(Message done) {
        Exceptions.verifyArgumentsNotNull(done);

        remoteSignature = done.getContent(MessageField.CNT_CRSIG);
        verifyRemoteSignature();
        calculateSessionKey();
    }

    @Override
    protected void calculateSessionKey() {
        byte[] merged = Arrays.mergeArrays(remoteNonce, localNonce);
        sessionKey = Digest.digestWithSha256(merged);
    }

}
