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
import org.beamproject.common.crypto.EccKeyPairGenerator;
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
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetContentToBytesOnNull() {
        message.getContent(null);
    }
    
    @Test
    public void testGetContentToBytes() {
        message.appendContent(MessageField.VRS, version.getBytes());
        assertArrayEquals(version.getBytes(), message.getContent(MessageField.VRS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsContentOnNull() {
        message.containsContent(null);
    }

    @Test
    public void testContainsContent() {
        assertFalse(message.containsContent(MessageField.VRS));
        
        message.appendContent(MessageField.VRS, version.getBytes());
        assertTrue(message.containsContent(MessageField.VRS));
        
        message.content.remove(MessageField.VRS.toString());
        assertFalse(message.containsContent(MessageField.VRS));
    }
}
