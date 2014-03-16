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
 * This class contains general purpose exception methods.
 */
public class Exceptions {

    private Exceptions() {
        // Only static access.
    }

    /**
     * Tests if the {@code argument} is null. If so, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param argument May not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static void verifyArgumentNotNull(Object argument) {
        if (argument == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }
    }

    /**
     * Tests if {@code arguments} or at lest one of it's values is null. If so,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param arguments May not be null.
     * @throws IllegalArgumentException If the argument or at least one of it's
     * values is null.
     */
    public static void verifyArgumentsNotNull(Object... arguments) {
        verifyArgumentNotNull(arguments);

        for (Object argument : arguments) {
            verifyArgumentNotNull(argument);
        }
    }

    /**
     * Tests if the {@code argument} is empty (or null). If so, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param argument May not be empty nor null.
     * @throws IllegalArgumentException If the argument is empty or null.
     */
    public static void verifyArgumentNotEmpty(String argument) {
        verifyArgumentNotNull(argument);

        if (argument.isEmpty()) {
            throw new IllegalArgumentException("The argument may not be empty.");
        }
    }

}
