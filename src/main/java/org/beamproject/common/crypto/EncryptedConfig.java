/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-client.
 *
 * beam-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.crypto;

import java.util.Properties;
import lombok.Getter;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Config;
import org.beamproject.common.util.Exceptions;

/**
 * This class abstracts form {@link Properties} to enable a strongly typed
 * interface and also encrypt all values (not the keys!) using a given password
 * and salt.
 *
 * @param <T> An enum which has to be used with an instance of EncryptedConfig.
 */
public class EncryptedConfig<T extends Enum<T>> extends Config<T> {

    @Getter
    private char[] password;
    @Getter
    private final byte[] salt;
    PasswordCryptor cryptor;

    /**
     * Creates a new instance of {@link EncryptedConfig} using the given
     * password and salt.
     *
     * @param password The password to use.
     * @param salt The salt to use.
     */
    public EncryptedConfig(char[] password, byte[] salt) {
        this.password = password;
        this.salt = salt;
        this.cryptor = new PasswordCryptor(password, salt);
    }

    /**
     * Creates a new instance of {@link EncryptedConfig} using the given
     * password and salt. The provided properties object is being decrypted and
     * used to fill the new instance.
     *
     * @param password The password to use.
     * @param salt The salt to use.
     * @param properties Properties, encrypted with this password and salt.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public EncryptedConfig(char[] password, byte[] salt, Properties properties) {
        this(password, salt);
        Exceptions.verifyArgumentsNotNull(properties);

        decryptAndFillPropertiesToMap(properties);
    }

    private void decryptAndFillPropertiesToMap(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            byte[] decodedValue = Base58.decode(properties.getProperty(key));
            byte[] decryptedValue = cryptor.decrypt(decodedValue);
            map.put(key, decryptedValue);
        }
    }

    public void changePassword(char[] newPassword) {
        cryptor.changePassword(newPassword);
    }

    /**
     * Encrypts and copies the stored values, encoded as {@link Base58} strings,
     * to a new {@link Properties} instance.
     *
     * @return The created and filled instance.
     */
    @Override
    public Properties copyToProperties() {
        Properties properties = new Properties();

        for (String key : map.keySet()) {
            byte[] encryptedValue = cryptor.encrypt(getByStringAsBytes(key));
            properties.setProperty(key, Base58.encode(encryptedValue));
        }

        return properties;
    }
}
