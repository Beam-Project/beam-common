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

import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.HSPHASE;
import org.beamproject.common.crypto.Handshake;

/**
 * Validates if a {@link Message} is part of a {@link Handshake} and if the
 * {@link Handshake.Phase} is set and valid.
 *
 * @see Handshake
 * @see Handshake.Phase
 */
public class HandshakePhaseMessageValidator implements MessageValidator {

    /**
     * Checks if the given message is of a {@link Handshake} and contains a
     * valid {@link Handshake.Phase}.
     *
     * @param message The message to validate.
     * @return true, if the phase is set and valid, false otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        if (!message.containsContent(HSPHASE)) {
            return false;
        }

        try {
            Handshake.Phase phase = Handshake.Phase.valueOf(message.getContent(HSPHASE));
            return phase != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}
