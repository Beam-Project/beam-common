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

import org.beamproject.common.util.Exceptions;

/**
 * Validates if a given {@link Message} has certain fields.
 *
 * @see Field
 */
public class ContentFieldMessageValidator implements MessageValidator {

    private final Field.Cnt[] requiredFields;

    /**
     * Creates a new validator that checks against all given content fields.
     *
     * @param requiredFields The required fields to pass this validator tests.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public ContentFieldMessageValidator(Field.Cnt... requiredFields) {
        Exceptions.verifyArgumentsNotNull((Object[]) requiredFields);

        this.requiredFields = requiredFields;
    }

    /**
     * Checks the given message
     * <ul>
     * <li>if it contains exactly the configured and only those
     * {@link MessageField.ContentField}s,</li>
     * <li>if no value of the checked entries is null, and</li>
     * <li>if all values are at least 1 byte long.</li>
     * </ul>
     *
     * @param message The message to verify.
     * @return true, if all checks have been passed, false otherwise.
     */
    @Override
    public boolean isValid(Message message) {
        for (Field.Cnt field : requiredFields) {
            if (!message.containsContent(field)
                    || message.getContent(field) == null
                    || message.getContent(field).length == 0) {
                return false;
            }
        }

        return requiredFields.length == message.getContent().size();
    }

}
