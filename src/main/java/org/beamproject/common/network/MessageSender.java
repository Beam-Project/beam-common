/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.network;

import java.net.URL;
import org.beamproject.common.Message;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.util.Base64;
import org.beamproject.common.util.Exceptions;

/**
 * Allows to send and sendAndReceive a {@link Message}.
 */
public class MessageSender {

    CryptoPacker packer = new CryptoPacker();
    HttpConnector connector;
    Participant localParticipant;

    /**
     * Configures this {@link MessageSender} instance.
     *
     * @param recipientUrl The URL to the recipient.
     * @param localParticipant The local participant, initialized with both keys
     * for decrypting the response.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public MessageSender(URL recipientUrl, Participant localParticipant) {
        Exceptions.verifyArgumentsNotNull(recipientUrl, localParticipant);

        this.connector = new HttpConnector(recipientUrl);
        this.localParticipant = localParticipant;
    }

    /**
     * Sends the message and returns immediately.
     *
     * @param message The message to send.
     */
    public void send(Message message) {
        byte[] ciphertextRequest = packer.packAndEncrypt(message);
        connector.executePost(ciphertextRequest);
    }

    /**
     * Sends the message and waits for the response.
     *
     * @param message The message to send.
     * @return The response.
     */
    public Message sendAndReceive(Message message) {
        byte[] ciphertextRequest = packer.packAndEncrypt(message);
        byte[] base64ByteResponse = connector.executePost(ciphertextRequest);
        byte[] ciphertextResponse = Base64.decode(new String(base64ByteResponse));
        return packer.decryptAndUnpack(ciphertextResponse, localParticipant);
    }

}
