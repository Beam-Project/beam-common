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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import javax.crypto.IllegalBlockSizeException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.jcajce.provider.asymmetric.ec.IESCipher;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Exceptions;

/**
 * Allows to encrypt and decrypt data asymmetrically using Elliptic Curve
 * Cryptography (ECC).
 * <p>
 * Parameters:
 * <ul>
 * <li>Used Curve: depending on the {@link EccKeyPairGenerator} key</li>
 * <li>Engine Mode: {@code DHAES}</li>
 * <li>KDF: uses {@link KDF2BytesGenerator} with SHA-256 as digest</li>
 * <li>MAC: 256 bits key size, SHA-256 as digest</li>
 * <li>Symmetric Block Cipher: AES with 256 bits key size, 128 bits block size,
 * in CBC mode and padded with {@link PKCS7Padding}</li>
 * </ul>
 */
public class EccCipher {

    public final static String ENGINE_MODE = "DHAES";
    public final static int MAC_KEY_SIZE_IN_BITS = 256;
    public final static int AES_KEY_SIZE_IN_BITS = 256;
    IESEngine engine;
    IESCipher cipher;
    IESParameterSpec parameterSpec;

    /**
     * Initializes the cipher.
     *
     * @throws IllegalStateException If the cipher could not be set up
     * correctly.
     */
    public EccCipher() {
        initCipher();
    }

    private void initCipher() {
        engine = new IESEngine(new ECDHBasicAgreement(),
                new KDF2BytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine())));
        cipher = new IESCipher(engine);
        parameterSpec = new IESParameterSpec(null, null, MAC_KEY_SIZE_IN_BITS, AES_KEY_SIZE_IN_BITS);

        try {
            cipher.engineSetMode(ENGINE_MODE);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not initialize the cipher correctly since the mode " + ENGINE_MODE + " is not available: " + ex.getMessage());
        }
    }

    /**
     * Encrypts the given plaintext with the initialized public key of the
     * remote participant.
     *
     * @param plaintext The plaintext to encrypt, may not be null.
     * @param remotePublicKey The public key of the remote {@link Participant}
     * to encrypt the plaintext.
     * @return The ciphertext.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If something goes wrong during encryption. This
     * may not be null.
     */
    public byte[] encrypt(byte[] plaintext, PublicKey remotePublicKey) {
        Exceptions.verifyArgumentsNotNull(plaintext, remotePublicKey);

        try {
            cipher.engineInit(ENCRYPT_MODE, remotePublicKey, parameterSpec, new SecureRandom());
            return cipher.engineDoFinal(plaintext, 0, plaintext.length);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            throw new CryptoException("Could not encrypt the given plaintext: " + ex.getMessage());
        }
    }

    /**
     * Decrypts the given ciphertext with the initialized private key of the
     * local participant.
     *
     * @param ciphertext The ciphertext to decrypt, may not be null.
     * @param localPrivateKey The private key of the local {@link Participant}
     * to decrypt the ciphertext. This may not be null.
     * @return The plaintext.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If something goes wrong during decryption.
     */
    public byte[] decrypt(byte[] ciphertext, PrivateKey localPrivateKey) {
        Exceptions.verifyArgumentsNotNull(ciphertext, localPrivateKey);

        try {
            cipher.engineInit(DECRYPT_MODE, localPrivateKey, parameterSpec, new SecureRandom());
            return cipher.engineDoFinal(ciphertext, 0, ciphertext.length);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            throw new CryptoException("Could not decrypt the given ciphertext: " + ex.getMessage());
        }
    }
}
