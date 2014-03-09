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
package org.inchat.common;

import java.util.HashMap;
import java.util.Map;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class MessageTest {

    private Message message;
    private String version;
    private Participant participant;
    private Map<String, byte[]> content;

    @Before
    public void setUp() {
        message = new Message();
        version = "1.2a";
        participant = new Participant(EccKeyPairGenerator.generate());
        content = new HashMap<>();
    }

    @Test
    public void testConstructorOnCreatingMap() {
        message = new Message();
        assertNotNull(message.content);
        assertTrue(message.content.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVersionOnNull() {
        message.setVersion(null);
    }

    @Test
    public void testSetVersion() {
        message.setVersion(version);
        assertEquals(version, message.version);
    }

    @Test
    public void testGetVersion() {
        message.version = version;
        assertEquals(version, message.getVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParticipantOnNull() {
        message.setParticipant(null);
    }

    @Test
    public void testSetParticipant() {
        message.setParticipant(participant);
        assertSame(participant, message.participant);
    }

    @Test
    public void testGetParticipant() {
        message.participant = participant;
        assertSame(participant, message.getParticipant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendContentOnNulls() {
        message.appendContent(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendContentOnNullKey() {
        message.appendContent(null, new byte[]{1, 2, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendContentOnNullContent() {
        message.appendContent(MessageField.CNT, null);
    }

    @Test
    public void testAppendContent() {
        byte[] value = "hello".getBytes();
        message.appendContent(MessageField.CNT, value);

        assertTrue(message.content.containsKey(MessageField.CNT.toString()));
        assertArrayEquals(value, message.content.get(MessageField.CNT.toString()));
    }

    @Test
    public void testGetContent() {
        message.content = content;
        assertSame(content, message.getContent());
    }
}
