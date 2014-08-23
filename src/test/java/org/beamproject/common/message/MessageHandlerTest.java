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

import org.beamproject.common.carrier.MessageException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

public class MessageHandlerTest {

    private MessageHandler handler;
    private Validator validator1, validator2, validator3;
    private Message message, response;

    @Before
    public void setUp() {
        validator1 = new Validator();
        validator2 = new Validator();
        validator3 = new Validator();
        message = new Message();
    }

    @Test
    public void testValidatorInvocation() {
        setUpHandlerWithValidators();

        response = handler.handle(message);

        assertSame(message, response);
        assertSame(message, validator1.givenMessage);
        assertSame(message, validator2.givenMessage);
        assertSame(message, validator3.givenMessage);
        assertEquals(1, validator1.numberOfInvocation);
        assertEquals(1, validator2.numberOfInvocation);
        assertEquals(1, validator3.numberOfInvocation);
    }

    @Test(expected = MessageException.class)
    public void testValidatorOnException() {
        validator1.returnValue = false;
        setUpHandlerWithValidators();

        response = handler.handle(message);
    }

    private void setUpHandlerWithValidators() {
        handler = new MessageHandler(validator1, validator2, validator3) {
            @Override
            protected Message handleValidMessage() throws MessageException {
                return message;
            }
        };
    }

    private class Validator implements MessageValidator {

        boolean returnValue = true;
        Message givenMessage;
        int numberOfInvocation = 0;

        @Override
        public boolean isValid(Message message) {
            givenMessage = message;
            numberOfInvocation++;
            return returnValue;
        }
    }

}
