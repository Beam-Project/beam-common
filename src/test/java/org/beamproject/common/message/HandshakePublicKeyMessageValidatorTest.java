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

import static org.beamproject.common.message.MessageField.ContentField.HSPUBKEY;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakePublicKeyMessageValidatorTest {

    private HandshakePublicKeyMessageValidator validator;
    private Message message;

    @Before
    public void setUp() {
        message = new Message();
    }

    @Test
    public void testIsValidOnEmptyMessage() {
        testValidator(false);
    }

    @Test
    public void testIsValidOnInvalidPublicKey() {
        message.putContent(HSPUBKEY, new byte[0]);
        testValidator(false);

        message.putContent(HSPUBKEY, "something but not a public key".getBytes());
        testValidator(false);
    }

    @Test
    public void testIsValid() {
        byte[] bytes = EccKeyPairGenerator.generate().getPublic().getEncoded();
        message.putContent(HSPUBKEY, bytes);
        testValidator(true);
    }

    private void testValidator(boolean exptected) {
        validator = new HandshakePublicKeyMessageValidator();
        assertEquals(exptected, validator.isValid(message));
    }

}
