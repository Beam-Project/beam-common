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
package org.inchat.common.util;

import javax.xml.bind.DatatypeConverter;

/**
 * Encodes byte arrays to strings and decodes strings to byte arrays using
 * Base64.
 */
public abstract class Base64 {

    /**
     * Encodes the given argument to a Base64 string.
     *
     * @param decoded This may not be null.
     * @return The encoded string.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static String encode(byte[] decoded) {
        Exceptions.verifyArgumentNotNull(decoded);
        return DatatypeConverter.printBase64Binary(decoded);
    }

    /**
     * Decodes the given Base64 encoded argument to a byte array.
     *
     * @param encoded This may not be null.
     * @return The decoded byte array.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static byte[] decode(String encoded) {
        Exceptions.verifyArgumentNotNull(encoded);
        return DatatypeConverter.parseBase64Binary(encoded);
    }
}
