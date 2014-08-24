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

import org.beamproject.common.message.Message;

/**
 * The {@link CarrierModel}s contain the business logic of the {@link Carrier}s.
 *
 * @param <T> The type of a {@link CarrierModel} what has to be a subtype of
 * {@link Carrier}.
 *
 * @see ClientCarrier
 * @see ServerCarrier
 * @see ClientCarrierModel
 * @see ServerCarrierModel
 */
public interface CarrierModel<T extends Carrier> {

    /**
     * Prepares the {@link Carrier} of this model and starts listening/receiving
     * incoming messages.
     */
    public void startReceiving();

    /**
     * Stops the {@link Carrier} of this model receiving new messages.
     */
    public void stopReceiving();

    /**
     * Consumes messages that are being received by the internally used
     * {@link Carrier}.
     *
     * @param message The message as byte array.
     * @param sender Information about the sender, depending on the used
     * transport medium. For example, when using MQTT, this would be the MQTT
     * username of the sender.
     */
    public void consumeMessage(byte[] message, String sender);

    /**
     * Encrypts the given message for its recipient and sends it to that via the
     * {@link Carrier} of the {@link CarrierModel}.
     *
     * @param message The message to encrypt and sen.d
     * @param target The target of this message, depending on the used transport
     * medium. For example, when using MQTT, this would be the MQTT username of
     * the target.
     */
    public void encryptAndSend(Message message, String target);

    /**
     * Shuts the {@link Carrier} of this model down and closes all connections.
     */
    public void shutdown();
}
