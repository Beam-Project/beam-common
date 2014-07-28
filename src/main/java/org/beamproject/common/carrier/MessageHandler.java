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
package org.beamproject.common.carrier;

import org.beamproject.common.Message;
import org.beamproject.common.MessageField;

/**
 * Defines a common interface for all different {@link Message} types, defined
 * in {@link MessageField.ContentField#TYPE}.
 * <p>
 * Concrete handler implement this interface to handle one specific message
 * type.
 */
public interface MessageHandler {

    /**
     * Handles the given {@link Message}.
     * <p>
     * The message has been validated against the following aspects:
     * <ul>
     * <li>Version</li>
     * <li>Recipient (is equals the local participant)</li>
     * <li>Type is existing and a valid one (in
     * {@link MessageField.ContentField#TYPE})</li>
     * </ul>
     *
     * @param message The message to process.
     * @return Returns a response message that has to be sent to its recipient,
     * or {@code null} if there is no response.
     * @throws MessageException If anything goes wrong during handling the
     * message.
     */
    public Message handle(Message message) throws MessageException;
}
