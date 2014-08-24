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
package org.beamproject.common.message;

import java.security.KeyPair;
import java.security.PublicKey;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.crypto.Handshake;
import static org.beamproject.common.message.Field.Cnt.PUBLIC_KEY;

/**
 * Verifies that a {@link Message} contains a valid {@link PublicKey} used for a
 * {@link Handshake}.
 *
 * @see Handshake
 * @see EccKeyPairGenerator
 */
public class HandshakePublicKeyValidator implements MessageValidator {

    /**
     * Verifies if the given message contains a valid {@link PublicKey}, stored
     * under the key {@link MessageField.ContentField#HSPUBKEY}.
     *
     * @param message The message to verify.
     * @return true, if a valid public key is stored, false otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        if (!message.containsContent(PUBLIC_KEY)
                || message.getContent(PUBLIC_KEY) == null
                || message.getContent(PUBLIC_KEY).length == 0) {
            return false;
        }

        try {
            byte[] remotePublicKey = message.getContent(PUBLIC_KEY);
            KeyPair remoteKeyPair = fromPublicKey(remotePublicKey);
            return remoteKeyPair != null;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return false;
        }
    }

}
