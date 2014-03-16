/**
 * This files source was originally copied from
 * http://code.google.com/p/bccapi/. Only minor changes were made (removed not
 * needed methods, reformatted the document, changed the Javadoc to our needs
 * and added input validation).
 *
 * All changes are release under the following license:
 * <p>
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-common.
 *
 * beam-common is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beam-common is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * </p>
 *
 * The original source code falls under the original license terms.
 */
/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.beamproject.common.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import org.beamproject.common.crypto.Digest;

/**
 * <p>
 * Base58 is a way to encode Bitcoin addresses as numbers and letters. Note that
 * this is not the same base58 as used by Flickr, which you may see reference to
 * around the internet.
 * </p>
 *
 * <p>
 * You may instead wish to work with {@link VersionedChecksummedBytes}, which
 * adds support for testing the prefix and suffix bytes commonly found in
 * addresses.
 * </p>
 *
 * <p>
 * Satoshi says: why base-58 instead of standard base-64 encoding?
 * <p>
 *
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and could be
 * used to create visually identical looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted as an
 * account number.</li>
 * <li>E-mail usually won't line-break if there's no punctuation to break
 * at.</li>
 * <li>Doubleclicking selects the whole number as one word if it's all
 * alphanumeric.</li>
 * </ul>
 */
public class Base58 {

    public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[] INDEXES = new int[128];

    static {
        for (int i = 0; i < INDEXES.length; i++) {
            INDEXES[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    /**
     * Encodes the given argument to a Base58 string. This uses the Bitcoin
     * implementation without checksum.
     *
     * @param input This may not be null.
     * @return The encoded string.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static String encode(byte[] input) {
        Exceptions.verifyArgumentNotNull(input);

        if (input.length == 0) {
            return "";
        }

        input = copyOfRange(input, 0, input.length);

        // Count leading zeroes.
        int zeroCount = 0;
        while (zeroCount < input.length && input[zeroCount] == 0) {
            ++zeroCount;
        }

        // The actual encoding.
        byte[] temp = new byte[input.length * 2];
        int j = temp.length;

        int startAt = zeroCount;

        while (startAt < input.length) {
            byte mod = divmod58(input, startAt);
            if (input[startAt] == 0) {
                ++startAt;
            }
            temp[--j] = (byte) ALPHABET[mod];
        }

        // Strip extra '1' if there are some after decoding.
        while (j < temp.length && temp[j] == ALPHABET[0]) {
            ++j;
        }

        // Add as many leading '1' as there were leading zeros.
        while (--zeroCount >= 0) {
            temp[--j] = (byte) ALPHABET[0];
        }

        byte[] output = copyOfRange(temp, j, temp.length);

        try {
            return new String(output, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("An error occured during performing Base58 operations. This should never happen: " + e.getMessage()); // Cannot happen.
        }
    }

    /**
     * Decodes the given Base58 encoded argument to a byte array.
     *
     * @param input This may not be null.
     * @return The decoded byte array.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static byte[] decode(String input) {
        Exceptions.verifyArgumentNotNull(input);

        if (input.length() == 0) {
            return new byte[0];
        }

        byte[] input58 = new byte[input.length()];

        // Transform the String to a base58 byte sequence
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            int digit58 = -1;

            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }

            if (digit58 < 0) {
                throw new IllegalArgumentException("The given argument could not be decoded correctly.");
            }

            input58[i] = (byte) digit58;
        }

        // Count leading zeroes
        int zeroCount = 0;

        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }

        // The encoding
        byte[] temp = new byte[input.length()];
        int j = temp.length;
        int startAt = zeroCount;

        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }

        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    //
    // number -> number / 58, returns number % 58
    //
    private static byte divmod58(byte[] number, int startAt) {
        int remainder = 0;

        for (int i = startAt; i < number.length; i++) {
            int digit256 = (int) number[i] & 0xFF;
            int temp = remainder * 256 + digit256;

            number[i] = (byte) (temp / 58);

            remainder = temp % 58;
        }

        return (byte) remainder;
    }

    //
    // number -> number / 256, returns number % 256
    //
    private static byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;

        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * 58 + digit58;

            number58[i] = (byte) (temp / 256);

            remainder = temp % 256;
        }

        return (byte) remainder;
    }

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);

        return range;
    }
}
