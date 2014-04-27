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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.beamproject.common.MessageField.ContentField.*;

public class MessageTest {

    private final String VERSION = "1.2a";
    private Message message;
    private Participant recipient;
    private Map<String, byte[]> content;

    @Before
    public void setUp() {
        recipient = Participant.generate();
        message = new Message(recipient);
        content = new HashMap<>();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        message = new Message(null);
    }

    @Test
    public void testConstructor() {
        assertEquals(Message.VERSION, message.version);
        assertSame(recipient, message.recipient);
        assertNotNull(message.content);
        assertTrue(message.content.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVersionOnNull() {
        message.setVersion(null);
    }

    @Test
    public void testSetVersion() {
        message.setVersion(VERSION);
        assertEquals(VERSION, message.version);
    }

    @Test
    public void testGetVersion() {
        message.version = VERSION;
        assertEquals(VERSION, message.getVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRecipientOnNull() {
        message.setRecipient(null);
    }

    @Test
    public void testSetRecipient() {
        message.setRecipient(recipient);
        assertSame(recipient, message.recipient);
    }

    @Test
    public void testGetRecipient() {
        message.recipient = recipient;
        assertSame(recipient, message.getRecipient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutContentOnNullKey() {
        message.putContent(null, new byte[]{1, 2, 3});
    }

    @Test
    public void testPutContentAsBytes() {
        byte[] value = "hello".getBytes();
        message.putContent(MSG, value);

        assertTrue(message.content.containsKey(MSG.toString()));
        assertArrayEquals(value, message.content.get(MSG.toString()));
    }

    @Test
    public void testPutContentAsString() {
        String value = "hello";
        message.putContent(MSG, value);

        assertTrue(message.content.containsKey(MSG.toString()));
        assertArrayEquals(value.getBytes(), message.content.get(MSG.toString()));
    }

    @Test
    public void testGetContent() {
        message.content = content;
        assertSame(content, message.getContent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetContentToBytesOnNull() {
        message.getContent(null);
    }

    @Test
    public void testGetContentToBytes() {
        message.putContent(MSG, VERSION.getBytes());
        assertArrayEquals(VERSION.getBytes(), message.getContent(MSG));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsContentOnNull() {
        message.containsContent(null);
    }

    @Test
    public void testContainsContent() {
        assertFalse(message.containsContent(MSG));

        message.putContent(MSG, VERSION.getBytes());
        assertTrue(message.containsContent(MSG));

        message.content.remove(MSG.toString());
        assertFalse(message.containsContent(MSG));
    }
}
