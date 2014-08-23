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

import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.crypto.Handshake;
import static org.beamproject.common.crypto.Handshake.NONCE_LENGTH_IN_BYTES;
import static org.beamproject.common.message.Field.Cnt.HS_NONCE;

/**
 * Verifies that a {@link Message} contains a valid nonce used for a
 * {@link Handshake}.
 *
 * @see Handshake
 * @see EccKeyPairGenerator
 */
public class HandshakeNonceValidator implements MessageValidator {

    /**
     * Verifies if the given message contains a valid nonce, stored under the
     * key {@link MessageField.ContentField#HSNONCE}.
     *
     * @param message The message to verify.
     * @return true, if a valid nonce is stored, false otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        return message.containsContent(HS_NONCE)
                && message.getContent(HS_NONCE) != null
                && message.getContent(HS_NONCE).length == NONCE_LENGTH_IN_BYTES;
    }

}
