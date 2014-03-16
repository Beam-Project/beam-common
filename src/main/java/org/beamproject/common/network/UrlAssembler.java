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
package org.beamproject.common.network;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.beamproject.common.Contact;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.BouncyCastleIntegrator;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;

/**
 * Creates an URL of a {@link Participant} and its Server.
 */
public class UrlAssembler {

    public final static String SCHEME_PART = "beam:";
    public final static String SERVER_SEPERATOR = ".";
    public final static String PARAMETER_PART = "?";
    public final static String PARAMETER_SEPERATOR = "&";
    public final static String PARAMETER_KEY_VALUE_ASSINGER = "=";
    public final static String NAME_PARAMETER_KEY = "name";
    public final static int PUBLIC_KEY_BASE58_LENGTH = 164;
    public final static String SERVER_CLIENT_SCHEME_REGEX = "beam:[0-9a-zA-Z]"
            + "{" + PUBLIC_KEY_BASE58_LENGTH + "}"
            + "\\" + SERVER_SEPERATOR + "[0-9a-zA-Z]"
            + "{" + PUBLIC_KEY_BASE58_LENGTH + "}"
            + "(\\?[a-zA-Z0-9]+=[-_a-zA-Z0-9]+(&[a-zA-Z0-9]+=[-_a-zA-Z0-9]+)*)?";
    public final static String KEY_ALGORITHM_NAME = "EC";

    private UrlAssembler() {
        // Only static access.
    }

    /**
     * Assembles an beam URL of a client with its server.
     *
     * @param server The server, may not be null.
     * @param client The client, may not be null.
     * @param name The name of the client (the person).
     * @return The link.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public static String toUrlByServerAndClient(Participant server, Participant client, String name) {
        Exceptions.verifyArgumentsNotNull(server, client, name);
        Exceptions.verifyArgumentNotEmpty(name);

        return SCHEME_PART + server.getPublicKeyAsBase58()
                + SERVER_SEPERATOR + client.getPublicKeyAsBase58()
                + PARAMETER_PART + NAME_PARAMETER_KEY + PARAMETER_KEY_VALUE_ASSINGER + Base58.encode(name.getBytes());
    }

    /**
     * Assembles an beam URL of a server.
     *
     * @param server The server, may not be null.
     * @return The link.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static String toUrlByServer(Participant server) {
        Exceptions.verifyArgumentNotNull(server);

        return SCHEME_PART + server.getPublicKeyAsBase58();
    }

    /**
     * Converts a given {@code url} to a {@link Contact} instance. Only the
     * public key values will be set.
     *
     * @param url This has to be a valid beam url and may not be null.
     * @return The {@link Contact}, initialized with two {@link Participant}s, a
     * server and a client. Both do only contain a {@link PublicKey}.
     * @throws IllegalArgumentException If the argument is not a valid beam
     * url.
     * @throws IllegalStateException If the internally used {@link KeyFactory}
     * could not be set up.
     */
    public static Contact toContactByServerAndClientUrl(String url) {
        Exceptions.verifyArgumentNotEmpty(url);

        if (!url.matches(SERVER_CLIENT_SCHEME_REGEX)) {
            throw new IllegalArgumentException("The given url is not a valid beam url.");
        }

        int serverPartStart = SCHEME_PART.length();
        int serverPartEnd = serverPartStart + PUBLIC_KEY_BASE58_LENGTH;
        int clientPartStart = serverPartEnd + SERVER_SEPERATOR.length();
        int clientPartEnd = clientPartStart + PUBLIC_KEY_BASE58_LENGTH;
        int parameterPartStart = clientPartEnd + PARAMETER_PART.length();
        int parameterPartEnd = url.length();

        String serverPart = url.substring(serverPartStart, serverPartEnd);
        String clientPart = url.substring(clientPartStart, clientPartEnd);
        String parameterPart = url.substring(parameterPartStart, parameterPartEnd);

        PublicKey serverKey = toPublicKey(serverPart);
        PublicKey clientKey = toPublicKey(clientPart);

        KeyPair serverKeyPair = new KeyPair(serverKey, null);
        KeyPair clientKeyPair = new KeyPair(clientKey, null);

        Participant server = new Participant(serverKeyPair);
        Participant client = new Participant(clientKeyPair);

        String[] parameterPairs = parameterPart.split(PARAMETER_SEPERATOR);
        String nameAsBase58 = getParameterValueByKey(parameterPairs, NAME_PARAMETER_KEY);
        String name = new String(Base58.decode(nameAsBase58));

        return new Contact(server, client, name);
    }

    private static String getParameterValueByKey(String[] parameterPairs, String key) {
        for (String parameterPair : parameterPairs) {
            String[] keyAndValue = parameterPair.split(PARAMETER_KEY_VALUE_ASSINGER);
            if (keyAndValue[0].equals(key)) {
                return keyAndValue[1];
            }
        }

        throw new IllegalArgumentException("The parameter '" + key + "' could not be found.");
    }

    private static PublicKey toPublicKey(String pubicKeyAsBase58) {
        BouncyCastleIntegrator.initBouncyCastleProvider();

        try {
            byte[] asBytes = Base58.decode(pubicKeyAsBase58);

            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            return keyFactory.generatePublic(new X509EncodedKeySpec(asBytes));
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new IllegalArgumentException("The given Base58 string cannot be converted to a public key: " + ex.getMessage());
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Could not load Bouncy Castle: " + ex.getMessage());
        }
    }

}
