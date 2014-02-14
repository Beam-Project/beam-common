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

import java.awt.geom.IllegalPathStateException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConfigTest {

    private final static String TEST_FILE = "src/test/resources/org/inchat/common/test-config-file.conf";
    private final static String TEST_KEY = "keyword";
    private final static String TEST_VALUE = "please";
    private final static String TEST_CONFIG_DEFAULTS = "src/test/resources/org/inchat/common/test-write-defaults.conf";
    private final static String TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY = "src/test/resources/org/inchat/common/NEW_DIRECTORY/test-write-defaults.conf";
    private Participant participant;

    @Before
    public void setUp() {
        this.participant = new Participant(EccKeyPairGenerator.generate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNull() {
        Config.loadConfigFile(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNotExistingFile() {
        Config.loadConfigFile(new File("oops -- file not found"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteDefaultConfigOnNull() {
        Config.createDefaultConfig(null);
    }

    @Test(expected = IllegalPathStateException.class)
    public void testWriteDefaultConfigOnExistingDirectory() {
        Config.createDefaultConfig(new File(""));
    }

    @Test
    public void testWriteDefaultConfigWithExistingDirectory() throws IOException {
        deleteTestDefaultsFile();
        
        Config.createDefaultConfig(new File(TEST_CONFIG_DEFAULTS));

        File config = new File(TEST_CONFIG_DEFAULTS);
        assertTrue(config.exists());

        String writtenConfig = readFile(config);
        String expectedConfig = Config.getDefaultConfig();
        assertEquals(expectedConfig, writtenConfig);
        
        deleteTestDefaultsFile();
    }

    @Test
    public void testWriteDefaultConfigWithNewDirectory() throws IOException {
        deleteTestDefaultsFileWithNewDirectory();
        
        Config.createDefaultConfig(new File(TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY));

        File config = new File(TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY);
        assertTrue(config.exists());

        String writtenConfig = readFile(config);
        String expectedConfig = Config.getDefaultConfig();
        assertEquals(expectedConfig, writtenConfig);
        
        deleteTestDefaultsFileWithNewDirectory();
    }

    private void deleteTestDefaultsFile() {
        File target = new File(TEST_CONFIG_DEFAULTS);
        if (target.exists()) {
            target.delete();
        }
    }

    private void deleteTestDefaultsFileWithNewDirectory() {
        File target = new File(TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY);
        if (target.exists()) {
            target.delete();
        }
        
        if (target.getParentFile().exists()) {
            target.getParentFile().delete();
        }
    }

    private String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded);
    }

    @Test
    public void testLoadConfigFile() {
        Config.loadConfigFile(new File(TEST_FILE));
        assertNotNull(Config.getInstance().configFile);

        String actualValue = Config.getInstance().configFile.getProperty(TEST_KEY);
        assertEquals(TEST_VALUE, actualValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyOnNull() {
        Config.getProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyOnEmptyString() {
        Config.getProperty("");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetPropertyOnMissingConfigFile() {
        Config.getInstance().configFile = null;
        Config.getProperty(TEST_KEY);
    }

    @Test
    public void testGetProperty() {
        Config.loadConfigFile(new File(TEST_FILE));
        assertEquals(TEST_VALUE, Config.getProperty(TEST_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParticipantyOnNull() {
        Config.setParticipant(null);
    }

    @Test
    public void testSetParticipant() {
        Config.setParticipant(participant);
        assertSame(participant, Config.getInstance().participant);
    }

    @Test
    public void testGetParticipant() {
        Config.getInstance().participant = null;

        assertNull(Config.getParticipant());
        Config.setParticipant(participant);
        assertSame(participant, Config.getParticipant());
    }

}
