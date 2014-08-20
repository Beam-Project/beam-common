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
package org.beamproject.common.message;

import org.beamproject.common.Message;

/**
 * A {@link MessageValidator} is used to validate messages against different
 * rules they have to fulfill.
 *
 * @see FieldMessageValidator
 */
public interface MessageValidator {

    /**
     * Validates if the given message fulfills the requirements of this
     * validator.
     *
     * @param message The message to validate.
     * @return true if everything is correct, false otherwise.
     */
    public boolean isValid(Message message);
}
