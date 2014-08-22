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
package org.beamproject.common.message;

import java.util.HashMap;
import java.util.Map;
import org.beamproject.common.Participant;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.beamproject.common.message.MessageField.ContentField.*;
import static org.beamproject.common.message.MessageField.ContentField.TypeValue.*;

public class MessageTest {

    private final String VERSION = "1.2a";
    private Message message;
    private Participant recipient;
    private Map<String, byte[]> content;

    @Before
    public void setUp() {
        recipient = Participant.generate();
        message = new Message();
        content = new HashMap<>();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        message = new Message(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullType() {
        message = new Message(null, recipient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullRecipient() {
        message = new Message(HS_CHALLENGE, null);
    }

    @Test
    public void testConstructor() {
        message = new Message(HS_CHALLENGE, recipient);
        assertEquals(Message.VERSION, message.version);
        assertSame(recipient, message.recipient);
        assertNotNull(message.content);
        assertArrayEquals(HS_CHALLENGE.getBytes(), message.content.get(TYP.toString()));
        assertEquals(1, message.content.size());
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
    public void testSetTypeOnNull() {
        message.setType(null);
    }

    @Test
    public void testSetType() {
        message.setType(FORWARD);
        assertArrayEquals(FORWARD.getBytes(), message.content.get(TYP.toString()));
    }

    @Test
    public void testGetType() {
        message.setType(FORWARD);
        assertEquals(FORWARD, message.getType());
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
    public void testPutContentAsEnum() {
        message.putContent(TYP, HS_INVALIDATE);

        assertTrue(message.content.containsKey(TYP.toString()));
        assertArrayEquals(HS_INVALIDATE.getBytes(), message.content.get(TYP.toString()));
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
