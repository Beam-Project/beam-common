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

import javax.xml.bind.DatatypeConverter;
import org.inchat.common.Participant;
import org.inchat.common.util.Exceptions;

/**
 * Creates an URL of a {@link Participant} and its Server.
 */
public class UrlAssembler {

    public final static String SCHEME_PART = "inchat://";
    public final static String SEPERATOR = "/";
    private final static int LENGTH_OF_HEX_BLOCK = 64;

    private UrlAssembler() {
        // Only static access.
    }

    /**
     * Assembles an inchat URL form a server and a client.
     *
     * @param server The server, may not be null.
     * @param client The client, may not be null.
     * @return The link.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public static String toUrlByServerAndClient(Participant server, Participant client) {
        Exceptions.verifyArgumentsNotNull(server, client);

        String serverIdAsHex = DatatypeConverter.printHexBinary(server.getId()).toLowerCase();
        String clientIdAsHex = DatatypeConverter.printHexBinary(client.getId()).toLowerCase();

        return SCHEME_PART + serverIdAsHex + SEPERATOR + clientIdAsHex;
    }

    /**
     * Extracts the server part of an inchat URL and creates a new instance of a
     * {@link Participant} with the id of the URL.
     *
     * @param url The URL, may not be null nor empty and it has to be a valid
     * inchat URL.
     * @return The initialized server.
     * @throws IllegalArgumentException If the argument is null or not a valid
     * inchat URL.
     */
    public static Participant toServerByUrl(String url) {
        Exceptions.verifyArgumentNotEmpty(url);
        verifyUrlValidity(url);

        int beginIndex = SCHEME_PART.length();
        int endIndex = beginIndex + LENGTH_OF_HEX_BLOCK;
        String serverPart = url.substring(beginIndex, endIndex);
        byte[] serverId = DatatypeConverter.parseHexBinary(serverPart);

        return new Participant(serverId);
    }

    /**
     * Extracts the client part of an inchat URL and creates a new instance of a
     * {@link Participant} with the id of the URL.
     *
     * @param url The URL, may not be null nor empty and it has to be a valid
     * inchat URL.
     * @return The initialized client.
     * @throws IllegalArgumentException If the argument is null or not a valid
     * inchat URL.
     */
    public static Participant toClientByUrl(String url) {
        Exceptions.verifyArgumentNotEmpty(url);
        verifyUrlValidity(url);

        int beginIndex = SCHEME_PART.length() + LENGTH_OF_HEX_BLOCK + 1;
        int endIndex = beginIndex + LENGTH_OF_HEX_BLOCK;
        String clientPart = url.substring(beginIndex, endIndex);
        byte[] clientId = DatatypeConverter.parseHexBinary(clientPart);

        return new Participant(clientId);
    }

    private static void verifyUrlValidity(String url) {
        if (!url.matches(SCHEME_PART + "[a-f0-9]{" + LENGTH_OF_HEX_BLOCK + "}/[a-f0-9]{" + LENGTH_OF_HEX_BLOCK + "}(/.?)?")) {
            throw new IllegalArgumentException("The URL does not seem to be valid.");
        }
    }
}
