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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.inchat.common.crypto.KeyPairStore;
import org.inchat.common.util.Exceptions;

/**
 * This singleton can be used to load a specific configuration file and keep
 * important things with easy (singleton) access at one place.
 */
public class Config {

    /**
     * This is a collection of all keys (of the key/value pairs) of the inchat
     * config files.
     */
    public static enum Key {

        /**
         * The filename of the key pair of this {@link Participant}.
         */
        /**
         * The filename of the key pair of this {@link Participant}.
         */
        keyPairFilename,
        /**
         * The password which is used to encrypt/decrypt the key pair file.
         */
        keyPairPassword,
        /**
         * The name of this {@link Participant}.
         */
        participantName,
        /**
         * Only relevant for the client. The url of the server, on which the
         * client has its account. //TODO This should be refactored.
         */
        serverUrl,
        /**
         * Only relevant for the client. The X position of the desktop client
         * window. //TODO This should be refactored.
         */
        windowPositionX,
        /**
         * Only relevant for the client. The Y position of the desktop client
         * window. //TODO This should be refactored.
         */
        windowPositionY
    }
    private final static String DEFAULT_CONFIG_CLASSPATH = "/org/inchat/common/defaults.conf";
    File configFile;
    File configDirectory;
    Properties config;
    Participant participant;
    boolean isLoaded = false;

    Config() {
        // Only static access and from the test package.
    }

    public static boolean isLoaded() {
        return getInstance().isLoaded;
    }

    /**
     * Loads the config file. It has to be a key/value pair file like the Java
     * properties files.
     *
     * @param path The file to read.
     * @throws IllegalArgumentException If the argument is null or the file
     * cannot be found.
     */
    public static void loadConfig(String path) {
        Exceptions.verifyArgumentNotEmpty(path);

        getInstance().config = new Properties();
        File configFile = new File(path).getAbsoluteFile();

        try (FileInputStream fileStream = new FileInputStream(configFile)) {
            getInstance().config.load(fileStream);
            fileStream.close();
            getInstance().configFile = configFile;
            getInstance().configDirectory = configFile.getParentFile();
            getInstance().isLoaded = true;
        } catch (IOException ex) {
            String message = "The file cannot be found.";
            String currentPath;

            try {
                currentPath = (new File("")).getCanonicalPath();
            } catch (IOException ex1) {
                throw new IllegalArgumentException(message);
            }

            throw new IllegalArgumentException(message + " Enter the path relative to '" + currentPath + "'. The path of your file was '" + path + "'.");
        }
    }

    /**
     * Writes the inchat default configs to the given file.
     *
     * @param path The file, where the new config file should be. This may not
     * be null and the path has to be writable. The file must not exist before
     * the invocation of this method.
     * @throws IllegalArgumentException If the argument is null or the file
     * cannot be created.
     * @throws IllegalPathStateException If the file already exists or it could
     * not be written.
     */
    public static void createDefaultConfig(String path) {
        Exceptions.verifyArgumentNotEmpty(path);

        File file = new File(path);

        if (file.exists()) {
            throw new IllegalPathStateException("The config could not be written since the path already exists.");
        }

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            file.createNewFile();
            writeDefaultsToFile(file);
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
    public static String getProperty(Key key) {
        Exceptions.verifyArgumentNotNull(key);

        if (getInstance().config == null) {
            throw new IllegalStateException("You have load the config file first.");
        }

        return getInstance().config.getProperty(key.toString());
    }

    /**
     * This method writes/adds/updates a property key/value pair to the config
     * file.
     *
     * @param key The key that has to be set.
     * @param value The value for the key.
     * @throws IllegalArgumentException If at lest one of the arguments is null.
     * @throws IllegalStateException If the config file was not loaded before or
     * if it could not be stored correctly.
     */
    public static void setProperty(Key key, String value) {
        Exceptions.verifyArgumentsNotNull(key, value);

        getInstance().config.setProperty(key.toString(), value);

        try {
            FileOutputStream output = new FileOutputStream(getInstance().configFile);
            getInstance().config.store(output, null);
        } catch (IOException ex) {
            throw new IllegalStateException("The file could not be stored correctly.");
        }
    }

    /**
     * Loads the {@link Participant} with the keys, configured in the config
     * file. When no {@link Participant} is existing, a new one will be created.
     *
     * @return The participant.
     * @throws IllegalStateException If the {@link Config} is not loaded at the
     * time of invocation.
     */
    public static Participant loadOrCreateParticipant() {
        if (!isLoaded()) {
            throw new IllegalStateException("The config file has to be loaded first.");
        }

        if (isKeyPairExisting()) {
            loadParticipant();
        } else {
            createAndStoreNewParticipant();
        }

        return null;
    }

    private static boolean isKeyPairExisting() {
        String directory = getInstance().configDirectory.getAbsolutePath() + File.separator;
        String keyPairFilename = getProperty(Key.keyPairFilename);

        File privateKeyFile = new File(directory + keyPairFilename + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
        File publicKeyFile = new File(directory + keyPairFilename + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
        File saltFile = new File(directory + keyPairFilename + KeyPairStore.SALT_FILE_EXTENSION);

        if (!privateKeyFile.exists()) {
            return false;
        }

        if (!publicKeyFile.exists()) {
            return false;
        }

        return saltFile.exists();
    }

    private static void loadParticipant() {
        String directory = getInstance().configDirectory.getAbsolutePath() + File.separator;
        String keyPairFilename = getProperty(Key.keyPairFilename);
        String keyPairPassword = getProperty(Key.keyPairPassword);

        KeyPairStore store = new KeyPairStore(keyPairPassword, directory + keyPairFilename);
        getInstance().participant = new Participant(store.readKeys());
    }

    private static void createAndStoreNewParticipant() {
        String directory = getInstance().configDirectory.getAbsolutePath() + File.separator;
        String keyPairFilename = getProperty(Key.keyPairFilename);
        String keyPairPassword = getProperty(Key.keyPairPassword);

        getInstance().participant = new Participant(EccKeyPairGenerator.generate());

        KeyPairStore store = new KeyPairStore(keyPairPassword, directory + keyPairFilename);
        store.storeKeys(getInstance().participant.getKeyPair());
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
