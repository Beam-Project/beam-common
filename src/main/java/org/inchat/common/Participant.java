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

import java.util.UUID;

/**
 * Represents a instance in the network that does something with messages. For
 * example, a Participant could be a user or a server.
 */
public class Participant {

    UUID id = null;

    public Participant() {
    }

    public Participant(UUID newUUID) {
        id = newUUID;
    }

    /**
     * Sets the id.
     *
     * @param id
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
