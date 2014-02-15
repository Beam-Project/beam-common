/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-common.
 *
 * inchat-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.common;

import java.security.KeyPair;
import javax.xml.bind.DatatypeConverter;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ParticipantTest {

    private Participant participant;
    private KeyPair keyPair;

    @Before
    public void setUp() {
        keyPair = EccKeyPairGenerator.generate();
        participant = new Participant(keyPair);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConstructorOnNull() {
        participant = new Participant(null);
    }

    @Test
    public void testNullConstructorOnAssignment() {
        participant = new Participant(keyPair);
        assertSame(keyPair, participant.keyPair);
    }

    @Test
    public void testGetPublicKey() {
        participant.keyPair = keyPair;
        assertSame(keyPair.getPublic(), participant.getPublicKey());
    }

    @Test
    public void testGetPublicKeyAsBytes() {
        participant.keyPair = keyPair;
        assertArrayEquals(keyPair.getPublic().getEncoded(), participant.getPublicKeyAsBytes());
    }

    @Test
    public void testGetPublicKeyAsHex() {
        participant.keyPair = keyPair;
        String expected = DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded());
        assertEquals(expected, participant.getPublicKeyAsHex());
    }

    @Test
    public void testGetPrivateKey() {
        participant.keyPair = keyPair;
        assertSame(keyPair.getPrivate(), participant.getPrivateKey());
    }

    @Test
    public void testGetPrivateKeyAsBytes() {
        participant.keyPair = keyPair;
        assertArrayEquals(keyPair.getPrivate().getEncoded(), participant.getPrivateKeyAsBytes());
    }

}
