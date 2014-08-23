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
import java.util.Properties;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class FilesTest {

    private final String TEST_FILE_PATH = "./FilesTest-storeProperties.conf";
    private final File FILE = new File(TEST_FILE_PATH);
    private Files files;
    private Properties properties, reader;

    @Before
    public void setUp() {
        deleteFile();

        files = new Files();
        properties = new Properties();
        properties.setProperty("1", "myValue1");
        properties.setProperty("2", "myValue2");
        properties.setProperty("3", "myValue3");
    }

    @After
    public void cleanUp() {
        deleteFile();
    }

    private void deleteFile() {
        if (FILE.exists()) {
            FILE.delete();
        }

        assert !FILE.exists() : "File not deleted correctly!";
    }

    @Test
    public void testStoreAndLoadConfigIfAvailable() {
        assertFalse(FILE.exists());
        files.storeProperies(properties, TEST_FILE_PATH);
        assertTrue(FILE.exists());

        reader = files.loadConfigIfAvailable(TEST_FILE_PATH);
        assertEquals(3, reader.stringPropertyNames().size());
        compare("1");
        compare("2");
        compare("3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorePrpertiesOnNulls() {
        files.storeProperies(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorePrpertiesOnNullProperties() {
        files.storeProperies(null, TEST_FILE_PATH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorePrpertiesOnNullPath() {
        files.storeProperies(properties, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorePrpertiesOnIllegalPath() {
        files.storeProperies(properties, "/\\     //////////illegal path /");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorePrpertiesOnEmptyPath() {
        files.storeProperies(properties, "");
    }

    private void compare(String id) {
        assertEquals(properties.getProperty(id), reader.getProperty(id));
    }

}
