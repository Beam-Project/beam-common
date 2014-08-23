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
package org.beamproject.common;

import java.security.KeyPair;
import java.util.LinkedList;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromBothKeys;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.util.Base58;
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
    public void testGetPublicKeyAsBase58() {
        participant.keyPair = keyPair;
        String expected = Base58.encode(keyPair.getPublic().getEncoded());
        assertEquals(expected, participant.getPublicKeyAsBase58());
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

    @Test
    public void testGetPrivateKeyAsBase58() {
        participant.keyPair = keyPair;
        String expected = Base58.encode(keyPair.getPrivate().getEncoded());
        assertEquals(expected, participant.getPrivateKeyAsBase58());

        // ensure public and private key are not the same
        assertNotEquals(participant.getPrivateKeyAsBase58(),
                participant.getPublicKeyAsBase58());
    }

    @Test
    public void testGetKeyPair() {
        participant.keyPair = keyPair;
        assertSame(keyPair, participant.getKeyPair());
    }

    @Test
    public void testGenerate() {
        LinkedList<Participant> uniques = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            participant = Participant.generate();

            if (participant == null || participant.getPrivateKey() == null || participant.getPublicKey() == null) {
                fail("This participant is not completely initialized.");
            }

            if (uniques.contains(participant)) {
                fail("The generated participants must be unique.");
            }

            uniques.add(participant);
        }
    }

    @Test
    public void testEquals() {
        Participant other = null;
        assertFalse(participant.equals(null));
        assertFalse(participant.equals(other));

        other = Participant.generate();
        assertFalse(participant.equals(other));

        other.keyPair = fromPublicKey(participant.getPublicKeyAsBytes());
        assertFalse(participant.equals(other));

        other.keyPair = fromBothKeys(participant.getPublicKeyAsBytes(), participant.getPrivateKeyAsBytes());
        assertTrue(participant.equals(other));

        other.keyPair = participant.keyPair;
        assertTrue(participant.equals(other));

        other = new Participant(keyPair);
        assertTrue(participant.equals(other));

        participant.keyPair = fromPublicKey(keyPair.getPublic().getEncoded());
        other.keyPair = keyPair;
        assertFalse(participant.equals(other));

        other.keyPair = fromPublicKey(keyPair.getPublic().getEncoded());
        assertTrue(participant.equals(other));

        assertTrue(participant.equals(participant));
    }

    /**
     * These cases should actually never occur. However, they are also tested.
     */
    @Test
    public void testEqualsOnNullKeyPairs() {
        Participant other = Participant.generate();

        participant.keyPair = null;
        assertFalse(participant.equals(null));
        assertFalse(participant.equals(other));

        other.keyPair = null;
        assertTrue(participant.equals(other));
    }

    @Test
    public void testHashCodeOnDifferentKeyPairs() {
        int hashCode = participant.hashCode();
        Participant other = Participant.generate();

        assertFalse(hashCode == other.hashCode());
    }

    @Test
    public void testHashCodeOnSameKeyPairs() {
        int hashCode = participant.hashCode();
        Participant other = Participant.generate();
        other.keyPair = participant.keyPair;

        assertTrue(hashCode == other.hashCode());
    }

    /**
     * The first participant holds public and private keys, the second only the
     * public key.
     */
    @Test
    public void testHashCodeOnPartiallyEqualKeyPairs() {
        int hashCode = participant.hashCode();
        Participant other = new Participant(fromPublicKey(
                participant.getPublicKeyAsBytes()));

        assertFalse(hashCode == other.hashCode());
    }

    /**
     * Both, public and private key are equal.
     */
    @Test
    public void testHashCodeOnEqualKeyPairs() {
        int hashCode = participant.hashCode();
        Participant other = new Participant(fromBothKeys(
                participant.getPublicKeyAsBytes(),
                participant.getPrivateKeyAsBytes()));

        assertTrue(hashCode == other.hashCode());
    }

}
