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

import org.beamproject.common.util.Base58;

/**
 * This class is a simple holder for the output of the {@link KeyPairCryptor}.
 */
public class EncryptedKeyPair {

    byte[] encryptedPublicKey;
    byte[] encryptedPrivateKey;
    byte[] salt;

    /**
     * Initializes the new instance of {@link EncryptedKeyPair} with the needed
     * data: encrypted public and private key and the salt.
     * <p>
     * If an argument is null, it is replaced by an empty array.
     *
     * @param encryptedPublicKey The encrypted public key.
     * @param encryptedPrivateKey The encrypted private key.
     * @param salt The salt used for the encryption.
     */
    public EncryptedKeyPair(byte[] encryptedPublicKey, byte[] encryptedPrivateKey, byte[] salt) {
        this.encryptedPublicKey = encryptedPublicKey == null ? new byte[0] : encryptedPublicKey;
        this.encryptedPrivateKey = encryptedPrivateKey == null ? new byte[0] : encryptedPrivateKey;
        this.salt = salt == null ? new byte[0] : salt;
    }

    /**
     * @return The encrypted public key as String, {@link Base58} encoded.
     */
    public String getEncryptedPublicKey() {
        return Base58.encode(encryptedPublicKey);
    }

    /**
     * @return The encrypted private key as String, {@link Base58} encoded.
     */
    public String getEncryptedPrivateKey() {
        return Base58.encode(encryptedPrivateKey);
    }

    /**
     * @return The salt which was used to encrypt the keys, {@link Base58}
     * encoded.
     */
    public String getSalt() {
        return Base58.encode(salt);
    }

    /**
     * @return The encrypted public key as byte array.
     */
    public byte[] getEncryptedPublicKeyAsBytes() {
        return encryptedPublicKey;
    }

    /**
     * @return The encrypted private key as byte array.
     */
    public byte[] getEncryptedPrivateKeyAsBytes() {
        return encryptedPrivateKey;
    }

    /**
     * @return The salt which was used to encrypt the keys, as byte array.
     */
    public byte[] getSaltAsBytes() {
        return salt;
    }
}
