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
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.Handshake.NUMBER_OF_CHALLENGE_BYTES;
import org.beamproject.common.util.Arrays;
import org.junit.Before;

public class HandshakeTest {

    protected Participant localParticipant;
    protected Participant remoteParticipant;
    protected EccSigner signer;
    protected byte[] remoteNonce;
    protected byte[] remoteSignature;

    @Before
    public void setUp() {
        localParticipant = new Participant(EccKeyPairGenerator.generate());
        remoteParticipant = new Participant(EccKeyPairGenerator.generate());
        signer = new EccSigner();
    }

    protected byte[] generateNonce() {
        byte[] nonce = new byte[NUMBER_OF_CHALLENGE_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        return nonce;
    }

    protected byte[] sign(Participant signingParticipant, byte[] signersNonce, byte[] othersNonce) {
        byte[] digest = digest(signingParticipant, signersNonce, othersNonce);
        return signer.sign(digest, signingParticipant.getPrivateKey());
    }

    protected byte[] digest(Participant digestingParticipant, byte[] digestersNonce, byte[] othersNonce) {
        return Digest.digestWithSha256(Arrays.mergeArrays(digestingParticipant.getPublicKeyAsBytes(), digestersNonce, othersNonce));
    }

    protected byte[] calculateSessionKey(byte[] requesterNonce, byte[] requesteeNonce) {
        return Digest.digestWithSha256(Arrays.mergeArrays(requesterNonce, requesteeNonce));
    }

}
