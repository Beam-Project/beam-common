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
import java.util.Objects;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
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

    /**
     * Constructs a new {@link Server} from the given address.
     * <p>
     * The address has to be like
     * {@code beam:[Base58 public key]?url=[Base58 URL]}.
     *
     * @param address The address to use.
     * @throws IllegalArgumentException If the argument is not a valid address.
     */
    public Server(String address) {
        this(extractUrlFromAddress(address), extractKeyPairFromAddress(address));
    }

    public URL getUrl() {
        return url;
    }

    public String getAddress() {
        return "beam:" + getPublicKeyAsBase58() + "?url=" + Base58.encode(url.toString().getBytes());
    }

    /**
     * Compares this {@link Server} to the other object. The objects are only
     * equals if both are of the same type, both have the same keys (both
     * {@link PublicKey}s and/or both {@link PrivateKey}s) AND if they are
     * pairwise the same, and if both URLs are equal. Two {@link Server}s are
     * also the same, when both key pairs are null and the URLs are equal.
     *
     * @param other Another object to compare with this one.
     * @return true, if the key pairs are equals, otherwise false.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != this.getClass()) {
            return false;
        }

        Participant otherAsParticipant = (Participant) other;

        if (!super.equals(otherAsParticipant)) {
            return false;
        }

        Server otherAsServer = (Server) other;

        return url.equals(otherAsServer.url);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.url);
        return hash;
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

    static URL extractUrlFromAddress(String address) {
        validateAddressFromat(address);

        try {
            String urlPart = address.substring(address.indexOf("?url=") + 5);
            String urlAsString = new String(Base58.decode(urlPart));
            return new URL(urlAsString);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The URL of the address is invalid.");
        }
    }

    static KeyPair extractKeyPairFromAddress(String address) {
        validateAddressFromat(address);

        try {
            String keyPairPart = address.substring("beam:".length(), address.indexOf('?'));
            return EccKeyPairGenerator.fromPublicKey(Base58.decode(keyPairPart));
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("The public key of the address is invalid.");
        }
    }

    static void validateAddressFromat(String address) {
        if (address == null
                || !address.matches("beam:[a-zA-Z0-9]+\\?url=[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("The given address is not valild.");
        }
    }

}
