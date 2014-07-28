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

/**
 * Provides several statically accessible methods to work with arrays.
 */
public class Arrays {

    /**
     * Merges several byte[] arguments into one large array. The order is
     * provided as the arguments are given.
     *
     * @param arguments Zero or several arguments that should be merged into one
     * array.
     * @return The merged array.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public static byte[] mergeArrays(byte[] ...  arguments) {
        Exceptions.verifyArgumentsNotNull(arguments);

        int totalLength = 0;

        for (byte[] argument : arguments) {
            totalLength += argument.length;
        }

        byte[] merged = new byte[totalLength];
        int destinationIndex = 0;

        for (byte[] argument : arguments) {
            System.arraycopy(argument, 0, merged, destinationIndex, argument.length);
            destinationIndex += argument.length;
        }

        return merged;
    }

}
