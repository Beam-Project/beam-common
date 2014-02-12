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
package org.inchat.common.util;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigTest {

    private final static String TEST_FILE = "test-config-file.conf";
    private final static String TEST_KEY = "keyword";
    private final static String TEST_VALUE = "please";

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNull() {
        Config.loadConfigFile(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNotExistingFile() {
        Config.loadConfigFile(new File("oops -- file not found"));
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

}
