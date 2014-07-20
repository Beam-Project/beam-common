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
package org.beamproject.common.crypto;

import java.util.Properties;
import org.beamproject.common.util.Base58;
import static org.easymock.EasyMock.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class EncryptedConfigTest {

    private enum Key {

        PASSWORD;
    }
    private final char[] password = "mypass".toCharArray();
    private final byte[] salt = PasswordCryptor.generateSalt();
    private EncryptedConfig<Key> config;

    @Before
    public void setUp() {
        config = new EncryptedConfig<>(password, salt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullProperties() {
        config = new EncryptedConfig<>(password, salt, null);
    }

    @Test
    public void testConstructor() {
        Properties properties = new Properties();
        byte[] encryptedValue = config.cryptor.encrypt("1234".getBytes());
        String base58edValue = Base58.encode(encryptedValue);
        properties.setProperty(Key.PASSWORD.toString(), base58edValue);
        config = new EncryptedConfig<>(password, salt, properties);

        assertEquals("1234", config.getAsString(Key.PASSWORD));
        assertSame(password, config.getPassword());
        assertSame(salt, config.getSalt());
    }

    @Test
    public void testCopyToPropertiesOnEmptyConfig() {
        Properties properties = config.copyToProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testChangePassword() {
        char[] newPassword = "new password".toCharArray();
        config.cryptor = createMock(PasswordCryptor.class);
        config.cryptor.changePassword(newPassword);
        expectLastCall();
        replay(config.cryptor);

        config.changePassword(newPassword);

        verify(config.cryptor);
    }

    @Test
    public void testCopyToProperties() {
        String value = "1234";
        config.set(Key.PASSWORD, value);
        Properties properties = config.copyToProperties();

        String base58edCiphertext = properties.getProperty(Key.PASSWORD.toString());
        byte[] ciphertext = Base58.decode(base58edCiphertext);
        byte[] plaintext = config.cryptor.decrypt(ciphertext);
        assertEquals(value, new String(plaintext));
    }

}
