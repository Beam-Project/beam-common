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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Exceptions;

/**
 * A {@link Server} is a {@link Participant} with a server address which
 * contains the server public key and its URL (HTTP).
 */
public class Server extends Participant {

    private static final long serialVersionUID = 1L;

    URL url;

    /**
     * Creates a new {@link Server}, configured with the given URL (HTTP URL,
     * under which {@link Message} can be sent to this server) and the given
     * {@link KeyPair}.
     *
     * @param url The URL under which the server is reachable.
     * @param keyPair The key pair of this server.
     * @throws IllegalArgumentException If at least one argument is null or the
     * arguments are not valid.
     */
    public Server(URL url, KeyPair keyPair) {
        super(keyPair);

        Exceptions.verifyArgumentsNotNull(url, keyPair);

        this.url = url;
    }
    
    public URL getUrl() {
        return url;
    }

    /**
     * Generates a new {@link Server}, initialized with a new {@link KeyPair}
     * and a the {@link URL} {@code http://example.com}.
     * <p>
     * Both, public and private keys will be initialized.
     *
     * @return The new {@link Server}.
     */
    public static Server generate() {
        try {
            return new Server(new URL("http://example.com"), EccKeyPairGenerator.generate());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Could not create a new server object: " + ex.getMessage());
        }
    }

}
