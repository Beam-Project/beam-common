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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * This encrypts and decrypts AES in CBC mode.
 */
public class AesCbcCipher {

    private final int[] VALID_KEY_LENGTHS_IN_BYTES = {16, 24, 32};
    PaddedBufferedBlockCipher cipher;
    CipherParameters parameters;

    /**
     * The block size of the used AES engine is 128 bits (16 bytes). The key has
     * to be 16/24/32 bytes in length.
     *
     * @param key Has to be 16/24/32 bytes long, may not be null.
     * @throws IllegalArgumentException If the argument is null or not as long
     * as required.
     */
    public AesCbcCipher(byte[] key) {
        if (!isKeyValid(key)) {
            throw new IllegalArgumentException("The key has to be 16, 24 or 32 bytes long.");
        }

        initCipher(key);
    }

    private boolean isKeyValid(byte[] key) {
        for (int length : VALID_KEY_LENGTHS_IN_BYTES) {
            if (key != null && key.length == length) {
                return true;
            }
        }

        return false;
    }

    private void initCipher(byte[] key) {
        BouncyCastleIntegrator.initBouncyCastleProvider();
        parameters = new KeyParameter(key);
        cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    }

    /**
     * Encrypts the given {@code plaintext}.
     *
     * @param plaintext May not be null.
     * @return The ciphertext. The length is a multiple of 16 bytes.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If anything goes wrong during encryption.
     */
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        try {
            initCipherForEncryption();
            byte[] outputBuffer = new byte[cipher.getOutputSize(plaintext.length)];
            int processedBytes = cipher.processBytes(plaintext, 0, plaintext.length, outputBuffer, 0);
            processedBytes += cipher.doFinal(outputBuffer, processedBytes);
            return copyContentBytes(outputBuffer, processedBytes);
        } catch (IllegalArgumentException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
            throw new CryptoException("Could not encrypt the plaintext: " + ex.getMessage());
        }
    }

    private void initCipherForEncryption() {
        boolean isForEncryption = true;
        cipher.init(isForEncryption, parameters);
    }

    /**
     * Decrypts the given {@code ciphertext}.
     *
     * @param ciphertext May not be null.
     * @return The plaintext.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If anything goes wrong during decryption.
     */
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        try {
            initCipherForDecryption();
            byte[] outputBuffer = new byte[cipher.getOutputSize(ciphertext.length)];
            int processedBytes = cipher.processBytes(ciphertext, 0, ciphertext.length, outputBuffer, 0);
            processedBytes += cipher.doFinal(outputBuffer, processedBytes);
            return copyContentBytes(outputBuffer, processedBytes);
        } catch (IllegalArgumentException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
            throw new CryptoException("Could not decrypt the ciphertext: " + ex.getMessage());
        }
    }

    private void initCipherForDecryption() {
        boolean isForEncryption = false;
        cipher.init(isForEncryption, parameters);
    }

    private byte[] copyContentBytes(byte[] withPadding, int bytesToRemove) {
        byte[] withoutPadding = new byte[bytesToRemove];
        System.arraycopy(withPadding, 0, withoutPadding, 0, bytesToRemove);

        return withoutPadding;
    }
}
