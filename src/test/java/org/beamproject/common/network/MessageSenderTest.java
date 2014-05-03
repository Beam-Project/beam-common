/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.network;

import java.net.MalformedURLException;
import java.net.URL;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.util.Base64;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class MessageSenderTest {

    private Participant localParticipant;
    private Participant remoteParticipant;
    private Message message;
    private final String urlAsString = "http://localhost:1234";
    private URL url;
    private MessageSender sender;
    private HttpConnector connector;

    @Before
    public void setUp() throws MalformedURLException {
        connector = createMock(HttpConnector.class);
        localParticipant = Participant.generate();
        remoteParticipant = Participant.generate();
        message = new Message(HANDSHAKE, remoteParticipant);
        url = new URL(urlAsString);
        sender = new MessageSender(url, localParticipant);
        sender.connector = connector;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        sender = new MessageSender(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUrl() {
        sender = new MessageSender(null, localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullParticipant() {
        sender = new MessageSender(url, null);
    }

    @Test
    public void testConstructor() {
        assertNotNull(sender.packer);
        assertNotNull(sender.connector);
        assertSame(localParticipant, sender.localParticipant);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendOnNull() {
        sender.send(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAndReceiveOnNull() {
        sender.sendAndReceive(null);
    }

    @Test
    public void testSend() {
        expect(connector.executePost(anyObject(byte[].class))).andReturn(null);
        replay(connector);

        message.putContent(MSG, "");
        sender = new MessageSender(url, localParticipant);
        sender.connector = connector;
        sender.send(message);

        verify(connector);
    }

    @Test
    public void testSendAndReceive() {
        Message response = new Message(HANDSHAKE, localParticipant);
        response.putContent(MSG, "response");
        byte[] encryptedResponse = new CryptoPacker().packAndEncrypt(response);
        byte[] encryptedBase64Response = Base64.encode(encryptedResponse).getBytes();
        expect(connector.executePost(anyObject(byte[].class))).andReturn(encryptedBase64Response);
        replay(connector);

        message.putContent(MSG, "");
        sender = new MessageSender(url, localParticipant);
        sender.connector = connector;
        Message responsed = sender.sendAndReceive(message);

        assertArrayEquals("response".getBytes(), responsed.getContent(MSG));
        verify(connector);
    }

}
