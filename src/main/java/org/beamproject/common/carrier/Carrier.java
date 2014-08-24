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
package org.beamproject.common.carrier;

import org.beamproject.common.Participant;

/**
 * A {@link Carrier} is like a postal service (respectively a <i>carrier of
 * mail</i>) in real life; one places a letter at the post office and they send
 * it to its destination. We can also receive letters delivered by a carrier.
 * Implementations of {@link Carrier} do the same, but probably via different
 * media or protocols (MQTT, HTTP, pneumatic post, carrier pigeon, etc.).
 * <p>
 * When sending a message using a {@link Carrier}, the recipient (a
 * {@link Participant}) and the message (a byte array) have to be provided. The
 * {@link Carrier} tries then to deliver the message to its destination.
 * <p>
 * Please note, that a {@link Carrier} (hopefully like the local post office)
 * only knows the recipient with its address and possesses the message, but
 * never sees the content of the message. Therefore, no {@link Carrier} does any
 * cryptographic operations.
 *
 * @param <T> The type of the Carrier, either {@link ClientCarrier} or
 * {@link ServerCarrier}.
 * @see ClientCarrier
 * @see ServerCarrier
 */
public interface Carrier<T extends CarrierModel> {

    /**
     * Delivers the given message to the targeted recipient. Depending on the
     * concrete transport medium, the target may be an MQTT topic or an HTTP
     * server address.
     *
     * @param message The message to send. This has to be already encrypted.
     * @param target The target of the message.
     */
    public void deliverMessage(byte[] message, String target);

    /**
     * Start to receive messages.
     */
    public void startReceiving();

    /**
     * Takes a received message and redirects it to the configured
     * {@link CarrierModel}. This may only be invoked after
     * {@link #startReceiving()} was successfully invoked.
     *
     * @param message The new message to handle.
     * @param sender Information about the sender. Depending on the used
     * transport medium, this may have a different content. For example when
     * using MQTT, this would be the MQTT username.
     * @throws CarrierException If anything goes wrong during receiving the
     * message.
     */
    public void receive(byte[] message, String sender);

    /**
     * Do not receive further messages.
     */
    public void stopReceiving();

    /**
     * Stop receiving further incoming messages and close all connections.
     */
    public void shutdown();
}
