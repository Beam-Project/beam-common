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
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class provides timestamp methods via static access.
 */
public class Timestamps {

    /**
     * Gets current the ISO 8601 UTC timestamp. Such a timestamp looks like
     * {@code 2014-05-03T15:03:37.143Z}, where {@code Z} stands for <i>zero</i>
     * and indicates that the UTC offset is 0.
     *
     * @return The current timestamp.
     */
    public static String getIso8601UtcTimestamp() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        return formatter.print(new DateTime(ISOChronology.getInstanceUTC()));
    }
}
