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

import com.google.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;

/**
 * Implements the {@link ClientCarrier} interface using <a
 * href="http://mqtt.org/">MQTT</a> to communicate with the clients.
 *
 * @see ClientCarrier
 * @see ClientCarrierModel
 */
public class ClientCarrierImpl implements ClientCarrier {

    private final ClientCarrierModel model;
    private final Executor executor;
    private final MqttConnectionPool connectionPool;
    ConcurrentHashMap<Participant, String> topics;
    MqttConnection subscriberConnection;

    @Inject
    public ClientCarrierImpl(ClientCarrierModel model, Executor executor, MqttConnectionPool connectionPool) {
        this.model = model;
        this.executor = executor;
        this.connectionPool = connectionPool;

        topics = new ConcurrentHashMap<>();
    }

    @Override
    public void bindParticipantToTopic(Participant participant, String topic) {
        topics.put(participant, topic);
    }

    @Override
    public void unbindParticipant(Participant participant) {
        topics.remove(participant);
    }

    /**
     * Delivers the given message to the given topic.
     *
     * @param message The message to send. This has to be already encrypted.
     * @param topic The topic to which to send this message to.
     * @throws CarrierException If the message could not be sent.
     */
    @Override
    public void deliverMessage(final byte[] message, final String topic) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                try {
                    MqttConnection connection = connectionPool.borrowObject();
                    connection.publish(topic, message);
                    connectionPool.returnObject(connection);
                } catch (Exception ex) {
                    throw new CarrierException("The message could not be sent: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Start to receive messages and therefore subscribes this
     * {@link ClientCarrier}.
     *
     * @throws CarrierException If the subscription was not successful.
     */
    @Override
    public void startReceiving() {
        final ClientCarrier thisCarrier = this;

        executor.runAsync(new Task() {
            @Override
            public void run() {
                try {
                    subscriberConnection = connectionPool.borrowObject();
                    subscriberConnection.subscribe(thisCarrier);

                    connectionPool.returnObject(subscriberConnection);
                    subscriberConnection = null;
                } catch (Exception ex) {
                    throw new CarrierException("Could not subscribe to topic: " + ex.getMessage());
                }
            }
        });
    }

    @Override
    public void receive(byte[] message, String topic) {
        if (!topic.startsWith(MQTT_IN_TOPIC_PREFIX)) {
            throw new CarrierException("The topic has to start with the prefix " + MQTT_IN_TOPIC_PREFIX);
        }

        String username = topic.substring(MQTT_IN_TOPIC_PREFIX.length());
        model.consumeMessage(message, username);
    }

    /**
     * Do not receive further messages.
     *
     * @throws IllegalStateException If this {@link ClientCarrier} was not
     * receiving before.
     */
    @Override
    public void stopReceiving() {
        if (subscriberConnection == null) {
            throw new IllegalStateException("This may only be invoked when receiving.");
        }

        subscriberConnection.doReceive(false);
    }

    @Override
    public void shutdown() {
        if (subscriberConnection != null) {
            stopReceiving();
        }

        connectionPool.close();
    }

}
