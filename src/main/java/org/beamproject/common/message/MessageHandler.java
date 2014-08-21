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
import org.beamproject.common.MessageField;
import org.beamproject.common.carrier.MessageException;

/**
 * Defines a common interface for all different {@link Message} types, defined
 * in {@link MessageField.ContentField#TYPE}.
 * <p>
 * Concrete handler implement this interface to handle one specific message
 * type.
 */
abstract public class MessageHandler {

    private final MessageValidator[] validators;
    protected Message message;

    public MessageHandler(MessageValidator... validators) {
        this.validators = validators;
    }

    /**
     * Handles the given {@link Message}. If {@link MessageValidator} have been
     * configured, they will be applied before the message is being processed by
     * a concrete handler.
     *
     * @param message The message to process.
     * @return Returns a response message that has to be sent to its recipient,
     * or {@code null} if there is no response.
     * @throws MessageException If anything goes wrong during handling the
     * message.
     */
    public Message handle(Message message) throws MessageException {
        this.message = message;

        validateMessage(message);

        return handleValidMessage();
    }

    private void validateMessage(Message message) {
        for (MessageValidator validator : validators) {
            if (!validator.isValid(message)) {
                throw new MessageException("The message did not pass the validator "
                        + validator.getClass().getName() + ".");
            }
        }
    }

    /**
     * Handles the {@code message}, as given by the superclass
     * {@link MessageHandler}. It can be expected that the message is valid.
     *
     * @return Returns a response message that has to be sent to its recipient,
     * or {@code null} if there is no response.
     * @throws MessageException If anything goes wrong during handling the
     * message.
     */
    protected abstract Message handleValidMessage() throws MessageException;
}
