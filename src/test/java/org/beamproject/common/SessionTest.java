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

import org.beamproject.common.util.Timestamps;
import org.joda.time.DateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

public class SessionTest {

    private Participant user;
    private Session session;
    private final byte[] key = "my secret session key".getBytes();

    @Before
    public void setUp() {
        user = Participant.generate();
        session = new Session(user, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        session = new Session(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUser() {
        session = new Session(null, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullKey() {
        session = new Session(user, null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertSame(user, session.remoteParticipant);
        assertSame(key, session.key);
    }

    @Test
    public void testConstructorOnCreatingTimestamp() {
        assertNotNull(session.latestInteractionTime);
    }

    @Test
    public void testGetUser() {
        assertSame(user, session.getRemoteParticipant());
        session.remoteParticipant = null;
        assertNull(session.getRemoteParticipant());
    }

    @Test
    public void testGetKey() {
        assertSame(key, session.getKey());
        session.key = null;
        assertNull(session.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLastInteractionTimeOnNull() {
        session.setLastestInteractionTime(null);
    }

    @Test
    public void testSetLastInteractionTime() {
        DateTime now = Timestamps.getUtcTimestamp();
        session.latestInteractionTime = null;
        session.setLastestInteractionTime(now);
        assertSame(now, session.latestInteractionTime);
    }

    @Test
    public void testGetLastInteractionTime() {
        assertSame(session.latestInteractionTime, session.getLatestInteractionTime());
        session.latestInteractionTime = null;
        assertNull(session.getLatestInteractionTime());
    }

    @Test
    public void testInvalidate() {
        int length = key.length;
        assertSame(key, session.key);

        session.invalidateSession();
        assertSame(key, session.key);
        assertEquals(length, session.key.length);

        for (byte b : session.key) {
            assertEquals(b, 0);
        }

        assertNull(session.remoteParticipant);
    }

}
