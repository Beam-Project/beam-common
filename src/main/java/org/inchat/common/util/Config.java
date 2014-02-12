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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This singleton can be used to load a specific configuration file.
 */
public class Config {

    Properties configFile;

    private Config() {
        // Only static access.

    }

    /**
     * Loads the config file. It has to be a key/value pair file like the Java
     * properties files.
     *
     * @param file The file to read.
     * @throws IllegalArgumentException If the argument is null or the file
     * cannot be found.
     */
    public static void loadConfigFile(File file) {
        Exceptions.verifyArgumentNotNull(file);

        getInstance().configFile = new java.util.Properties();

        try (FileInputStream fileStream = new FileInputStream(file)) {
            getInstance().configFile.load(fileStream);
            fileStream.close();
        } catch (IOException ex) {
            String message = "The file cannot be found.";
            String currentPath;

            try {
                currentPath = (new File("")).getCanonicalPath();
            } catch (IOException ex1) {
                throw new IllegalArgumentException(message);
            }
            throw new IllegalArgumentException(message + " Enter the path relative to '" + currentPath + "'.");
        }
    }

    /**
     * Searches the {@code key} and returns the value, if found.
     *
     * @param key The key of the key/value pair in the config file.
     * @return The value of the key/value pair in the config file.
     * @throws IllegalStateException If no config file was loaded before.
     * @throws IllegalArgumentException If the key cannot be found or the
     * argument was null or empty.
     */
    public static String getProperty(String key) {
        Exceptions.verifyArgumentNotEmpty(key);

        if (getInstance().configFile == null) {
            throw new IllegalStateException("You have load the config file first.");
        }

        return getInstance().configFile.getProperty(key);
    }

    static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }

    private static class ConfigHolder {

        private static final Config INSTANCE = new Config();
    }
}
