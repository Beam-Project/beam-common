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
package org.beamproject.common.util;

import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConfigTest {

    private enum Key {

        PASSWORD;
    }
    private Config<Key> config;

    @Before
    public void setUp() {
        config = new Config<>();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        config = new Config<>(null);
    }

    @Test
    public void testConstructor() {
        Properties properties = new Properties();
        String base58edValue = Base58.encode("1234".getBytes());
        properties.setProperty(Key.PASSWORD.toString(), base58edValue);
        config = new Config<>(properties);

        assertEquals("1234", config.getAsString(Key.PASSWORD));
    }

    @Test
    public void testSetAndGetAsString() {
        String value = "hello you";
        config.set(Key.PASSWORD, value);
        assertEquals(value, config.getAsString(Key.PASSWORD));
    }

    @Test
    public void testSetAndGetAsBytes() {
        byte[] value = "hello you".getBytes();
        config.set(Key.PASSWORD, value);
        assertSame(value, config.getAsBytes(Key.PASSWORD));
    }

    @Test
    public void testContainsKey() {
        assertFalse(config.contains(Key.PASSWORD));
        config.set(Key.PASSWORD, "hi");
        assertTrue(config.contains(Key.PASSWORD));
    }

    @Test
    public void testCopyToPropertiesOnEmptyConfig() {
        Properties properties = config.copyToProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testCopyToProperties() {
        String value = "1234";
        String base58edValue = Base58.encode(value.getBytes());

        config.set(Key.PASSWORD, value);
        Properties properties = config.copyToProperties();
        assertEquals(base58edValue, properties.getProperty(Key.PASSWORD.toString()));
    }

}
