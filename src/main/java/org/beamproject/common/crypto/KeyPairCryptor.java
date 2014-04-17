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

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.beamproject.common.util.Base64;
import org.beamproject.common.util.Exceptions;

/**
 * The {@link KeyPairCryptor} allows to encrypt/decrypt {@link  KeyPair}s to/from
 * {@link Base64}ed strings. It uses PBKDF2 to strengthen the user password.<p>
 * This code is inspired by Jerry Orrs article "Secure Password Storage - Lots
 * of don'ts, a few dos, and a concrete Java SE example". See:
 * http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html.
 * He licensed his original code under Public Domain.
 */
public class KeyPairCryptor {

    public final static String PBKDF_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    public final static int NUMBER_OF_ITERATIONS = 50000;
    public final static String SALT_RANDOM_ALGORITHM_NAME = "SHA1PRNG";
    public final static int SALT_LENGTH_IN_BYTES = 16;
    public final static String SYMMETRIC_ALGORITHM_NAME = "AES";
    public final static int KEY_LENGTH_IN_BITS = 256;

    /**
     * Encrypts the given {@link KeyPair} with the password. A random salt is
     * generated and also stored into the {@link EncryptedKeyPair}.
     *
     * @param password The password to use for the encryption.
     * @param keyPair The key pair to encrypt. Both, {@link PublicKey} and
     * {@link PrivateKey} will be encrypted if they are set. If one is not set,
     * the resulting {@link EncryptedKeyPair} will contain <b>an empty
     * string</b> instead the (missing) encrypted key.
     * @return The encrypted keys.
     * @throws IllegalArgumentException If at least one argument is null.
     * @throws IllegalStateException If the salt cannot be generated or PBKDF2
     * cannot be used to strengthen the password.
     */
    public static EncryptedKeyPair encrypt(String password, KeyPair keyPair) {
        Exceptions.verifyArgumentsNotNull(password, keyPair);
        BouncyCastleIntegrator.initBouncyCastleProvider();

        byte[] salt = generateSalt();
        char[] passwordAsChars = password.toCharArray();
        Key aesKey = strengthenPasswordToAesKey(passwordAsChars, salt);
        overwritePassword(passwordAsChars);

        return encryptKeys(aesKey, keyPair, salt);
    }

    private static byte[] generateSalt() {
        try {
            SecureRandom random = SecureRandom.getInstance(SALT_RANDOM_ALGORITHM_NAME);
            byte[] salt = new byte[SALT_LENGTH_IN_BYTES];
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not generate salt for the store key: " + ex.getMessage());
        }
    }

    private static Key strengthenPasswordToAesKey(char[] password, byte[] salt) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF_ALGORITHM_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            KeySpec keySpec = new PBEKeySpec(password, salt, NUMBER_OF_ITERATIONS, KEY_LENGTH_IN_BITS);
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            return new SecretKeySpec(secretKey.getEncoded(), SYMMETRIC_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
            throw new IllegalStateException("Could not strengthen the store password with PBKDF2: " + ex.getMessage());
        }
    }

    private static EncryptedKeyPair encryptKeys(Key aesKey, KeyPair keyPair, byte[] salt) {
        AesCbcCipher cipher = new AesCbcCipher(aesKey.getEncoded());
        String publicKeyAsBase64 = "";
        String privateKeyAsBase64 = "";
        String saltAsBase64 = Base64.encode(salt);

        if (keyPair.getPublic() != null) {
            byte[] encryptedPublicKey = cipher.encrypt(keyPair.getPublic().getEncoded());
            publicKeyAsBase64 = Base64.encode(encryptedPublicKey);
        }

        if (keyPair.getPrivate() != null) {
            byte[] encryptedPrivateKey = cipher.encrypt(keyPair.getPrivate().getEncoded());
            privateKeyAsBase64 = Base64.encode(encryptedPrivateKey);
        }

        return new EncryptedKeyPair(publicKeyAsBase64, privateKeyAsBase64, saltAsBase64);
    }

    /**
     * Decrypts the given {@link EncryptedKeyPair} with the password. At least
     * the {@link PublicKey} has to be set.
     *
     * @param password The password to use for the decryption.
     * @param encryptedKeyPair The key pair to decrypt.
     * @return The decrypted keys.
     * @throws IllegalArgumentException If at least one argument is null.
     * @throws IllegalArgumentException If neither the PublicKey nor the
     * PrivateKey is set in the given KeyPair.
     * @throws IllegalStateException If PBKDF2 cannot be used to strengthen the
     * password.
     */
    public static KeyPair decrypt(String password, EncryptedKeyPair encryptedKeyPair) {
        Exceptions.verifyArgumentsNotNull(password, encryptedKeyPair);
        BouncyCastleIntegrator.initBouncyCastleProvider();

        char[] passwordAsChars = password.toCharArray();
        Key aesKey = strengthenPasswordToAesKey(passwordAsChars, encryptedKeyPair.getSaltAsBytes());
        overwritePassword(passwordAsChars);

        return decryptKeys(aesKey, encryptedKeyPair);
    }

    private static KeyPair decryptKeys(Key aesKey, EncryptedKeyPair encryptedKeyPair) {
        AesCbcCipher cipher = new AesCbcCipher(aesKey.getEncoded());
        byte[] decryptedPublicKey = null;
        byte[] decryptedPrivateKey = null;

        if (encryptedKeyPair.getEncryptedPublicKey() != null
                && !encryptedKeyPair.getEncryptedPublicKey().isEmpty()) {
            decryptedPublicKey = cipher.decrypt(encryptedKeyPair.getEncryptedPublicKeyAsBytes());
        }

        if (encryptedKeyPair.getEncryptedPrivateKey() != null
                && !encryptedKeyPair.getEncryptedPrivateKey().isEmpty()) {
            decryptedPrivateKey = cipher.decrypt(encryptedKeyPair.getEncryptedPrivateKeyAsBytes());
        }

        if (decryptedPublicKey != null && decryptedPrivateKey != null) {
            return EccKeyPairGenerator.fromBothKeys(decryptedPublicKey, decryptedPrivateKey);
        }

        if (decryptedPublicKey != null) {
            return EccKeyPairGenerator.fromPublicKey(decryptedPublicKey);
        }

        throw new IllegalArgumentException("Could not find non-null keys in the given KeyPair. At least the PublicKey has to be.");
    }

    static void overwritePassword(char[] password) {
        for (int i = 0; i < password.length; i++) {
            password[i] = 0;
        }
    }

}
