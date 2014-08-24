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
import org.beamproject.common.crypto.EccSigner;
import static org.beamproject.common.message.Field.Cnt.SIGNATURE;
import static org.beamproject.common.message.HandshakeSignatureValidator.MAXIMAL_SIGNATURE_LENGTH_IN_BYTES;
import static org.beamproject.common.message.HandshakeSignatureValidator.MINIMAL_SIGNATURE_LENGTH_IN_BYTES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class HandshakeSignatureValidatorTest {

    private HandshakeSignatureValidator validator;
    private Message message;

    @Before
    public void setUp() {
        validator = new HandshakeSignatureValidator();
        message = new Message();
    }

    @Test
    public void testIsValidOnMissingSignature() {
        assertFalse(validator.isValid(message));
    }

    @Test
    public void testIsValidOnNullSignature() {
        message.getContent().put(SIGNATURE.toString(), null);
        assertFalse(validator.isValid(message));
    }

    @Test
    public void testIsVaildOnSignautre() {
        EccSigner signer = new EccSigner();
        byte[] sig = signer.sign("sign me".getBytes(), EccKeyPairGenerator.generate().getPrivate());

        message.putContent(SIGNATURE, sig);
        assertTrue(validator.isValid(message));
    }

    @Test
    public void testIsValidOnLength() {
        for (int i = 0; i < MAXIMAL_SIGNATURE_LENGTH_IN_BYTES * 2; i++) {
            message.getContent().put(SIGNATURE.toString(), getArrayOfLengt(i));

            if (i >= MINIMAL_SIGNATURE_LENGTH_IN_BYTES
                    && i <= MAXIMAL_SIGNATURE_LENGTH_IN_BYTES) {
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
