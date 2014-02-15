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
package org.inchat.common.transfer;

import org.inchat.common.Participant;
import org.inchat.common.util.Exceptions;

/**
 * Creates an URL of a {@link Participant} and its Server.
 */
public class UrlAssembler {

    public final static String SCHEME_PART = "inchat:";
    public final static String SEPERATOR = ".";

    private UrlAssembler() {
        // Only static access.
    }

    /**
     * Assembles an inchat URL of a client with its server.
     *
     * @param server The server, may not be null.
     * @param client The client, may not be null.
     * @return The link.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public static String toUrlByServerAndClient(Participant server, Participant client) {
        Exceptions.verifyArgumentsNotNull(server, client);

        return SCHEME_PART + server.getPublicKeyAsHex() + SEPERATOR + client.getPublicKeyAsHex();
    }

    /**
     * Assembles an inchat URL of a server.
     *
     * @param server The server, may not be null.
     * @return The link.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static String toUrlByServer(Participant server) {
        Exceptions.verifyArgumentNotNull(server);

        return SCHEME_PART + server.getPublicKeyAsHex();
    }

}
