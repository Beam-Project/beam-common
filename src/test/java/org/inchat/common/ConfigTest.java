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
import org.inchat.common.crypto.KeyPairStore;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConfigTest {

    private final static String DEFAULT_TEST_FILE = "test-default-config.conf";
    private final static Config.Key TEST_KEY = Config.Key.keyPairFilename;
    private final static String KEY_PAIR_FILENAME = "keypair";
    private final static String TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY = "NEW_DIRECTORY/test-write-defaults.conf";
    private Participant participant;

    @Before
    @After
    public void cleanUp() {
        File defaultTestFile = new File(DEFAULT_TEST_FILE);

        if (defaultTestFile.exists()) {
            defaultTestFile.delete();
        }

        deleteKeyPairFiles();
    }

    private void deleteKeyPairFiles() {
        File privateKey = getPrivateKey();
        File publicKey = getPublicKey();
        File salt = getSalt();

        if (privateKey.exists()) {
            privateKey.delete();
        }

        if (publicKey.exists()) {
            publicKey.delete();
        }

        if (salt.exists()) {
            salt.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNull() {
        Config.loadConfig(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnEmptyString() {
        Config.loadConfig("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigFileOnNotExistingFile() {
        Config.loadConfig("oops -- file not found");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteDefaultConfigOnNull() {
        Config.createDefaultConfig(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteDefaultConfigOnEmptyString() {
        Config.createDefaultConfig("");
    }

    @Test(expected = IllegalPathStateException.class)
    public void testWriteDefaultConfigOnExistingDirectory() {
        Config.createDefaultConfig("./");
    }

    @Test
    public void testWriteDefaultConfigWithExistingDirectory() throws IOException {
        deleteTestDefaultsFile();

        Config.createDefaultConfig(DEFAULT_TEST_FILE);

        File configFile = new File(DEFAULT_TEST_FILE);
        assertTrue(configFile.exists());

        String writtenConfig = readFile(configFile);
        String expectedConfig = Config.getDefaultConfig();
        assertEquals(expectedConfig, writtenConfig);

        deleteTestDefaultsFile();
    }

    @Test
    public void testWriteDefaultConfigWithNewDirectory() throws IOException {
        deleteTestDefaultsFileWithNewDirectory();

        Config.createDefaultConfig(TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY);

        File configFile = new File(TEST_CONFIG_DEFAULTS_IN_NEW_DIRECTORY);
        assertTrue(configFile.exists());

        String writtenConfig = readFile(configFile);
        String expectedConfig = Config.getDefaultConfig();
        assertEquals(expectedConfig, writtenConfig);

        deleteTestDefaultsFileWithNewDirectory();
    }

    private void deleteTestDefaultsFile() {
        File target = new File(DEFAULT_TEST_FILE);
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
    public void testLoadConfig() throws IOException {
        Config.createDefaultConfig(DEFAULT_TEST_FILE);
        Config.loadConfig(DEFAULT_TEST_FILE);
        assertNotNull(Config.getInstance().config);

        String actualValue = Config.getProperty(TEST_KEY);
        assertEquals(KEY_PAIR_FILENAME, actualValue);

        File configFile = new File(DEFAULT_TEST_FILE).getAbsoluteFile();
        assertEquals(configFile.getParentFile().getAbsolutePath(),
                Config.getInstance().configDirectory.getAbsolutePath());
        assertTrue(Config.isLoaded());
    }

    @Test
    public void testIsLoaded() {
        Config.getInstance().isLoaded = false;
        assertFalse(Config.isLoaded());

        Config.getInstance().isLoaded = true;
        assertTrue(Config.isLoaded());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyOnNull() {
        Config.getProperty(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetPropertyOnMissingConfigFile() {
        Config.getInstance().config = null;
        Config.getProperty(TEST_KEY);
    }

    @Test
    public void testGetProperty() {
        Config.createDefaultConfig(DEFAULT_TEST_FILE);
        Config.loadConfig(DEFAULT_TEST_FILE);
        assertEquals(KEY_PAIR_FILENAME, Config.getProperty(TEST_KEY));
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadOrCreateParticipantOnNotLoadedConfig() {
        Config.getInstance().isLoaded = false;
        Config.loadOrCreateParticipant();
    }

    @Test
    public void testLoadOrCreateParticipantOnCreatingKey() {
        Config.createDefaultConfig(DEFAULT_TEST_FILE);
        Config.loadConfig(DEFAULT_TEST_FILE);
        deleteKeyPairFiles();
        Config.loadOrCreateParticipant();

        File publicKey = getPublicKey();
        File privateKey = getPrivateKey();
        File salt = getSalt();

        if (!publicKey.exists()) {
            fail("Public key not created.");
        }

        if (!privateKey.exists()) {
            fail("Private key not created.");
        }

        if (!salt.exists()) {
            fail("Salt not created.");
        }
    }

    @Test
    public void testContextInitOnLoadingKeyPair() {
        deleteKeyPairFiles();
        Config.createDefaultConfig(DEFAULT_TEST_FILE);
        Config.loadConfig(DEFAULT_TEST_FILE);

        createAndStoreKeyPair();
        Config.loadOrCreateParticipant();

        assertArrayEquals(participant.getPublicKeyAsBytes(),
                Config.getParticipant().getPublicKeyAsBytes());

        assertArrayEquals(participant.getPrivateKeyAsBytes(),
                Config.getParticipant().getPrivateKeyAsBytes());
    }

    private void createAndStoreKeyPair() {
        participant = new Participant(EccKeyPairGenerator.generate());
        KeyPairStore store = new KeyPairStore(Config.getProperty(Config.Key.keyPairPassword),
                Config.getProperty(Config.Key.keyPairFilename));
        store.storeKeys(participant.getKeyPair());
    }

    private File getPublicKey() {
        return new File(KEY_PAIR_FILENAME + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
    }

    private File getPrivateKey() {
        return new File(KEY_PAIR_FILENAME + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
    }

    private File getSalt() {
        return new File(KEY_PAIR_FILENAME + KeyPairStore.SALT_FILE_EXTENSION);
    }

    @Test
    public void testGetParticipant() {
        Config.getInstance().participant = null;
        assertNull(Config.getParticipant());
        Config.getInstance().participant = participant;
        assertSame(participant, Config.getParticipant());
    }

}
