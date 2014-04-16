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
public abstract class Exceptions {

    private final static String ARGUMENTS_NOT_NULL_MESSAGE = "The argument may not be null.";
    private final static String ARGUMENTS_NOT_EMPTY_MESSAGE = "The argument may not be empty.";

    /**
     * Verifies that the arguments are not null. If at least one argument is
     * null, an {@link IllegalArgumentException} is thrown.
     *
     * @param arguments May not be null.
     * @throws IllegalArgumentException If the argument or at least one of it's
     * values is null.
     */
    public static void verifyArgumentsNotNull(Object... arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException(ARGUMENTS_NOT_NULL_MESSAGE);
        }

        for (Object argument : arguments) {
            if (argument == null) {
                throw new IllegalArgumentException(ARGUMENTS_NOT_NULL_MESSAGE);
            }
        }
    }

    /**
     * Verifies that the arguments are not empty. If at least one argument is
     * null or empty, an {@link IllegalArgumentException} is thrown.
     *
     * @param arguments May not be null, nor empty.
     * @throws IllegalArgumentException If the argument or at least one of it's
     * values is null or empty.
     */
    public static void verifyArgumentsNotEmpty(String... arguments) {
        verifyArgumentsNotNull((Object[]) arguments);

        for (String argument : arguments) {
            if (argument.isEmpty()) {
                throw new IllegalArgumentException(ARGUMENTS_NOT_EMPTY_MESSAGE);
            }
        }
    }

}
