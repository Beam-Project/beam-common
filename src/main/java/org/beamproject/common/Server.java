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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.message.Message;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

/**
 * A {@link Server} is a {@link Participant} with a server address which
 * contains the server public key and its URL (HTTP).
 */
public class Server extends Participant {

    private static final long serialVersionUID = 1L;
    private static final int ADDRESS_SCHEMA_OFFSET = 5;
    public static final int MQTT_DEFAULT_PORT = 3625;
    public static final String ADDRESS_PUBLIC_KEY_IDENTIFIER = "SK";
    public static final String ADDRESS_MQTT_HOST_IDENTIFIER = "SMH";
    public static final String ADDRESS_MQTT_PORT_IDENTIFIER = "SMP";
    public static final String ADDRESS_HTTP_URL_IDENTIFIER = "SHU";
    /**
     * The URL at which this server can be connected via. The complete URL,
     * including path, query and fragments is used to connect this server via
     * HTTP.
     */
    @Getter
    URL httpUrl;
    /**
     * The socket address at which this server respectively its MQTT broker can
     * be connected via MQTT.
     */
    @Getter
    InetSocketAddress mqttAddress;

    /**
     * Creates a new {@link Server}, configured with the given URL (HTTP URL,
     * under which {@link Message} can be sent to this server) and the given
     * {@link KeyPair}.
     *
     * @param mqttAddress The socket address of the MQTT broker of this server
     * (hostname or IP address and port).
     * @param httpUrl The URL under which the server is reachable.
     * @param keyPair The key pair of this server.
     * @throws IllegalArgumentException If at least one argument is null or the
     * arguments are not valid.
     */
    public Server(InetSocketAddress mqttAddress, URL httpUrl, KeyPair keyPair) {
        super(keyPair);

        Exceptions.verifyArgumentsNotNull(mqttAddress, httpUrl, keyPair);

        this.mqttAddress = mqttAddress;
        this.httpUrl = httpUrl;
    }

    /**
     * Constructs a new {@link Server} from the given address.
     * <p>
     * The address has to be like
     * {@code beam:[Base58 of messagepack bytes of public key, the mqttAddress, the httpUrl, and possibliy further information]}.
     *
     * @param address The address to use.
     * @throws IllegalArgumentException If the argument is not a valid address.
     */
    public Server(String address) {
        this(extractMqttAddress(address),
                extractUrlFromAddress(address),
                extractKeyPairFromAddress(address));
    }

    public String getAddress() {
        MessagePack pack = new MessagePack();
        Map<String, byte[]> addressMap = new LinkedHashMap<>();
        addressMap.put(ADDRESS_PUBLIC_KEY_IDENTIFIER, getPublicKeyAsBytes());
        addressMap.put(ADDRESS_HTTP_URL_IDENTIFIER, getHttpUrl().toString().getBytes());
        addressMap.put(ADDRESS_MQTT_HOST_IDENTIFIER, mqttAddress.getHostString().getBytes());
        addressMap.put(ADDRESS_MQTT_PORT_IDENTIFIER, String.valueOf(mqttAddress.getPort()).getBytes());

        try {
            byte[] addressBytes = pack.write(addressMap);
            return "beam:" + Base58.encode(addressBytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create the address: " + ex.getMessage());
        }
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

        return httpUrl.equals(otherAsServer.httpUrl);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.httpUrl);
        return hash;
    }

    /**
     * Generates a new {@link Server}, initialized with a new {@link KeyPair},
     * the HTTP {@link URL} {@code http://example.com}, and the MQTT
     * {@link InetSocketAddress} hostname {@code example.com}:{@code 3625}.
     * <p>
     * Both, public and private keys will be initialized.
     *
     * @return The new {@link Server}.
     */
    public static Server generate() {
        try {
            return new Server(
                    new InetSocketAddress("example.com", MQTT_DEFAULT_PORT),
                    new URL("http://example.com"),
                    EccKeyPairGenerator.generate());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Could not create a new server object: " + ex.getMessage());
        }
    }

    static URL extractUrlFromAddress(String address) {
        try {
            Map<String, byte[]> addressValues = readAddressMap(address);
            return new URL(new String(addressValues.get(ADDRESS_HTTP_URL_IDENTIFIER)));
        } catch (NullPointerException | IllegalArgumentException | IOException ex) {
            throw new IllegalArgumentException("The URL of the address is invalid: " + ex.getMessage());
        }
    }

    static KeyPair extractKeyPairFromAddress(String address) {
        try {
            Map<String, byte[]> addressValues = readAddressMap(address);
            return fromPublicKey(addressValues.get(ADDRESS_PUBLIC_KEY_IDENTIFIER));
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("The public key of the address is invalid: " + ex.getMessage());
        }
    }

    static InetSocketAddress extractMqttAddress(String address) {
        try {
            Map<String, byte[]> addressValues = readAddressMap(address);
            byte[] hostAsBytes = addressValues.get(ADDRESS_MQTT_HOST_IDENTIFIER);
            byte[] portAsBytes = addressValues.get(ADDRESS_MQTT_PORT_IDENTIFIER);

            String host = new String(hostAsBytes);
            int port = Integer.parseInt(new String(portAsBytes));

            return new InetSocketAddress(host, port);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("The MQTT address is invalid: " + ex.getMessage());
        }
    }

    static Map<String, byte[]> readAddressMap(String address) {
        validateAddressFromat(address);

        MessagePack pack = new MessagePack();

        try {
            byte[] addressBytes = Base58.decode(address.substring(ADDRESS_SCHEMA_OFFSET));
            Value value = pack.read(addressBytes);
            return pack.convert(value, Templates.tMap(Templates.TString, Templates.TByteArray));
        } catch (MessageTypeException | IllegalArgumentException | IOException ex) {
            throw new IllegalArgumentException("The message pack format of the address is invalid: " + ex.getMessage());
        }
    }

    static void validateAddressFromat(String address) {
        if (address == null || !address.matches("beam:[a-zA-Z0-9]{170,}")) {
            throw new IllegalArgumentException("The given address is not valild.");
        }
    }

}
