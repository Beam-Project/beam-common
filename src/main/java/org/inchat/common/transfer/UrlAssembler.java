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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.inchat.common.Contact;
import org.inchat.common.Participant;
import org.inchat.common.crypto.BouncyCastleIntegrator;
import org.inchat.common.util.Exceptions;

/**
 * Creates an URL of a {@link Participant} and its Server.
 */
public class UrlAssembler {

    public final static String SCHEME_PART = "inchat:";
    public final static String SEPERATOR = ".";
    public final static int PUBLIC_KEY_HEX_LENGTH = 240;
    public final static String SERVER_CLIENT_SCHEME_REGEX = "inchat:[0-9a-fA-F]"
            + "{" + PUBLIC_KEY_HEX_LENGTH + "}"
            + "\\" + SEPERATOR + "[0-9a-fA-F]"
            + "{" + PUBLIC_KEY_HEX_LENGTH + "}";
    public final static String KEY_ALGORITHM_NAME = "EC";

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

    /**
     * Converts a given {@code url} to a {@link Contact} instance. Only the
     * public key values will be set.
     *
     * @param url This has to be a valid inchat url and may not be null.
     * @return The {@link Contact}, initialized with two {@link Participant}s, a
     * server and a client. Both do only contain a {@link PublicKey}.
     * @throws IllegalArgumentException If the argument is not a valid inchat
     * url.
     * @throws IllegalStateException If the internally used {@link KeyFactory}
     * could not be set up.
     */
    public static Contact toContactByServerAndClientUrl(String url) {
        Exceptions.verifyArgumentNotEmpty(url);

        if (!url.matches(SERVER_CLIENT_SCHEME_REGEX)) {
            throw new IllegalArgumentException("The given url is not a valid inchat url.");
        }

        int serverPartStart = SCHEME_PART.length();
        int serverPartEnd = serverPartStart + PUBLIC_KEY_HEX_LENGTH;
        int clientPartStart = serverPartEnd + SEPERATOR.length();
        int clientPartEnd = clientPartStart + PUBLIC_KEY_HEX_LENGTH;

        String serverPart = url.substring(serverPartStart, serverPartEnd);
        String clientPart = url.substring(clientPartStart, clientPartEnd);

        PublicKey serverKey = toPublicKey(serverPart);
        PublicKey clientKey = toPublicKey(clientPart);

        KeyPair serverKeyPair = new KeyPair(serverKey, null);
        KeyPair clientKeyPair = new KeyPair(clientKey, null);

        Participant server = new Participant(serverKeyPair);
        Participant client = new Participant(clientKeyPair);
        return new Contact(server, client);
    }

    private static PublicKey toPublicKey(String pubicKeyAsHex) {
        BouncyCastleIntegrator.initBouncyCastleProvider();

        try {
            byte[] asBytes = DatatypeConverter.parseHexBinary(pubicKeyAsHex);

            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            return keyFactory.generatePublic(new X509EncodedKeySpec(asBytes));
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new IllegalArgumentException("The given hex string cannot be converted to a public key: " + ex.getMessage());
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Could not load Bouncy Castle: " + ex.getMessage());
        }
    }

}
