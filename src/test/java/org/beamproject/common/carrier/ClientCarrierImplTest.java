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

import org.beamproject.common.ExecutorFake;
import org.beamproject.common.User;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.easymock.IAnswer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

public class ClientCarrierImplTest {

    private final User USER = User.generate();
    private final String IN_TOPIC = "in/username";
    private final String OUT_TOPIC = "out/username";
    private final String USERNAME = "username";
    private final byte[] MESSAGE = "myMessage".getBytes();
    private ExecutorFake executorFake;
    private MqttConnectionPool connectionPool;
    private MqttConnection connection;
    private ClientCarrierModel model;
    private ClientCarrierImpl carrier;

    @Before
    public void setUp() {
        executorFake = new ExecutorFake();
        connectionPool = createMock(MqttConnectionPool.class);
        connection = createMock(MqttConnection.class);
        model = createMock(ClientCarrierModel.class);
        carrier = new ClientCarrierImpl(model, executorFake, connectionPool);
    }

    @Test
    public void testBindUserToTopic() {
        carrier.bindParticipantToTopic(USER, IN_TOPIC);

        assertEquals(IN_TOPIC, carrier.topics.get(USER));
    }

    @Test
    public void testUnbindUser() {
        carrier.topics.put(USER, IN_TOPIC);

        carrier.unbindParticipant(USER);

        assertFalse(carrier.topics.contains(USER));
    }

    @Test
    public void testDeliverMessage() throws Exception {
        carrier.bindParticipantToTopic(USER, IN_TOPIC);
        expect(connectionPool.borrowObject()).andReturn(connection);
        connection.publish(IN_TOPIC, MESSAGE);
        expectLastCall();
        connectionPool.returnObject(connection);
        replay(connectionPool, connection);

        carrier.deliverMessage(MESSAGE, IN_TOPIC);

        verify(connectionPool, connection);
    }

    @Test
    public void testStartReceiving() throws Exception {
        expect(connectionPool.borrowObject()).andReturn(connection);
        connection.subscribe(carrier);
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                assertSame(connection, carrier.subscriberConnection);
                return null;
            }
        });
        connectionPool.returnObject(connection);
        expectLastCall();
        replay(connectionPool, connection);

        assertNull(carrier.subscriberConnection);

        carrier.startReceiving();

        assertNull(carrier.subscriberConnection);
        verify(connectionPool, connection);
    }

    @Test(expected = IllegalStateException.class)
    public void testStopReceivingWhenNotReceiving() {
        carrier.stopReceiving();
    }

    @Test
    public void testStopReceiving() {
        carrier.subscriberConnection = connection;
        connection.doReceive(false);
        expectLastCall();
        replay(connection);

        carrier.stopReceiving();

        verify(connection);
    }

    @Test
    public void testReceive() {
        model.consumeMessage(MESSAGE, USERNAME);
        expectLastCall().times(2);
        replay(model);

        carrier.receive(MESSAGE, IN_TOPIC);
        carrier.receive(MESSAGE, OUT_TOPIC);

        verify(model);
    }
    
    @Test(expected = CarrierException.class)
    public void testReceiveOnWrongTopicPrefix() {
        carrier.receive(MESSAGE, "not really a topic");
    }

    @Test
    public void testShutdown() {
        carrier.subscriberConnection = connection;

        connectionPool.close();
        expectLastCall();
        replay(connectionPool);

        carrier.shutdown();

        verify(connectionPool);
    }

}
