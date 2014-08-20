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
import static org.beamproject.common.MessageField.ContentField.TypeValue.BLANK;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.Handshake.Phase.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakePhaseMessageValidatorTest {

    private HandshakePhaseMessageValidator validator;
    private Message message;

    @Before
    public void setUp() {
        message = new Message(BLANK, Participant.generate());
        message.getContent().clear();
    }

    @Test
    public void testIsValidOnEmptyMessage() {
        testValidator(false);
    }

    @Test
    public void testIsValidOnInvalidPhase() {
        message.putContent(HSPHASE, new byte[0]);
        testValidator(false);

        message.putContent(HSPHASE, "something but not a phase".getBytes());
        testValidator(false);

        byte[] challengeBytes = CHALLENGE.getBytes();
        challengeBytes[2]++;
        message.putContent(HSPHASE, challengeBytes);
        testValidator(false);
    }

    @Test
    public void testIsValid() {
        message.putContent(HSPHASE, CHALLENGE);
        testValidator(true);
    }

    private void testValidator(boolean exptected) {
        validator = new HandshakePhaseMessageValidator();
        assertEquals(exptected, validator.isValid(message));
    }

}
