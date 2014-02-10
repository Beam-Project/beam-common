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
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.jcajce.provider.asymmetric.ec.IESCipher;

/**
 * This {@link Cipher} allows to encrypt and decrypt data asymmetrically using
 * Elliptic Curve Cryptography (ECC).
 */
public class EccCipher implements Cipher {

    public final static String ENGINE_MODE = "DHAES";
    PrivateKey localPrivateKey;
    PublicKey remotePublicKey;
    IESEngine engine;
    IESCipher cipher;

    /**
     * Initializes the cipher with the given key pairs. To decrypt the
     * {@code localKeyPair} has to contain the private key. To encrypt for the
     * remote user, the {@code keyPair} has to contain at least the public key.
     *
     * @param localPrivateKey The private key of the local participant. This may
     * not be null.
     * @param remotePublicKey The public key of the remote participant. This may
     * not be null.
     * @throws IllegalArgumentException If the arguments are null.
     */
    public EccCipher(PrivateKey localPrivateKey, PublicKey remotePublicKey) {
        if (localPrivateKey == null || remotePublicKey == null) {
            throw new IllegalArgumentException("The arguments may not be null.");
        }

        this.localPrivateKey = localPrivateKey;
        this.remotePublicKey = remotePublicKey;

        initCipher();
    }

    private void initCipher() {
        engine = new IESEngine(new ECDHBasicAgreement(),
                new KDF2BytesGenerator(new SHA1Digest()),
                new HMac(new SHA256Digest()),
                new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine())));
        cipher = new IESCipher(engine);

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
     * @return The ciphertext.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If something goes wrong during encryption.
     */
    @Override
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        try {
            cipher.engineInit(ENCRYPT_MODE, remotePublicKey, new SecureRandom());
            return cipher.engineDoFinal(plaintext, 0, plaintext.length);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException("Could not encrypt the given plaintext: " + ex.getMessage());
        }
    }

    /**
     * Decrypts the given ciphertext with the initialized private key of the
     * local participant.
     *
     * @param ciphertext The ciphertext to decrypt, may not be null.
     * @return The plaintext.
     * @throws IllegalArgumentException If the argument is null.
     * @throws CryptoException If something goes wrong during decryption.
     */
    @Override
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        try {
            cipher.engineInit(DECRYPT_MODE, localPrivateKey, new SecureRandom());
            return cipher.engineDoFinal(ciphertext, 0, ciphertext.length);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException("Could not decrypt the given ciphertext: " + ex.getMessage());
        }
    }
}
