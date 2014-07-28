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

/**
 * This exception is thrown when a problem occurs during processing in incoming
 * message in a {@link CarrierModel}. This could be, for example, when a needed
 * {@link MessageField} is messing or a value not valid.
 */
public class MessageException extends RuntimeException {

    private final static long serialVersionUID = 1L;

    public MessageException(String message) {
        super(message);
    }

}
