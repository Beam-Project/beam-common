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
package org.inchat.common.crypto;

import java.io.File;
import java.security.KeyPair;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class KeyPairStoreTest {

    private final String PASSWORD = "12345678";
    private final String FILENAME = "keystore";
    private KeyPairStore store;
    private KeyPair keyPair;

    @Before
    public void setUp() {
        store = new KeyPairStore(PASSWORD, FILENAME);
        keyPair = EccKeyPairGenerator.generate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        store = new KeyPairStore(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullPassword() {
        store = new KeyPairStore(null, FILENAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullFilename() {
        store = new KeyPairStore(PASSWORD, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnEmptyPassword() {
        store = new KeyPairStore("", FILENAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnEmptyFilename() {
        store = new KeyPairStore(PASSWORD, "");
    }

    @Test
    public void testConstructorOnAssignment() {
        store = new KeyPairStore(PASSWORD, FILENAME);

        assertEquals(PASSWORD, store.password);
        assertEquals(FILENAME, store.filename);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreKeysOnNull() {
        store.storeKeys(null);
    }

    @Test
    public void testStoreKeysOnGeneratingSaltAndKey() {
        store.storeKeys(keyPair);
        assertNotNull(store.aesKey);
        assertEquals(KeyPairStore.SALT_LENGTH_IN_BYTES, store.salt.length);
    }

    @Test
    public void testStoreKeys() {
        cleanupExistingStores();

        store.storeKeys(keyPair);

        File privateKey = new File(FILENAME + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
        File publicKey = new File(FILENAME + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
        File salt = new File(FILENAME + KeyPairStore.SALT_FILE_EXTENSION);

        assertTrue(privateKey.exists());
        assertTrue(publicKey.exists());
        assertTrue(salt.exists());
    }

    @Test
    public void testStoreAndReadKeys() {
        cleanupExistingStores();

        KeyPairStore store1 = new KeyPairStore(PASSWORD, FILENAME);
        store1.storeKeys(keyPair);

        KeyPairStore store2 = new KeyPairStore(PASSWORD, FILENAME);
        KeyPair keyPair2 = store2.readKeys();

        assertArrayEquals(keyPair.getPublic().getEncoded(), keyPair2.getPublic().getEncoded());
        assertEquals(keyPair.getPublic().getAlgorithm(), keyPair2.getPublic().getAlgorithm());
        assertEquals(keyPair.getPublic().getFormat(), keyPair2.getPublic().getFormat());

        assertArrayEquals(keyPair.getPrivate().getEncoded(), keyPair2.getPrivate().getEncoded());
        assertEquals(keyPair.getPrivate().getAlgorithm(), keyPair2.getPrivate().getAlgorithm());
        assertEquals(keyPair.getPrivate().getFormat(), keyPair2.getPrivate().getFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadKeysOnMissingFiles() {
        cleanupExistingStores();
        store.storeKeys(keyPair);
        
        cleanupExistingStores();
        keyPair = store.readKeys();
    }

    private void cleanupExistingStores() {
        File existingPrivateKey = new File(FILENAME + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
        File existingPublicKey = new File(FILENAME + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
        File existingSalt = new File(FILENAME + KeyPairStore.SALT_FILE_EXTENSION);

        if (existingPrivateKey.exists()) {
            existingPrivateKey.delete();
        }

        if (existingPublicKey.exists()) {
            existingPublicKey.delete();
        }

        if (existingSalt.exists()) {
            existingSalt.delete();
        }
    }

}
