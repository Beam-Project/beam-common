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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConfigTest {

    private enum Keys implements ConfigKey {

        testKey, otherKey;
    }

    private final String DEFAULT_VALUE = "test value";
    private final String CONFIG_FILE_NAME = "test-default-config.conf";
    private final String CONFIG_DIRECTORY = "config-test-directory/test-write-defaults.conf";
    private Config config;

    @Before
    public void setUp() {
        config = new Config(CONFIG_FILE_NAME);
    }

    @After
    public void cleanUp() {
        config.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        config = new Config(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnEmptyString() {
        config = new Config("");
    }

    @Test
    public void testConstructorOnAssertion() {
        config = new Config(CONFIG_FILE_NAME);
        assertEquals(CONFIG_FILE_NAME, config.configFile.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyOnNull() {
        config.getProperty(null);
    }

    @Test
    public void testGetPropertyOnMissingConfigEntry() {
        config.config = new Properties();
        assertNull(config.getProperty(Keys.testKey));
    }

    @Test
    public void testConstructorOnCreatingDirectory() throws IOException {
        config = new Config(CONFIG_DIRECTORY);
        assertTrue(config.configFile.exists());
        config.setProperty(Keys.testKey, "changed value");

        Config reader = new Config(CONFIG_DIRECTORY);
        assertEquals("changed value", reader.getProperty(Keys.testKey));

        config.delete();
        config.configDirectory.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsKeyExistingOnNull() {
        config.isKeyExisting(null);
    }

    @Test
    public void testIsKeyExisting() {
        assertFalse(config.isKeyExisting(Keys.otherKey));
        config.setProperty(Keys.otherKey, DEFAULT_VALUE);
        assertTrue(config.isKeyExisting(Keys.otherKey));
    }

    @Test
    public void testGetProperty() {
        config.setProperty(Keys.testKey, DEFAULT_VALUE);
        
        Config reader = new Config(CONFIG_FILE_NAME);
        assertEquals(DEFAULT_VALUE, reader.getProperty(Keys.testKey));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyOnNulls() {
        config.setProperty(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyOnNullKey() {
        config.setProperty(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPropertyOnNullValue() {
        config.setProperty(Keys.testKey, null);
    }

    @Test
    public void testSetProperty() {
        config.setProperty(Keys.testKey, "anonymous");

        Config reader = new Config(CONFIG_FILE_NAME);
        assertEquals("anonymous", reader.getProperty(Keys.testKey));
    }

    @Test
    public void testDelete() {
        File configFile = config.configFile;
        assertTrue(configFile.exists());

        config.delete();
        assertFalse(configFile.exists());
        assertTrue(config.config.isEmpty());
    }

}