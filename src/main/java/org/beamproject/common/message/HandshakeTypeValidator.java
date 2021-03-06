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

import org.beamproject.common.crypto.Handshake;
import static org.beamproject.common.message.Field.Cnt.TYP;
import org.beamproject.common.message.Field.Cnt.Typ;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_CHALLENGE;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_INVALIDATE;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_RESPONSE;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_SUCCESS;

/**
 * Checks if the given message is a {@link Handshake} message and therefore is
 * of the type {@link HS_CHALLENGE}, {@link HS_RESPONSE}, {@link HS_SUCCESS}, or
 * {@link HS_INVALIDATE}.
 *
 * @see Handshake
 * @see Handshake.Phase
 */
public class HandshakeTypeValidator implements MessageValidator {

    /**
     * Checks if the given message is a {@link Handshake} message and therefore
     * is of the type
     * {@link HS_CHALLENGE}, {@link HS_RESPONSE}, {@link HS_SUCCESS}, or
     * {@link HS_INVALIDATE}.
     *
     * @param message The message to validate.
     * @return true, an expected is set and valid, false otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        if (!message.containsContent(TYP)) {
            return false;
        }

        try {
            Typ phase = Typ.valueOf(new String(message.getContent(TYP)));
            return phase == HS_CHALLENGE
                    || phase == HS_RESPONSE
                    || phase == HS_SUCCESS
                    || phase == HS_INVALIDATE;
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }

}
