/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

/**
 * Provides methods to work with {@link Character}s.
 */
public class Chars {

    /**
     * Converts the given characters to bytes, assuming that the encoding of the
     * characters is in UTF.
     * <p>
     * This method is inspired by <a
     * href="http://www.javacodegeeks.com/2010/11/java-best-practices-char-to-byte-and.html">Java
     * Best Practices – Char to Byte and Byte to Char conversions</a> but was
     * refactored.
     *
     * @param chars The characters to represent as bytes.
     * @return The byte representation of the given characters.
     */
    public static byte[] utfCharsToBytes(char[] chars) {
        byte[] bytes = new byte[chars.length << 1];

        for (int i = 0; i < chars.length; i++) {
            int bytePosition = i << 1;
            bytes[bytePosition] = (byte) ((chars[i] & 0xFF00) >> 8);
            bytes[bytePosition + 1] = (byte) (chars[i] & 0x00FF);
        }
        return bytes;
    }

    /**
     * Converts the given bytes to characters, using the UTF encoding of the
     * characters.
     * <p>
     * This method is inspired by <a
     * href="http://www.javacodegeeks.com/2010/11/java-best-practices-char-to-byte-and.html">Java
     * Best Practices – Char to Byte and Byte to Char conversions</a> but was
     * refactored.
     *
     * @param bytes The bytes to convert to characters.
     * @return The character representation of the given bytes.
     */
    public static char[] bytesToUtfChars(byte[] bytes) {
        char[] chars = new char[bytes.length >> 1];

        for (int i = 0; i < chars.length; i++) {
            int bytePosition = i << 1;
            char character = (char) (((bytes[bytePosition] & 0x00FF) << 8) + (bytes[bytePosition + 1] & 0x00FF));
            chars[i] = character;
        }

        return chars;
    }

}
