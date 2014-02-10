/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-common.
 *
 * inchat-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.common;

public enum MessageField {

    /**
     * Stands for "Version". This field contains the message format version.
     */
    VRS("VRS"),
    /**
     * Stands for "Participant" and is the id of the target {@link Participant}
     * of this message.
     */
    PRT("PRT"),
    /**
     * Stands for "Content" and contains the message as several sub fields.
     */
    CNT("CNT"),
    /**
     * Stands for "Message" and contains the message text itself.
     */
    CNT_MSG("MSG");

    private final String value;

    private MessageField(String value) {
        this.value = value;
    }

    /**
     * Returns the inchat format code. For example, {@code PRM_KEY} results in
     * {@code KEY}, which can be used in a {@code PRM} field.
     *
     * @return The protocol field identifier.
     */
    @Override
    public String toString() {
        return value;
    }
}
