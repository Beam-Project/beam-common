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
package org.inchat.common.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParticipantIdGeneratorTest {

    private final int ID_LENGTH_IN_BYTES = 256 / 8;
    private final int MINIMAL_NUMBER_OF_NONZERO_BYTES = 20;
    private byte[] id;

    @Test
    public void testInstantiation() {
        ParticipantIdGenerator generator = new ParticipantIdGenerator();
    }

    @Test
    public void testGenerateId() {
        id = ParticipantIdGenerator.generateId();
        assertEquals(ID_LENGTH_IN_BYTES, id.length);

        int nonZeroBytes = 0;

        for (byte aByte : id) {
            if (aByte != (byte) 0) {
                nonZeroBytes++;
            }
        }

        if (nonZeroBytes < MINIMAL_NUMBER_OF_NONZERO_BYTES) {
            fail("Only " + nonZeroBytes + " bytes were not zero, but at least "
                    + MINIMAL_NUMBER_OF_NONZERO_BYTES + " were expected.");
        }
    }

}