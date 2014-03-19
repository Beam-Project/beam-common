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
import java.security.SecureRandom;
import java.util.Map;
import static org.easymock.EasyMock.*;
import org.beamproject.common.Message;
import org.beamproject.common.MessageField;
import org.beamproject.common.Participant;
import org.beamproject.common.network.HttpConnector;
import org.beamproject.common.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ChallengeResponseTest {

    private final int SESSION_ID_LENGTH_IN_BYTES = 256 / 8;
    private ChallengeResponse challengeResponse;
    private Participant localParticipant;
    private Participant remoteParticipant;
    private HttpConnector connector;

    @Before
    public void setUp() {
        localParticipant = new Participant(EccKeyPairGenerator.generate());
        remoteParticipant = new Participant(EccKeyPairGenerator.generate());
        connector = createMock(HttpConnector.class);

        challengeResponse = new ChallengeResponse(localParticipant, remoteParticipant, connector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        challengeResponse = new ChallengeResponse(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullLocalParticipant() {
        challengeResponse = new ChallengeResponse(null, remoteParticipant, connector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullRemoteParticipant() {
        challengeResponse = new ChallengeResponse(localParticipant, null, connector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullRemoteConnector() {
        challengeResponse = new ChallengeResponse(localParticipant, remoteParticipant, null);
    }

    @Test
    public void testConstructorOnAssignments() {
        assertSame(localParticipant, challengeResponse.localParticipant);
        assertSame(remoteParticipant, challengeResponse.remoteParticipant);
        assertSame(connector, challengeResponse.connector);
    }

    @Test
    public void testConstructorOnCreatingSignerAndPacker() {
        assertNotNull(challengeResponse.signer);
        assertNotNull(challengeResponse.packer);
    }

    @Test
    public void testAuthenticate() {
        configureConnectorToHandleConnections(false, false);
        replay(connector);

        challengeResponse.authenticate();

        testOnGeneratingLocalChallengeBytes();
    }

    @Test(expected = ChallengeResponseException.class)
    public void testAuthenticateOnManipultedRemoteNonce() {
        configureConnectorToHandleConnections(true, false);
        replay(connector);

        challengeResponse.authenticate();
    }

    @Test(expected = ChallengeResponseException.class)
    public void testAuthenticateOnInitChallengeResponse() {
        configureConnectorToHandleConnections(false, true);
        replay(connector);

        challengeResponse.authenticate();
    }

    private void configureConnectorToHandleConnections(
            final boolean manipulateRemoteNonce,
            final boolean manipulateResponseDone) {
        expect(connector.excutePost(anyObject(byte[].class))).andDelegateTo(new HttpConnector("http://localhost") {
            byte[] localNonce;
            byte[] remoteNonce;
            byte[] remotePublicKey;
            byte[] remoteSignature;
            Participant internalRemoteParticipant;
            boolean doManipulateResponseBytes;

            @Override
            public byte[] excutePost(byte[] data) {
                Message messsage = extractMessage(data);
                verifyMessageFields(messsage);
                return sendResponse(messsage);
            }

            private Message extractMessage(byte[] data) {
                CryptoPacker packer = new CryptoPacker();
                return packer.decryptAndUnpack(data, remoteParticipant);
            }

            private void verifyMessageFields(Message message) {
                assertEquals(Message.DEFAUTL_VERSION, message.getVersion());
                assertArrayEquals(remoteParticipant.getPublicKeyAsBytes(), message.getParticipant().getPublicKeyAsBytes());

                byte[] phaseBytes = message.getContent().get(MessageField.CNT_CRPHASE.toString());
                ChallengeResponse.Phase phase = ChallengeResponse.Phase.valueOf(phaseBytes);

                switch (phase) {
                    case INIT_CHALLENGE:
                        Map<String, byte[]> initChallengeContent = message.getContent();
                        remotePublicKey = initChallengeContent.get(MessageField.CNT_CRPUBKEY.toString());
                        remoteNonce = initChallengeContent.get(MessageField.CNT_CRNONCE.toString());
                        internalRemoteParticipant = new Participant(EccKeyPairGenerator.restoreFromPublicKeyBytes(remotePublicKey));

                        assertArrayEquals(localParticipant.getPublicKeyAsBytes(), remotePublicKey);
                        assertNotNull(remoteNonce);
                        assertEquals(ChallengeResponse.NUMBER_OF_CHALLENGE_BYTES, remoteNonce.length);
                        break;
                    case RESPONSE_DONE:
                        Map<String, byte[]> responseDoneContent = message.getContent();
                        remoteSignature = responseDoneContent.get(MessageField.CNT_CRSIG.toString());

                        assertArrayEquals(challengeResponse.localSignature, remoteSignature);
                        break;
                    default:
                        fail("This should not happen.");
                        break;
                }
            }

            private byte[] sendResponse(Message message) {
                String phaseName = new String(message.getContent().get(MessageField.CNT_CRPHASE.toString()));
                ChallengeResponse.Phase phase = ChallengeResponse.Phase.valueOf(phaseName);

                switch (phase) {
                    case INIT_CHALLENGE:
                        localNonce = generateNonce();
                        byte[] signature = sign(
                                remoteParticipant.getPublicKeyAsBytes(),
                                localNonce,
                                remoteNonce);
                        if (manipulateRemoteNonce) {
                            localNonce[0] = 0;
                            localNonce[10] = 0;
                            localNonce[17] = 0;
                        }
                        if (manipulateResponseDone) {
                            signature[10] = 123;
                            signature[50] = 0;
                            signature[60] = 17;
                        }
                        return encryptAndPack(localNonce, signature, message);
                    case RESPONSE_DONE:
                        verifyRemoteSignature();
                        return null;
                    default:
                        fail("This should not happen.");
                }

                throw new IllegalStateException("This should not happen.");
            }

            byte[] generateNonce() {
                SecureRandom random = new SecureRandom();
                byte[] nonce = new byte[ChallengeResponse.NUMBER_OF_CHALLENGE_BYTES];
                random.nextBytes(nonce);

                return nonce;
            }

            byte[] sign(byte[] publicKey, byte[] localNonce, byte[] remoteNonce) {
                byte[] merged = Arrays.mergeArrays(publicKey, localNonce, remoteNonce);
                byte[] digest = Digest.digestWithSha256(merged);

                EccSigner signer = new EccSigner();
                return signer.sign(digest, remoteParticipant.getPrivateKey());
            }

            byte[] encryptAndPack(byte[] localNonce, byte[] signature, Message message) {
                byte[] targetPublicKey = message.getContent().get(MessageField.CNT_CRPUBKEY.toString());
                KeyPair targetKeyPair = EccKeyPairGenerator.restoreFromPublicKeyBytes(targetPublicKey);
                Participant target = new Participant(targetKeyPair);

                Message response = new Message();
                response.setVersion(Message.DEFAUTL_VERSION);
                response.setParticipant(target);
                response.appendContent(MessageField.CNT_CRPHASE, ChallengeResponse.Phase.RESPONSE_CHALLENGE.getBytes());
                response.appendContent(MessageField.CNT_CRNONCE, localNonce);
                response.appendContent(MessageField.CNT_CRSIG, signature);

                CryptoPacker packer = new CryptoPacker();
                return packer.packAndEncrypt(response, localParticipant);
            }

            void verifyRemoteSignature() {
                byte[] merged = Arrays.mergeArrays(remotePublicKey, remoteNonce, localNonce);
                byte[] digest = Digest.digestWithSha256(merged);
                EccSigner signer = new EccSigner();

                assertTrue(signer.verify(digest, remoteSignature, internalRemoteParticipant.getPublicKey()));
            }
        }).times(2);
    }

    private void testOnGeneratingLocalChallengeBytes() {
        assertNotNull(challengeResponse.localNonce);
        assertEquals(ChallengeResponse.NUMBER_OF_CHALLENGE_BYTES, challengeResponse.localNonce.length);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionKeyOnUncompletedAuthentication() {
        challengeResponse.getSessionKey();
    }

    public void testGetSessionKey() {
        testAuthenticate();
        byte[] sessionId = challengeResponse.getSessionKey();
        assertEquals(SESSION_ID_LENGTH_IN_BYTES, sessionId.length);
    }
}
