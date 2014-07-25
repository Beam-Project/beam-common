/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-server.
 *
 * beam-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.carrier;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.fusesource.mqtt.client.MQTT;

/**
 * This factory class is required by the Apache Commons Pool library. It
 * provides methods to create {@link MqttConnection} objects.
 */
public class MqttConnectionPoolFactory extends BasePooledObjectFactory<MqttConnection> {

    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final String username;
    @Getter
    private final String subscriberTopic;

    @Inject
    public MqttConnectionPoolFactory(String host, int port, String username, String subscriberTopic) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.subscriberTopic = subscriberTopic;
    }

    @Override
    public MqttConnection create() throws Exception {
        MqttConnection connection = new MqttConnection(new MQTT(),
                host,
                port,
                username,
                subscriberTopic);

        connection.connect();

        return connection;
    }

    @Override
    public void destroyObject(PooledObject<MqttConnection> pooledObject) throws Exception {
        pooledObject.getObject().disconnect();
    }

    @Override
    public PooledObject<MqttConnection> wrap(MqttConnection obj) {
        return new DefaultPooledObject<>(obj);
    }

}
