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
package org.beamproject.common.util;

import java.io.File;
import org.aeonbits.owner.ConfigFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class ConfigWriterTest {

    private final String FOLDER = "./config-folder/";
    private final String FILE = "test.conf";
    private final File configFolder = new File(FOLDER);
    private final File configFile = new File(FOLDER + FILE);
    private ConfigWriter writer;
    private ConfigBase config;

    @Before
    public void setUp() {
        writer = new ConfigWriter();
        config = ConfigFactory.create(ConfigBase.class);
    }

    @After
    public void cleanUp() {
        configFile.delete();
        configFolder.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreConfigOnNulls() {
        writer.writeConfig(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreConfigOnNullFolder() {
        writer.writeConfig(config, null, FILE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreConfigOnNullFile() {
        writer.writeConfig(config, FOLDER, null);
    }

    @Test
    public void testStoreConfig() {
        assertFalse(configFolder.exists());
        assertFalse(configFile.exists());

        writer.writeConfig(config, FOLDER, FILE);

        assertTrue(configFolder.exists());
        assertTrue(configFile.exists());
    }

}
