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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;
import org.inchat.common.util.Exceptions;

/**
 * This singleton can be used to load a specific configuration file and keep
 * important things with easy (singleton) access at one place.
 */
public class Config {

    /**
     * This is a collection of all default keys (of the key/value pairs) of the
     * inchat config files.
     */
    public static enum Keys {

        keyPairFilename,
        keyPairPassword
    }
    public final static String DEFAULT_CONFIG_CLASSPATH = "/org/inchat/common/defaults.conf";
    Properties configFile;
    Participant participant;

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
     * Writes the inchat default configs to the given file.
     *
     * @param target The file, where the new config file should be. This may not
     * be null and the path has to be writable. The file must not exists before
     * the invocation of this method.
     * @throws IllegalArgumentException If the argument is null or the file
     * cannot be created.
     * @throws IllegalPathStateException If the file already exists or it could
     * not be written.
     */
    public static void createDefaultConfig(File target) {
        Exceptions.verifyArgumentNotNull(target);

        if (target.exists()) {
            throw new IllegalPathStateException("The config could not be written since the path already exists.");
        }

        if (target.getParentFile() != null && !target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }

        try {
            target.createNewFile();
            writeDefaultsToFile(target);
        } catch (IOException ex) {
            throw new IllegalPathStateException("The config could not be written: " + ex.getMessage());
        }
    }

    private static void writeDefaultsToFile(File target) throws IOException {
        try (FileWriter writer = new FileWriter(target)) {
            writer.write(getDefaultConfig());
        }
    }

    static String getDefaultConfig() throws IOException {
        InputStream stream = Config.class.getResourceAsStream(DEFAULT_CONFIG_CLASSPATH);
        StringWriter writer = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        char[] buffer = new char[1024];
        int length;
        
        while ((length = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, length);
        }

        return writer.toString();
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

    /**
     * Keeps a reference to the given {@link Participant}. This is just to keep
     * the important things at one place with easy access.
     *
     * @param participant The participant to set. This may not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static void setParticipant(Participant participant) {
        Exceptions.verifyArgumentNotNull(participant);

        getInstance().participant = participant;
    }

    /**
     * Returns the earlier set {@link Participant}. If no one was set,
     * {@code null} will be returned.
     *
     * @return The {@link Participant} or {@code null}.
     */
    public static Participant getParticipant() {
        return getInstance().participant;
    }

    static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }

    private static class ConfigHolder {

        private static final Config INSTANCE = new Config();
    }
}
