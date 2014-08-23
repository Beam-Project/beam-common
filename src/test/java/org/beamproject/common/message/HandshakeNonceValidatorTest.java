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

import static org.beamproject.common.message.Field.Cnt.HS_NONCE;
import static org.beamproject.common.crypto.Handshake.NONCE_LENGTH_IN_BYTES;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeNonceValidatorTest {

    private HandshakeNonceValidator validator;
    private Message message;

    @Before
    public void setUp() {
        validator = new HandshakeNonceValidator();
        message = new Message();
    }

    @Test
    public void testIsValidOnMissingNonce() {
        assertFalse(validator.isValid(message));
    }

    @Test
    public void testIsValidOnNullNonce() {
        message.getContent().put(HS_NONCE.toString(), null);
        assertFalse(validator.isValid(message));
    }

    @Test
    public void testIsValidOnLength() {
        for (int i = 0; i < NONCE_LENGTH_IN_BYTES * 2; i++) {
            message.getContent().put(HS_NONCE.toString(), getArrayOfLengt(i));

            if (i == NONCE_LENGTH_IN_BYTES) {
                assertTrue(validator.isValid(message));
            } else {
                assertFalse(validator.isValid(message));
            }
        }
    }

    private byte[] getArrayOfLengt(int length) {
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        return bytes;
    }

}
