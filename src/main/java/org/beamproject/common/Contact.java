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
package org.beamproject.common;

import java.io.Serializable;
import org.beamproject.common.util.Exceptions;

/**
 * This class represents a classic contact, as known form many other chat
 * messenger. The main functionality is to store end user information such as
 * username, server, etc..
 */
public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;

    Participant server;
    Participant client;
    String name;

    /**
     * Constructs a new {@link Contact} with server and client.
     *
     * @param server The server of this {@link Contact}. This is the first part
     * in the beam url. This may not be null.
     * @param client The client of this {@link Contact}. This is the second part
     * in the beam url. This may not be null.
     * @param name The name of this {@link Contact}. This may not be empty.
     * @throws IllegalArgumentException If at least one of the arguments is null
     * or the name is empty.
     */
    public Contact(Participant server, Participant client, String name) {
        Exceptions.verifyArgumentsNotNull(server, client, name);

        this.server = server;
        this.client = client;
        this.name = name;
    }
 
    public Participant getServer() {
        return server;
    }

    public Participant getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
