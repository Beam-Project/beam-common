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

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

public class User extends Participant {

    private static final long serialVersionUID = 1L;
    public static final String ADDRESS_PUBLIC_KEY_IDENTIFIER = "UK";
    public static final String ADDRESS_USERNAME_IDENTIFIER = "UN";
    public final static String DEFAULT_USERNAME = "Beamer";

    String username;
    Server server;
    MessagePack pack;

    /**
     * Creates a new {@link User}, configured with the given username and key
     * pair.
     *
     * @param username The username of this user.
     * @param keyPair The key pair of this user.
     * @throws IllegalArgumentException If at least one argument is null or the
     * username is empty.
     */
    public User(String username, KeyPair keyPair) {
        super(keyPair);

        Exceptions.verifyArgumentsNotEmpty(username);

        this.username = username;
        this.pack = new MessagePack();
    }

    /**
     * Creates a new {@link User}, configured with the given username, key pair
     * and server
     *
     * @param username The username of this user.
     * @param keyPair The key pair of this user.
     * @param server The server, at which this user is reachable over the
     * network.
     * @throws IllegalArgumentException If at least one argument is null or the
     * username is empty.
     */
    public User(String username, KeyPair keyPair, Server server) {
        this(username, keyPair);

        Exceptions.verifyArgumentsNotNull(server);

        this.server = server;
    }

    /**
     * Creates a new {@link User}, configured with the given address. Since the
     * address only contains the public key, the private key will not be set.
     *
     * @param address The address of the user.
     * @throws IllegalArgumentException If the given address is not valid.
     */
    public User(String address) {
        this(extractUsernameFromAddress(address),
                extractKeyPairFromAddress(address),
                extractServerFromAddress(address));
    }

    /**
     * Sets the given username.
     *
     * @param username The username to set.
     * @throws IllegalArgumentException If the username is null or empty.
     */
    public void setUsername(String username) {
        Exceptions.verifyArgumentsNotEmpty(username);

        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Sets the given {@link Server} to this {@link User}.
     *
     * @param server The server to set.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setServer(Server server) {
        Exceptions.verifyArgumentsNotNull(server);

        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public boolean isServerSet() {
        return server != null;
    }

    /**
     * Generates the user address. Since the format of this address is
     * {@code beam:[server part].[user part]}, the {@link Server} has to be set
     * before this method is invoked.
     *
     * @return The user address.
     * @throws IllegalStateException If the {@link Server} is not set at the
     * time of invocation.
     */
    public String getAddress() {
        if (server == null) {
            throw new IllegalStateException("The server has to be set to create the user address.");
        }

        Map<String, byte[]> addressMap = new LinkedHashMap<>();
        addressMap.put(ADDRESS_PUBLIC_KEY_IDENTIFIER, getPublicKeyAsBytes());
        addressMap.put(ADDRESS_USERNAME_IDENTIFIER, username.getBytes());

        try {
            byte[] addressBytes = pack.write(addressMap);
            return server.getAddress() + "." + Base58.encode(addressBytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create the address: " + ex.getMessage());
        }
    }

    /**
     * Creates a new {@link User} with the name <i>Beamer</i> and a newly
     * generated {@link KeyPair}.
     * <p>
     * The key pair contains both public and private key.
     *
     * @return The new user.
     */
    public static User generate() {
        return new User(DEFAULT_USERNAME, EccKeyPairGenerator.generate());
    }

    /**
     * Compares this {@link User} to the other object. The objects are only
     * equals if both are of the same type, both have the same keys (both
     * {@link PublicKey}s and/or both {@link PrivateKey}s) AND if they are
     * pairwise the same, and if both usernames are equal. Two {@link User}s are
     * also the same, when both keyPairs are null and the usernames are equal.
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

        User otherAsUser = (User) other;

        return username.equals(otherAsUser.username);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.username);
        return hash;
    }

    static KeyPair extractKeyPairFromAddress(String address) {
        try {
            Map<String, byte[]> addressValues = readAddressMap(address);
            return EccKeyPairGenerator.fromPublicKey(addressValues.get(ADDRESS_PUBLIC_KEY_IDENTIFIER));
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("The public key of the address is invalid.");
        }
    }

    static String extractUsernameFromAddress(String address) {
        try {
            Map<String, byte[]> addressValues = readAddressMap(address);
            return new String(addressValues.get(ADDRESS_USERNAME_IDENTIFIER));
        } catch (NullPointerException | IllegalStateException ex) {
            throw new IllegalArgumentException("The username of the address is invalid.");
        }
    }

    static Server extractServerFromAddress(String address) {

        return new Server(address.substring(0, address.indexOf('.')));
    }

    static Map<String, byte[]> readAddressMap(String address) {
        validateAddressFromat(address);

        MessagePack pack = new MessagePack();
        int userPartStartIndex = address.indexOf('.') + 1;

        try {
            byte[] addressBytes = Base58.decode(address.substring(userPartStartIndex));
            Value value = pack.read(addressBytes);
            return pack.convert(value, Templates.tMap(Templates.TString, Templates.TByteArray));
        } catch (MessageTypeException | IllegalArgumentException | IOException ex) {
            throw new IllegalArgumentException("The message pack format of the address is invalid.");
        }
    }

    static void validateAddressFromat(String address) {
        if (address == null || !address.matches("beam:[a-zA-Z0-9]{170,}\\.[a-zA-Z0-9]{170,}")) {
            throw new IllegalArgumentException("The given address is not valild.");
        }
    }

}
