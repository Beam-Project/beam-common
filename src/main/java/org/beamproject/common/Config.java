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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.beamproject.common.util.Exceptions;

/**
 * Provides access to simple property files ({@code key = value}) which are used
 * to store configuration data.
 * <p>
 * Notice: Use only one instance of this class per config file since changes
 * will not be synchronized between different {@link Config} instances.
 */
public class Config {

    File configFile;
    File configDirectory;
    Properties config;

    /**
     * Loads the config file form the specific configFilePath. If there is no
     * config file, a new one will be created.
     *
     * @param configFilePath The path to the config file.
     * @throws IllegalArgumentException If the argument is null or empty.
     */
    public Config(String configFilePath) {
        Exceptions.verifyArgumentNotEmpty(configFilePath);

        configFile = new File(configFilePath).getAbsoluteFile();
        loadConfigOrStoreInitially();
    }

    private void loadConfigOrStoreInitially() {
        config = new Properties();

        if (configFile.exists()) {
            try (FileInputStream fileStream = new FileInputStream(configFile)) {
                config.load(fileStream);
                configDirectory = configFile.getParentFile();
            } catch (IOException ex) {
                throw new IllegalArgumentException("The file cannot be found at " + configFile.getAbsolutePath() + ": " + ex.getMessage());
            }
        } else {
            storeConfigFile();
        }
    }

    /**
     * Tells if the key in this config exits.
     *
     * @param key The key to test for.
     * @return true, if the key exists, otherwise false.
     * @throws IllegalArgumentException If the argument is null.
     */
    public boolean isKeyExisting(ConfigKey key) {
        Exceptions.verifyArgumentNotNull(key);

        return config.containsKey(key.toString());
    }

    /**
     * Searches for the {@code key} and returns its value, if found.
     *
     * @param key The key of the key/value pair in the config file.
     * @return The value of the key/value pair in the config file or null, if
     * the key cannot be found.
     * @throws IllegalArgumentException If the argument is null.
     */
    public String getProperty(ConfigKey key) {
        Exceptions.verifyArgumentNotNull(key);

        return config.getProperty(key.toString());
    }

    /**
     * This method writes/adds/updates a property key/value pair to the config
     * file.
     *
     * @param key The key that has to be set.
     * @param value The value to set.
     * @throws IllegalArgumentException If at lest one of the arguments is null.
     */
    public void setProperty(ConfigKey key, String value) {
        Exceptions.verifyArgumentsNotNull(key, value);

        config.setProperty(key.toString(), value);
        storeConfigFile();
    }

    private void storeConfigFile() {
        createPartentFolder();

        try (FileOutputStream output = new FileOutputStream(configFile)) {
            config.store(output, null);
        } catch (IOException ex) {
            throw new IllegalStateException("The file could not be stored correctly.");
        }
    }

    private void createPartentFolder() {
        configDirectory = configFile.getParentFile();

        if (configDirectory != null && !configDirectory.exists()) {
            configDirectory.mkdirs();
        }
    }

    void delete() {
        configFile.delete();
        config = new Properties();
    }

}
