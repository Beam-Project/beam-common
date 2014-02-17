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

import org.inchat.common.util.Exceptions;

/**
 * This class represents a classic contact, as known form many other chat
 * messenger. The main functionality is to store end user information such as
 * username, server, etc..
 */
public class Contact {

    Participant server;
    Participant client;

    /**
     * Constructs a new {@link Contact} with server and client.
     *
     * @param server The server of this {@link Contact}. This is the first part
     * in the inchat url. This may not be null.
     * @param client The client of this {@link Contact}. This is the second part
     * in the inchat url. This may not be null.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public Contact(Participant server, Participant client) {
        Exceptions.verifyArgumentsNotNull(server, client);

        this.server = server;
        this.client = client;
    }

    public Participant getServer() {
        return server;
    }

    public Participant getClient() {
        return client;
    }

}
