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
package org.beamproject.common.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TimestampsTest {

    @Test
    public void testGetUtcTimestamp() {
        DateTime timestamp = Timestamps.getUtcTimestamp();

        double allowedDeltaInMilliseconds = 100d;
        assertEquals(System.currentTimeMillis(), timestamp.getMillis(), allowedDeltaInMilliseconds);
        assertEquals(DateTimeZone.UTC, timestamp.getZone());
    }

    @Test
    public void testGetIso8601UtcTimestamp() {
        String timestamp = Timestamps.getIso8601UtcTimestamp();
        assertTrue(timestamp.matches(Timestamps.TIMESTAMP_REGEX));

        DateTime reversedTimestamp = Timestamps.parseIso8601UtcTimestamp(timestamp);
        assertEquals(reversedTimestamp.hourOfDay().get(), Integer.parseInt(timestamp.substring(11, 13))); // Verify that time zone is UTC
    }

    @Test
    public void testIsValidIso8601UtcTimestamp() {
        assertFalse(Timestamps.isValidIso8601UtcTimestamp(null));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp(""));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("as;d fkjasdlfkj"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03 T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014.05.03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03 T 15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp(" 2014-05-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15:03:37.143Z "));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15:03:37.143"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("-2014-05-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-13-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03t15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15:03:37.143z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15:03:37.143"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("20140-05-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("0140-05-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("140-05-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15.03.37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-00-03T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-00T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-05-0T15:03:37.143Z"));
        assertFalse(Timestamps.isValidIso8601UtcTimestamp("2014-0-03T15:03:37.143Z"));

        assertTrue(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T15:03:37.143Z"));
        assertTrue(Timestamps.isValidIso8601UtcTimestamp("1999-05-03T15:03:37.143Z"));
        assertTrue(Timestamps.isValidIso8601UtcTimestamp("2999-12-03T15:03:37.143Z"));
        assertTrue(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T23:03:37.143Z"));
        assertTrue(Timestamps.isValidIso8601UtcTimestamp("2014-05-03T00:00:00.000Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIso8601UtcTimestampOnNull() {
        Timestamps.parseIso8601UtcTimestamp(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIso8601UtcTimestampOnEmptyString() {
        Timestamps.parseIso8601UtcTimestamp("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIso8601UtcTimestampOnInvalidString() {
        Timestamps.parseIso8601UtcTimestamp("2014-05-03 T 15:03:37.143 Z");
    }

    @Test
    public void testParseIso8601UtcTimestamp() {
        DateTime timestamp = Timestamps.parseIso8601UtcTimestamp("2014-05-03T15:03:37.143Z");
        assertEquals(2014, timestamp.getYear());
        assertEquals(5, timestamp.getMonthOfYear());
        assertEquals(3, timestamp.getDayOfMonth());
        assertEquals(15, timestamp.getHourOfDay());
        assertEquals(3, timestamp.getMinuteOfHour());
        assertEquals(37, timestamp.getSecondOfMinute());
        assertEquals(143, timestamp.getMillisOfSecond());
    }

}
