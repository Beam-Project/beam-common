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

import static org.beamproject.common.message.Field.Cnt.HS_SIG;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.crypto.Handshake;

/**
 * Verifies that a {@link Message} contains a syntactically signature used for a
 * {@link Handshake}.
 * <p>
 * This does NOT check if the signature is correct, ONLY if the format is
 * correct (length, not null, etc.).
 *
 * @see Handshake
 * @see EccKeyPairGenerator
 */
public class HandshakeSignatureMessageValidator implements MessageValidator {

    public final static int MINIMAL_SIGNATURE_LENGTH_IN_BYTES = 90;
    public final static int MAXIMAL_SIGNATURE_LENGTH_IN_BYTES = 120;

    /**
     * Verifies if the given message contains a syntactically correct signature,
     * stored under the key {@link MessageField.ContentField#HSSIG}.
     * <p>
     * This does NOT check if the signature is correct, ONLY if the format is
     * correct (length, not null, etc.).
     *
     * @param message The message to verify.
     * @return true, if a syntactically correct signature is stored, false
     * otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        return message.containsContent(HS_SIG)
                && message.getContent(HS_SIG) != null
                && message.getContent(HS_SIG).length >= MINIMAL_SIGNATURE_LENGTH_IN_BYTES
                && message.getContent(HS_SIG).length <= MAXIMAL_SIGNATURE_LENGTH_IN_BYTES;
    }

}
