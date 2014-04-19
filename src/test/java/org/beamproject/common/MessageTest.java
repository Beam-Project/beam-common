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

public class MessageTest {
    
    private final String VERSION = "1.2a";
    private Message message;
    private Participant participant;
    private Map<String, byte[]> content;
    
    @Before
    public void setUp() {
        message = new Message();
        participant = Participant.generate();
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
        message.setVersion(VERSION);
        assertEquals(VERSION, message.version);
    }
    
    @Test
    public void testGetVersion() {
        message.version = VERSION;
        assertEquals(VERSION, message.getVersion());
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
        message.putContent(null, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAppendContentOnNullKey() {
        message.putContent(null, new byte[]{1, 2, 3});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAppendContentOnNullContent() {
        message.putContent(MessageField.CNT, null);
    }
    
    @Test
    public void testAppendContent() {
        byte[] value = "hello".getBytes();
        message.putContent(MessageField.CNT, value);
        
        assertTrue(message.content.containsKey(MessageField.CNT.toString()));
        assertArrayEquals(value, message.content.get(MessageField.CNT.toString()));
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
        message.putContent(MessageField.VRS, VERSION.getBytes());
        assertArrayEquals(VERSION.getBytes(), message.getContent(MessageField.VRS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsContentOnNull() {
        message.containsContent(null);
    }

    @Test
    public void testContainsContent() {
        assertFalse(message.containsContent(MessageField.VRS));
        
        message.putContent(MessageField.VRS, VERSION.getBytes());
        assertTrue(message.containsContent(MessageField.VRS));
        
        message.content.remove(MessageField.VRS.toString());
        assertFalse(message.containsContent(MessageField.VRS));
    }
}
