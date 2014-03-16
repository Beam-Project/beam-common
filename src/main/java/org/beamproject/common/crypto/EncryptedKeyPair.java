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

import org.beamproject.common.util.Base64;
import org.beamproject.common.util.Exceptions;

/**
 * This class is a simple holder for the output of the {@link KeyPairCryptor}.
 */
public class EncryptedKeyPair {

    String encryptedPublicKey;
    String encryptedPrivateKey;
    String salt;

    /**
     * Initializes the new instance of {@link EncryptedKeyPair} with the needed
     * data: encrypted public and private key and the salt. All strings have to
     * be {@link Base64} encoded!
     *
     * @param encryptedPublicKey The encrypted public key, encoded as
     * {@link Base64}.
     * @param encryptedPrivateKey The encrypted private key, encoded as
     * {@link Base64}.
     * @param salt The salt used for the encryption, encoded as {@link Base64}.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public EncryptedKeyPair(String encryptedPublicKey, String encryptedPrivateKey, String salt) {
        Exceptions.verifyArgumentsNotNull(encryptedPublicKey, encryptedPrivateKey, salt);

        this.encryptedPublicKey = encryptedPublicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.salt = salt;
    }

    /**
     * @return The encrypted public key as String, {@link Base64} encoded.
     */
    public String getEncryptedPublicKey() {
        return encryptedPublicKey;
    }

    /**
     * @return The encrypted private key as String, {@link Base64} encoded.
     */
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /**
     * @return The salt which was used to encrypt the keys, {@link Base64}
     * encoded.
     */
    public String getSalt() {
        return salt;
    }

    /**
     * @return The encrypted public key as byte array (the data is not
     * {@link Base64} encoded anymore).
     */
    public byte[] getEncryptedPublicKeyAsBytes() {
        return Base64.decode(encryptedPublicKey);
    }

    /**
     * @return The encrypted private key as byte array (the data is not
     * {@link Base64} encoded anymore).
     */
    public byte[] getEncryptedPrivateKeyAsBytes() {
        return Base64.decode(encryptedPrivateKey);
    }

    /**
     * @return The salt which was used to encrypt the keys, as byte array (the
     * data is not {@link Base64} encoded anymore).
     */
    public byte[] getSaltAsBytes() {
        return Base64.decode(salt);
    }
}
